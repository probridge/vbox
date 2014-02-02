package com.probridge.vbox.vmm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import org.jinterop.dcom.common.JIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.servlet.OpStatus;
import com.probridge.vbox.vmm.wmi.HyperVVMM;
import com.probridge.vbox.vmm.wmi.WindowsManagementServiceLocator;
import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;

public class RepositoryManager {
	//
	private static final Logger logger = LoggerFactory.getLogger(RepositoryManager.class);

	public static ArrayList<String> listRepository() throws SmbException, MalformedURLException {
		String uri = "smb://" + HyperVVMM.hypervisors[VBoxConfig.repositoryLocation].url + "/"
				+ VBoxConfig.repositoryShareName + "/";
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", VBoxConfig.hypervisorUser,
				VBoxConfig.hypervisorPwd);
		SmbFile smbRepository = new SmbFile(uri, auth);
		SmbFile[] files = smbRepository.listFiles(VBoxConfig.fileFilter);
		ArrayList<String> goldenMasterList = new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isHidden() || (files[i].getAttributes() & SmbFile.ATTR_SYSTEM) == SmbFile.ATTR_SYSTEM)
				continue;
			goldenMasterList.add(files[i].getName());
		}
		return goldenMasterList;
	}

	public static void clone(String srcFn, String destFn) throws JIException, VirtualServiceException,
			UnknownHostException {
		logger.info("Cloning repository file: " + srcFn + " ==> " + destFn);
		WindowsManagementServiceLocator wmServiceLocator = new WindowsManagementServiceLocator(
				HyperVVMM.hypervisors[VBoxConfig.repositoryLocation].url);
		wmServiceLocator.copyFile(VBoxConfig.dataDrive, VBoxConfig.goldenMasterImageDirectory,
				srcFn.substring(0, srcFn.lastIndexOf(".")), "vhd", destFn);
		wmServiceLocator.destroySession();
		logger.info("File cloned!");
	}

	public static void deleteFile(String fileName) {
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", VBoxConfig.hypervisorUser,
				VBoxConfig.hypervisorPwd);
		logger.info("Start to deleting [" + fileName + "] on all hypervisors.");
		for (int i = 0; i < HyperVVMM.hypervisors.length; i++) {
			//
			String targetUri = "smb://" + HyperVVMM.hypervisors[i].url + "/" + VBoxConfig.repositoryShareName + "/"
					+ fileName;
			//
			try {
				logger.info("Deleting file on hypervisor " + HyperVVMM.hypervisors[i].hypervisorName + "[" + i + "]...");
				SmbFile targetFile = new SmbFile(targetUri, auth);
				targetFile.delete();
				logger.info("File deleted.");
			} catch (IOException e) {
				logger.error("Failed while deleting " + fileName + " on hypervisor "
						+ HyperVVMM.hypervisors[i].hypervisorName + "[" + i + "]...", e);
			}
		}
	}

	public static void syncFile(String fileName, OpStatus ops) {
		String uri = "smb://" + HyperVVMM.hypervisors[VBoxConfig.repositoryLocation].url + "/"
				+ VBoxConfig.repositoryShareName + "/" + fileName;
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", VBoxConfig.hypervisorUser,
				VBoxConfig.hypervisorPwd);
		logger.info("Synchronizing file " + fileName);
		SmbFile sourceFile = null;
		long size = -1L;
		try {
			sourceFile = new SmbFile(uri, auth);
			size = sourceFile.length();
		} catch (IOException e) {
			logger.error("Error getting source file ", e);
		}
		logger.info("File length=" + size);
		//
		String baseMsg = ops.getMsg();
		//
		for (int i = 0; i < HyperVVMM.hypervisors.length; i++) {
			// skip source
			if (i == VBoxConfig.repositoryLocation)
				continue;
			//
			String msg = baseMsg + " [" + i + "/" + (HyperVVMM.hypervisors.length - 1) + "] - ";
			//
			String targetUri = "smb://" + HyperVVMM.hypervisors[i].url + "/" + VBoxConfig.repositoryShareName + "/"
					+ fileName;
			//
			SmbFileInputStream in = null;
			SmbFileOutputStream out = null;
			try {
				SmbFile targetFile = new SmbFile(targetUri, auth);
				//
				in = new SmbFileInputStream(sourceFile);
				out = new SmbFileOutputStream(targetFile);

				logger.info("Sychronizing file to hypervisor " + HyperVVMM.hypervisors[i].hypervisorName + "[" + i
						+ "]...");

				/*
				 * final ReadableByteChannel inputChannel =
				 * Channels.newChannel(in); final WritableByteChannel
				 * outputChannel = Channels.newChannel(out);
				 * fastChannelCopy(inputChannel, outputChannel, size, msg, ops);
				 * inputChannel.close(); outputChannel.close();
				 */

				byte[] buffer = new byte[0xFFFF - 70];
				int read = 0, count = 0;
				long bTotal = 0;
				DecimalFormat df = new DecimalFormat("#0.0");
				while ((read = in.read(buffer)) > 0) {
					out.write(buffer, 0, read);
					bTotal += read;
					count++;
					if (count > 16) {
						ops.setMsg(msg + df.format(bTotal * 100.0d / size) + "%");
						count = 0;
					}
				}
				in.close();
				out.close();

				logger.info("File sychronized.");
			} catch (IOException e) {
				logger.error("Failed while syncing to hypervisor " + HyperVVMM.hypervisors[i].hypervisorName + "[" + i
						+ "]...", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// ignore
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}
}