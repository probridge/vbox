package com.probridge.vbox.zk;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbException;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.model.VMExample;
import com.probridge.vbox.vmm.RepositoryManager;

public class GoldenMasterImageManagementController extends SelectorComposer<Window> {
	private static final long serialVersionUID = -4592498413991042683L;

	@Wire
	Listbox lbImageList, lbImageFiles;

	@Wire
	Textbox tbNewImageDescription;

	@Wire
	Bandbox bdNewImageFileName;

	ListModelList<GMImage> imageList = new ListModelList<GMImage>();

	public ListModelList<GMImage> getImageList() {
		return imageList;
	}

	private ListModelList<String> imageFileList;

	private boolean hasMaint;

	public ListModelList<String> getImageFileList() {
		if (imageFileList == null) {
			imageFileList = new ListModelList<String>();
			//
			List<String> files = null;
			try {
				files = RepositoryManager.listRepository();
			} catch (SmbException | MalformedURLException e) {
				files = new ArrayList<String>();
				files.add("获取文件列表出错");
			}
			//
			imageFileList.addAll(files);
		}
		return imageFileList;
	}

	@Listen("onSelect = listbox#lbImageFiles")
	public void selectImage(Event e) {
		bdNewImageFileName.setValue((String) lbImageFiles.getModel().getElementAt(lbImageFiles.getSelectedIndex()));
		bdNewImageFileName.close();
	}

	public void reload() {
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
		} finally {
			session.close();
		}
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		reload();
	}

	@Listen("onClick = #btnDeleteImage")
	public void deleteImage(Event e) {
		if (lbImageList.getSelectedItem() == null)
			return;
		//
		Messagebox.show("确认要删除这个母盘吗？所选母盘文件及其所有副本也会完全被删除且无法恢复！", "确认", Messagebox.YES | Messagebox.NO,
				Messagebox.EXCLAMATION, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (!Messagebox.ON_YES.equals(event.getName()))
							return;
						//
						GMImage thisImage = (GMImage) lbImageList.getSelectedItem().getValue();
						// check VM reference
						SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
						VMMapper mp = session.getMapper(VMMapper.class);
						VMExample exp = new VMExample();
						exp.createCriteria().andVmVhdGmImageEqualTo(thisImage.getGmImageFilename())
								.andVmTypeNotEqualTo("2");
						int vmCount = mp.countByExample(exp);
						if (vmCount > 0) {
							Messagebox.show("还有" + vmCount + "个vBox正在使用这个母盘，不能删除");
							session.close();
							return;
						}
						RepositoryManager.deleteFile(thisImage.getGmImageFilename());
						//
						GMImageMapper mapper = session.getMapper(GMImageMapper.class);
						mapper.deleteByPrimaryKey(thisImage.getGmImageId());
						//
						session.commit();
						session.close();
						//
						reload();
					}
				});
	}

	@Listen("onClick = #btnReady")
	public void markReady(Event e) {
		Listitem thisListItem = lbImageList.getSelectedItem();
		if (thisListItem == null)
			return;
		GMImage thisImage = (GMImage) thisListItem.getValue();
		if ("0".equals(thisImage.getGmImageLock())) {
			Messagebox.show("本母盘已在就绪状态！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", thisImage);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/golden_master_maint.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnMaint")
	public void markMaint(Event e) {
		// check if there's any gm already maintaining
		Listitem thisListItem = lbImageList.getSelectedItem();
		if (thisListItem == null)
			return;
		GMImage thisImage = (GMImage) thisListItem.getValue();
		if ("1".equals(thisImage.getGmImageLock())) {
			Messagebox.show("本母盘已在维护状态！", "信息", Messagebox.OK, null);
			return;
		}
		if (hasMaint) {
			Messagebox.show("一次只能维护一个母盘！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", thisImage);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/golden_master_maint.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnDuplicateImage")
	public void duplicateImage(Event e) {
		Listitem thisListItem = lbImageList.getSelectedItem();
		if (thisListItem == null)
			return;
		GMImage thisImage = (GMImage) thisListItem.getValue();
		if ("1".equals(thisImage.getGmImageLock())) {
			Messagebox.show("只能克隆已经就绪的母盘！", "信息", Messagebox.OK, null);
			return;
		}
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("image", thisImage);
		arg.put("parentController", this);
		Window win = (Window) Executions.createComponents("/management/golden_master_duplicate.zul", getSelf(), arg);
		getSelf().appendChild(win);
	}

	@Listen("onClick = #btnAddImage")
	public void createImage(Event event) {
		for (GMImage eachExistingImage : imageList)
			if (eachExistingImage.getGmImageFilename().equalsIgnoreCase(bdNewImageFileName.getValue())) {
				Messagebox.show("该母盘已存在！", "信息", Messagebox.OK, null);
				return;
			}
		GMImage image = new GMImage();
		image.setGmImageFilename(bdNewImageFileName.getValue());
		image.setGmImageDescription(tbNewImageDescription.getValue());
		// TODO: check existance
		image.setGmImageLock("1");
		//
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		try {
			GMImageMapper mapper = session.getMapper(GMImageMapper.class);
			mapper.insertSelective(image);
			session.commit();
			//
		} finally {
			session.close();
		}
		reload();
	}
}
