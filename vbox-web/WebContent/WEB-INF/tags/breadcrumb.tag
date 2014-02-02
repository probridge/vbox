<%@tag import="java.util.ArrayList"%>
<%@tag import="java.util.StringTokenizer"%>
<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ attribute name="rp" fragment="false" required="true"%>
<%@ tag import="org.apache.shiro.codec.Hex"%>
<%
	ArrayList<String> displayName = new ArrayList<String>();
	ArrayList<String> url = new ArrayList<String>();

	String icon = null;
	if ("".equals(getJspContext().getAttribute("rp").toString())) {
		icon = "<i class=\"icon-reply-all\" style=\"font-size:25px\"></i>";
		url.add("home.do");
	} else {
		icon = "<i class=\"icon-home\" style=\"font-size:25px\"></i>";
		url.add("?rp=");
	}
	displayName.add(icon);

	String p = new String(Hex.decode((getJspContext()
			.getAttribute("rp").toString())), "utf-8");
	//
	int cur_index = 0;
	int prev_index = 0;
	while ((cur_index = p.indexOf("/", cur_index)) > 0) {
		String curPath = p.substring(0, cur_index);
		String curDispPath = p.substring(prev_index, cur_index);
		displayName.add(curDispPath);
		url.add("?rp="
				+ Hex.encodeToString((curPath + "/").getBytes("utf-8")));
		cur_index++;
		prev_index = cur_index;
	}
%>
<ul class="breadcrumb" style="margin-bottom: 5px;">
	<li><a href="<%=url.get(0)%>"><%=displayName.get(0)%></a></li>
	<%
		if (displayName.size() > 3) {
	%>
	<li>..</li>
	<%
		}
	%>
	<%
		for (int i = displayName.size() - 2; i < displayName.size(); i++) {
			if (i <= 0)
				continue;
	%>
	<li><a href="<%=url.get(i)%>"><%=displayName.get(i)%></a></li>
	<%
		}
	%>
</ul>
