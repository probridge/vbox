package com.probridge.vbox.fileupload;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.codec.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.vmm.wmi.VirtualMachine.VMState;

public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 8170636049135716502L;

	private static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);

	public UploadServlet() {
		logger.debug("File Servlet init");
	}

	private String getFunction(String str) {
		if (str == null || str.trim().length() == 0) {
			str = "listfile";
		}
		str = str.toLowerCase();
		return str;
	}

	private String getRelativePath(String str) {
		try {
			if (str == null)
				str = "";
			if (str != null && str.trim().length() > 0)
				str = new String(Hex.decode(str), "utf-8");
			if (str.indexOf('.') > 0)
				str = "";
			str.replace('\\', '/');
			//
			if (!str.endsWith("/"))
				str = str + "/";
			// remote leading backslash
			while (str.startsWith("/"))
				str = str.substring(1);
		} catch (UnsupportedEncodingException e) {
			logger.error("illegal rPath.", e);
			str = "";
		}
		return str;
	}

	private String getFileName(String str) {
		try {
			if (str != null) {
				str = new String(Hex.decode(str), "utf-8");
				str.replace('\\', '_');
				str.replace('/', '_');
				while (str.startsWith("."))
					str = str.substring(1);
				//
				while (str.endsWith("."))
					str = str.substring(0, str.length() - 1);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("illegal fName.", e);
			str = "";
		}
		return str;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//
		VM selectedVM = getSelectedVM();
		if (selectedVM == null) {
			response.sendError(404, "Selected vBox not available");
			return;
		}

		String uri = "smb://" + selectedVM.getVmIpAddress() + "/" + VBoxConfig.shareName + "/";
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", VBoxConfig.guestOSUserName,
				selectedVM.getVmGuestPassword());
		//
		request.setCharacterEncoding("utf-8");
		String fn = getFunction(request.getParameter("fn"));
		String rPath = getRelativePath(request.getParameter("rp"));
		String fName = getFileName(request.getParameter("f"));
		//
		logger.debug("GET request: fn=" + fn + ",rp=" + rPath + ",f=" + fName);

		SmbFile smbRepository = null;
		SmbFile smbPath = null;
		SmbFile smbFile = null;
		try {
			smbRepository = new SmbFile(uri, auth);
			smbPath = new SmbFile(smbRepository, rPath);
			if (fName != null)
				smbFile = new SmbFile(smbPath, fName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		logger.debug("uri: " + uri);
		if ("getrepoinfo".equals(fn)) {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter writer = response.getWriter();
			JSONObject jsono = new JSONObject();
			logger.debug("selectedVM:" + selectedVM);
			if (selectedVM.getVmStatus() == VMState.PoweredOff.getValue()
					|| selectedVM.getVmStatus() == VMState.Suspended.getValue()) {
				logger.debug("selected vm " + selectedVM.getVmId() + " not started... suggesting client to start it.");
				try {
					jsono.put("status", "TOSTART");
					jsono.put("uuid", selectedVM.getVmId());
				} catch (JSONException e) {
				}
			} else if (selectedVM.getVmStatus().equals(VMState.Resuming.getValue())
					|| selectedVM.getVmStatus().equals(VMState.Starting.getValue())
					|| (selectedVM.getVmStatus().equals(VMState.Running.getValue()) && selectedVM.getVmHeartbeat() != 2)
					|| (selectedVM.getVmStatus().equals(VMState.Running.getValue()) && selectedVM.getVmHeartbeat() == 2 && Utility
							.isEmptyOrNull(selectedVM.getVmIpAddress()))
					|| (selectedVM.getVmStatus().equals(VMState.Running.getValue()) && selectedVM.getVmHeartbeat() == 2
							&& !Utility.isEmptyOrNull(selectedVM.getVmIpAddress()) && Utility.isEmptyOrNull(selectedVM
							.getVmGuestPassword()))) {
				try {
					jsono.put("status", "TRYLATER");
					jsono.put("uuid", selectedVM.getVmId());
				} catch (JSONException e) {
				}
			} else {
				try {
					if (selectedVM.getVmIpAddress() != null && selectedVM.getVmIpAddress().length() > 0
							&& smbRepository.exists()) {
						long free = smbRepository.getDiskFreeSpace();
						long total = smbRepository.length();
						try {
							jsono.put("status", "OK");
							jsono.put("usage", total - free);
							jsono.put("total", total);
						} catch (JSONException e) {
						}
					} else {
						logger.error(fn + ": repository not exist");
						response.sendError(500);
					}
				} catch (Exception e) {
					logger.error(fn, e);
					response.sendError(500);
				}
			}
			writer.write(jsono.toString());
			writer.close();
		} else if ("getfile".equals(fn)) {
			if (smbFile.exists()) {
				int bytes = 0;
				ServletOutputStream op = response.getOutputStream();
				response.setCharacterEncoding("utf-8");
				response.setContentLength((int) smbFile.length());
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fName + "\"; filename*=UTF-8''"
						+ rfc5987_encode(fName));

				byte[] bbuf = new byte[1024];
				DataInputStream in = new DataInputStream(new SmbFileInputStream(smbFile));

				while ((in != null) && ((bytes = in.read(bbuf)) != -1)) {
					op.write(bbuf, 0, bytes);
				}

				in.close();
				op.flush();
				op.close();
			} else {
				response.sendError(404);
			}
		} else if ("ping".equals(fn)) {
			try {
				if (smbPath.canRead())
					response.setStatus(200);
				else
					response.sendError(500, "暂时不可用");
			} catch (SmbException e) {
				response.sendError(500, "暂时不可用");
			}
		} else if ("listfile".equals(fn)) {
			SmbFile[] files = smbPath.listFiles(VBoxConfig.fileFilter);
			if (files != null) {
				response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
				PrintWriter writer = response.getWriter();
				JSONArray json = new JSONArray();
				JSONObject out = new JSONObject();
				try {
					for (int i = 0; i < files.length; i++) {
						if (files[i].isHidden()
								|| (files[i].getAttributes() & SmbFile.ATTR_SYSTEM) == SmbFile.ATTR_SYSTEM)
							continue;
						JSONObject jsono = new JSONObject();
						jsono.put("name", files[i].getName());
						jsono.put("hexname", Hex.encodeToString(files[i].getName().getBytes("utf-8")));
						jsono.put("path", Hex.encodeToString(rPath.getBytes("utf-8")));
						jsono.put("type", FilenameUtils.getExtension(files[i].getName()));
						jsono.put("size", files[i].isFile() ? files[i].length() : -1L);
						jsono.put("lastModified", formatDate(files[i].lastModified()));
						json.put(jsono);
					}
					json = sort(json);
					out.put("files", json);
				} catch (JSONException e) {
				}
				writer.write(out.toString());
				writer.close();
			} else {
				response.sendError(500);
			}
		} else {
			response.sendError(405); // not implemented
		}
	}

	/**
	 * @throws IOException
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 * 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		//
		VM selectedVM = getSelectedVM();
		if (selectedVM == null)
			response.sendError(500, "vBox异常，请重新登录");

		String uri = "smb://" + selectedVM.getVmIpAddress() + "/" + VBoxConfig.shareName + "/";
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", VBoxConfig.guestOSUserName,
				selectedVM.getVmGuestPassword());
		//
		logger.debug(uri);
		//
		if (!ServletFileUpload.isMultipartContent(request)) {
			try {
				request.setCharacterEncoding("utf-8");
				String fn = getFunction(request.getParameter("fn"));
				String rPath = getRelativePath(request.getParameter("rp"));
				String fName = getFileName(request.getParameter("f"));
				//
				logger.debug("POST request: fn=" + fn + ",rp=" + rPath + ",f=" + fName);

				SmbFile smbRepository = null;
				SmbFile smbPath = null;
				SmbFile smbFile = null;
				try {
					smbRepository = new SmbFile(uri, auth);
					smbPath = new SmbFile(smbRepository, rPath);
					if (fName != null)
						smbFile = new SmbFile(smbPath, fName);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				//
				if ("mkdir".equals(fn)) {
					logger.debug("mkdir");
					if (!smbFile.exists())
						smbFile.mkdir();
					else
						response.sendError(500);
				} else if ("del".equals(fn)) {
					logger.debug("delete");
					if (smbFile.exists())
						smbFile.delete();
					else
						response.sendError(500);
				} else
					response.sendError(405);
			} catch (SmbAuthException e) {
				try {
					response.sendError(401);
				} catch (IOException e1) {
					logger.error("error sending back response.", e1);
				}
			} catch (IOException e) {
				logger.error("error posting to uploadservlet.", e);
			}
		}
		//
		if (ServletFileUpload.isMultipartContent(request)) {
			logger.debug("Got POST multipart request.");
			JSONArray json = new JSONArray();
			PrintWriter writer = null;
			try {
				ServletFileUpload uploadHandler = new ServletFileUpload(new SmbFileItemFactory());
				uploadHandler.setHeaderEncoding("utf-8");
				String accepts = request.getHeader("Accept");
				if (accepts != null && accepts.indexOf("json") >= 0)
					response.setContentType("application/json");
				else
					response.setContentType("text/plain");
				//
				response.setCharacterEncoding("utf-8");
				writer = response.getWriter();
				List<FileItem> items = uploadHandler.parseRequest(request);
				String rPath = null;
				for (FileItem item : items) {
					if (item.isFormField()) {
						if ("rp".equals(item.getFieldName()))
							rPath = item.getString();
						//
						rPath = getRelativePath(rPath);
						logger.debug("Got rp=" + rPath);
					} else {
						if (rPath == null) {
							logger.error("no destination path received. Setting rp to /.");
							rPath = "";
						}
						String encodedFn = item.getName();
						if (encodedFn != null) {
							encodedFn = FilenameUtils.getName(encodedFn);
						}
						logger.debug("file name=" + encodedFn);
						//
						SmbFile smbRepository = null;
						SmbFile smbPath = null;
						SmbFile smbFile = null;
						try {
							smbRepository = new SmbFile(uri, auth);
							smbPath = new SmbFile(smbRepository, rPath);
							smbFile = new SmbFile(smbPath, encodedFn);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}

						logger.debug("Saving uploaded file to " + smbFile.getCanonicalPath());
						if (item instanceof SmbFileItem)
							((SmbFileItem) item).write(smbFile);
						logger.debug("Uploaded file saved to " + smbFile.getCanonicalPath());
						JSONObject jsono = new JSONObject();
						jsono.put("name", item.getName());
						jsono.put("hexname", Hex.encodeToString(item.getName().getBytes("utf-8")));
						jsono.put("path", Hex.encodeToString(rPath.getBytes("utf-8")));
						jsono.put("size", item.getSize());
						jsono.put("type", FilenameUtils.getExtension(item.getName()));
						jsono.put("lastModified", formatDate(smbFile.lastModified() == 0L ? System.currentTimeMillis()
								: smbFile.lastModified()));
						jsono.put("newfile", true);
						json.put(jsono);
					}
				}
			} catch (FileUploadException e) {
				logger.error("error uploading file.", e);
			} catch (SmbAuthException e) {
				try {
					response.sendError(401);
				} catch (IOException e1) {
					logger.error("error sending error back", e1);
				}
			} catch (Exception e) {
				logger.error("error uploading file", e);
			} finally {
				JSONObject out = new JSONObject();
				try {
					out.put("files", json);
				} catch (JSONException e) {
				}
				writer.write(out.toString());
				writer.close();
			}
		}
	}

	private VM getSelectedVM() {
		VM selected = (VM) SecurityUtils.getSubject().getSession().getAttribute("selected");
		if (selected != null) {
			SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
			VMMapper mapper = session.getMapper(VMMapper.class);
			selected = mapper.selectByPrimaryKey(selected.getVmId());
			session.close();
		}
		return selected;
	}

	@SuppressWarnings("unused")
	private String getMimeType(String fileName) {
		String mimetype = "";
		if (getSuffix(fileName).equalsIgnoreCase("png")) {
			mimetype = "image/png";
		} else if (getSuffix(fileName).equalsIgnoreCase("jpg")) {
			mimetype = "image/jpg";
		} else if (getSuffix(fileName).equalsIgnoreCase("jpeg")) {
			mimetype = "image/jpeg";
		} else if (getSuffix(fileName).equalsIgnoreCase("gif")) {
			mimetype = "image/gif";
		} else {
			javax.activation.MimetypesFileTypeMap mtMap = new javax.activation.MimetypesFileTypeMap();
			mimetype = mtMap.getContentType(fileName);
		}
		return mimetype;
	}

	private String getSuffix(String filename) {
		String suffix = "";
		int pos = filename.lastIndexOf('.');
		if (pos > 0 && pos < filename.length() - 1) {
			suffix = filename.substring(pos + 1);
		}
		return suffix;
	}

	private synchronized String formatDate(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		return sdf.format(new Date(timestamp));
	}

	public static String rfc5987_encode(final String s) throws UnsupportedEncodingException {
		final byte[] s_bytes = s.getBytes("UTF-8");
		final int len = s_bytes.length;
		final StringBuilder sb = new StringBuilder(len << 1);
		final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		final byte[] attr_char = { '!', '#', '$', '&', '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
				'U', 'V', 'W', 'X', 'Y', 'Z', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
				'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '|', '~' };
		for (int i = 0; i < len; ++i) {
			final byte b = s_bytes[i];
			if (Arrays.binarySearch(attr_char, b) >= 0)
				sb.append((char) b);
			else {
				sb.append('%');
				sb.append(digits[0x0f & (b >>> 4)]);
				sb.append(digits[b & 0x0f]);
			}
		}
		return sb.toString();
	}

	private JSONArray sort(JSONArray json) {
		try {
			List<JSONObject> list = new ArrayList<JSONObject>();
			for (int i = 0; i < json.length(); i++) {
				list.add(json.getJSONObject(i));
			}
			Collections.sort(list, new FileNameSorter());
			JSONArray resultArray = new JSONArray(list);
			return resultArray;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}