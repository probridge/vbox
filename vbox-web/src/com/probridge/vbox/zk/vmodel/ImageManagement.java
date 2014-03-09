package com.probridge.vbox.zk.vmodel;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbException;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.vmm.RepositoryManager;
import com.probridge.vbox.vmm.wmi.HyperVVMM;

public class ImageManagement {

	private GMImage selectedImage;
	private List<GMImage> imageList = new ArrayList<GMImage>();

	private String selectedFile;
	private String newDescription;
	private List<String> imageFileList = new ArrayList<String>();

	private boolean hasMaint;

	@WireVariable
	private Page _page;

	@Init
	public void init() {
		reloadImageList();
		loadImageFileList();
	}

	public void loadImageFileList() {
		try {
			imageFileList = RepositoryManager.listRepository();
		} catch (SmbException | MalformedURLException e) {
			imageFileList.add("获取文件列表出错");
		}
	}

	@GlobalCommand
	@NotifyChange({ "imageList", "selectedImage" })
	public void reloadImageList() {
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			GMImageMapper mapper = session.getMapper(GMImageMapper.class);
			GMImageExample exp = new GMImageExample();
			exp.createCriteria();
			imageList.clear();
			imageList.addAll(mapper.selectByExample(exp));
			//
			exp = new GMImageExample();
			exp.createCriteria().andGmImageLockEqualTo("1");
			hasMaint = mapper.selectByExample(exp).size() > 0;
			//
			for (GMImage eachImage : imageList)
				if (selectedImage != null && eachImage.getGmImageId() == selectedImage.getGmImageId())
					selectedImage = eachImage;
			//
		} finally {
			session.close();
		}
	}
	
	@Command
	public void deleteImage() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", selectedImage);
		Window win = (Window) Executions.createComponents("/management/golden_master_delete_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void readyImage() {
		if ("0".equals(selectedImage.getGmImageLock())) {
			Messagebox.show("本母盘已在就绪状态！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", selectedImage);
		Window win = (Window) Executions.createComponents("/management/golden_master_maint_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void maintImage() {
		// check if there's any gm already maintaining
		if ("1".equals(selectedImage.getGmImageLock())) {
			Messagebox.show("本母盘已在维护状态！", "信息", Messagebox.OK, null);
			return;
		}
		if (hasMaint) {
			Messagebox.show("一次只能维护一个母盘！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", selectedImage);
		Window win = (Window) Executions.createComponents("/management/golden_master_maint_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void duplicateImage() {
		if ("1".equals(selectedImage.getGmImageLock())) {
			Messagebox.show("只能克隆已经就绪的母盘！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", selectedImage);
		Window win = (Window) Executions.createComponents("/management/golden_master_duplicate_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	public void syncImage() {
		if (!"2".equals(selectedImage.getGmImageLock())) {
			Messagebox.show("母盘必须为未同步状态！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", selectedImage);
		Window win = (Window) Executions.createComponents("/management/golden_master_maint_new.zul", null, arg);
		win.setPage(_page);
	}

	@Command
	@NotifyChange({ "imageList", "selectedImage", "selectedFile", "newDescription" })
	public void saveImage() {
		GMImage image = new GMImage();
		image.setGmImageFilename(selectedFile);
		image.setGmImageDescription(newDescription);
		if (HyperVVMM.hypervisors.length > 1)
			image.setGmImageLock("2");
		else
			image.setGmImageLock("0");
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			GMImageMapper mapper = session.getMapper(GMImageMapper.class);
			GMImageExample exp = new GMImageExample();
			exp.createCriteria().andGmImageFilenameEqualTo(selectedFile);
			List<GMImage> existing = mapper.selectByExample(exp);
			if (existing == null || existing.size() == 0) {
				mapper.insertSelective(image);
			} else {
				GMImage toUpdate = existing.get(0);
				toUpdate.setGmImageDescription(newDescription);
				mapper.updateByPrimaryKey(toUpdate);
			}
			session.commit();
		} finally {
			session.close();
		}
		newDescription = null;
		selectedFile = null;
		selectedImage = null;
		reloadImageList();
	}

	@Command
	@NotifyChange({ "selectedFile", "newDescription" })
	public void reload() {
		newDescription = "请输入[" + selectedFile + "]的描述信息";
	}

	@Command
	@NotifyChange({ "imageList", "selectedImage" })
	public void refresh() {
		reloadImageList();
	}

	public GMImage getSelectedImage() {
		return selectedImage;
	}

	public void setSelectedImage(GMImage selectedImage) {
		this.selectedImage = selectedImage;
	}

	public List<GMImage> getImageList() {
		return imageList;
	}

	public void setImageList(List<GMImage> imageList) {
		this.imageList = imageList;
	}

	public String getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
	}

	public String getNewDescription() {
		return newDescription;
	}

	public void setNewDescription(String newDescription) {
		this.newDescription = newDescription;
	}

	public List<String> getImageFileList() {
		return imageFileList;
	}

	public void setImageFileList(List<String> imageFileList) {
		this.imageFileList = imageFileList;
	}

	public boolean isHasMaint() {
		return hasMaint;
	}

	public void setHasMaint(boolean hasMaint) {
		this.hasMaint = hasMaint;
	}
}
