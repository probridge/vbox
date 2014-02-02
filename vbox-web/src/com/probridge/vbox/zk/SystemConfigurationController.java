package com.probridge.vbox.zk;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.SysParamMapper;
import com.probridge.vbox.model.SysParam;
import com.probridge.vbox.model.SysParamExample;

public class SystemConfigurationController extends SelectorComposer<Div> {

	private static final long serialVersionUID = 1L;
	@Wire
	Rows CurrentSettings, ConfigSettings;

	@Override
	public void doAfterCompose(Div comp) throws Exception {
		super.doAfterCompose(comp);
		Field[] declaredFields = VBoxConfig.class.getDeclaredFields();
		for (Field field : declaredFields) {
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
				Row thisSetting = new Row();
				thisSetting.appendChild(new Label(field.getName()));
				thisSetting.appendChild(new Label(field.get(null).toString()));
				CurrentSettings.appendChild(thisSetting);
			}
		}
		//
		reload();
	}

	@Listen("onClick = #btnSaveSettings")
	public void saveSetting() {
		Messagebox.show("请确认是否需要保存，保存后需要请重新启动vBox应用程序生效。不正确的设置可能会导致系统无法正常运行！", "确认", Messagebox.YES | Messagebox.NO,
				Messagebox.QUESTION, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							saveSettings();
						}
					}
				});
	}

	@Listen("onClick = #btnRevertSettings")
	public void reload() {
		Iterator<Component> itrComp = ConfigSettings.getChildren().iterator();
		while (itrComp.hasNext())
			if (itrComp.next() != null)
				itrComp.remove();
		//
		SqlSession sess = VBoxConfig.sqlSessionFactory.openSession();
		SysParamMapper spMapper = sess.getMapper(SysParamMapper.class);
		List<SysParam> sysParmList = spMapper.selectByExample(new SysParamExample());
		sess.close();
		//
		for (SysParam eachParm : sysParmList) {
			Row thisRow = new Row();
			thisRow.appendChild(new Label(eachParm.getSysparamKey()));
			Textbox thisValueTb = new Textbox(eachParm.getSysparamValue());
			thisValueTb.setAttribute("key", eachParm.getSysparamKey());
			thisValueTb.setWidth("250px");
			thisRow.appendChild(thisValueTb);
			ConfigSettings.appendChild(thisRow);
		}
	}

	//
	protected void saveSettings() {
		SqlSession sess = VBoxConfig.sqlSessionFactory.openSession();
		try {
			SysParamMapper spMapper = sess.getMapper(SysParamMapper.class);
			List<Component> eachConfigRowList = ConfigSettings.getChildren();
			for (Component eachRow : eachConfigRowList) {
				if (eachRow.getChildren() != null)
					if (eachRow.getChildren().get(1) instanceof Textbox) {
						Textbox thisTb = (Textbox) (eachRow.getChildren().get(1));
						SysParam thisParm = new SysParam();
						thisParm.setSysparamKey(thisTb.getAttribute("key").toString());
						thisParm.setSysparamValue(thisTb.getValue());
						spMapper.updateByPrimaryKey(thisParm);
						// Global Notice Save
						if ("globalNotice".equals(thisParm.getSysparamKey()))
							VBoxConfig.globalNotice = thisParm.getSysparamValue();
					}
			}
			sess.commit();
		} catch (Exception e) {
			Messagebox.show("保存出错！" + e.getMessage());
			sess.rollback();
		} finally {
			sess.close();
		}
	}
}