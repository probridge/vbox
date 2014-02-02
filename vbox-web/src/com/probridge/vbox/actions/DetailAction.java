package com.probridge.vbox.actions;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.dao.CourseMapper;
import com.probridge.vbox.dao.GMImageMapper;
import com.probridge.vbox.dao.VMMapper;
import com.probridge.vbox.model.Course;
import com.probridge.vbox.model.CourseExample;
import com.probridge.vbox.model.GMImage;
import com.probridge.vbox.model.GMImageExample;
import com.probridge.vbox.model.VM;
import com.probridge.vbox.utils.Utility;
import com.probridge.vbox.zk.converter.VboxHeartbeatConverter;
import com.probridge.vbox.zk.converter.VboxNetworkTypeConverter;
import com.probridge.vbox.zk.converter.VboxPersistanceConverter;
import com.probridge.vbox.zk.converter.VboxStatusConverter;

public class DetailAction implements Action {
	public static final String SUCCEED = "detail";

	public String execute(HttpServletRequest request, HttpServletResponse response) {
		String uuid = request.getParameter("uuid");
		SqlSession session = VBoxConfig.sqlSessionFactory.openSession();
		VMMapper mapper = session.getMapper(VMMapper.class);
		GMImageMapper gmMapper = session.getMapper(GMImageMapper.class);
		CourseMapper cMapper = session.getMapper(CourseMapper.class);
		VM thisVbox = mapper.selectByPrimaryKey(uuid);
		//
		//
		if (thisVbox != null) {
			String gmDesc = "";
			String courseDesc = "";

			GMImageExample exp = new GMImageExample();
			exp.or().andGmImageFilenameEqualTo(thisVbox.getVmVhdGmImage());
			List<GMImage> gmImage = gmMapper.selectByExample(exp);

			if (!Utility.isEmptyOrNull(thisVbox.getVmCourseCode())) {
				CourseExample exp2 = new CourseExample();
				exp2.or().andCourseIdEqualTo(thisVbox.getVmCourseCode());
				List<Course> course = cMapper.selectByExample(exp2);
				if (course.size() >= 1)
					courseDesc = course.get(0).getCourseName() + "：" + course.get(0).getCourseDescription();
			}

			if (gmImage.size() >= 1)
				gmDesc = gmImage.get(0).getGmImageDescription();
			//
			//
			request.setAttribute("vmId", thisVbox.getVmId());
			request.setAttribute("vmName", thisVbox.getVmName());
			request.setAttribute("vmTitle", thisVbox.getVmTitle());
			request.setAttribute("vmDescription", thisVbox.getVmDescription());
			request.setAttribute("vmCourseCode", courseDesc);
			request.setAttribute("vmGMInfo", gmDesc);
			//
			request.setAttribute("vmCores", thisVbox.getVmCores() + "核");
			request.setAttribute("vmMemory", thisVbox.getVmMemory() + "MB");
			request.setAttribute("vmNetwork",
					(new VboxNetworkTypeConverter().coerceToUi(thisVbox.getVmNetworkType(), null)));
			request.setAttribute("vmPersistance",
					(new VboxPersistanceConverter().coerceToUi(thisVbox.getVmPersistance(), null)));
			//
			request.setAttribute("vmStatus", (new VboxStatusConverter().coerceToUi(thisVbox.getVmStatus(), null)));
			request.setAttribute("vmHeartbeat",
					(new VboxHeartbeatConverter().coerceToUi(thisVbox.getVmHeartbeat(), null)));
			request.setAttribute("vmIpAddress", thisVbox.getVmIpAddress());
			request.setAttribute("vmGuestPassword", Utility.isEmptyOrNull(thisVbox.getVmGuestPassword()) ? "不可用" : "就绪");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			request.setAttribute("vmLastUpdateTimestamp", sdf.format(thisVbox.getVmLastUpdateTimestamp()));
			//
		}
		session.close();
		return SUCCEED;
	}
}
