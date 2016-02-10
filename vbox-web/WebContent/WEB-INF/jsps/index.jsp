<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
	<jsp:attribute name="header">
<link rel="stylesheet" type="text/css" media="screen"
			href="css/theme-style.css">
<style type="text/css">
.jumbotron {
	background-image: url("imgs/jumbotron_bg.jpg"); 
	background-position: right;
	color: #333333; 
}
</style>
</jsp:attribute>
<jsp:attribute name="jscode">
<script language="JavaScript">
	var lang = navigator.languages? navigator.languages[0] : (navigator.language || navigator.userLanguage);
	if (lang.substring(0, 2) != "zh") {
		document.location.href = 'index-en.do';
	}
</script>
</jsp:attribute>
	<jsp:body>
		<c:if test="${ not empty requestScope.globalNotice }">
		<div class="row"><div class="col-lg-12"><div class="alert alert-dismissable alert-warning">
		<button type="button" class="close" data-dismiss="alert">×</button>
		<p><c:out value="${ requestScope.globalNotice }" /></p>
		</div></div></div>
		</c:if>
		<!-- Jumbotron -->
		<div class="jumbotron" style="height: 320px">
			<h1>你好！</h1>
			<p class="lead">欢迎使用vBox，在这里你可以随时随地连接到自己专有的科研桌面进行学习和工作。</p>
			<p><a class="btn btn-success" href="jaccount.do">开始使用 &raquo;</a></p>
			<h5><i class="icon-lock"></i>&nbsp;<em>vBox使用上海交大jAccount统一认证体系</em></h5>
		</div>
		<div class="block features">
      <h2 class="title-divider">
				<span>平台特点</span><small>领先的虚拟实验室云计算桌面</small>
			</h2>
      <div class="row">
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-4.png"
						alt="专属的云计算平台" class="img-responsive"></a>
          <h3 class="title">专属的云计算平台</h3>
          <p>只需几秒种就可以为您建立私人的计算空间，开箱即用预先安装好的软件，不再为繁琐的安装和配置烦恼。</p>
        </div>
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-2.png"
						alt="随时随地访问" class="img-responsive"></a>
          <h3 class="title">随时访问</h3>
          <p>计算平台24/7可供使用，平台智能分配资源，每次连接都可以直接恢复到上次退出的状态继续工作。</p> 
        </div>
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-3.png"
						alt="数据安全" class="img-responsive"></a>
          <h3 class="title">数据安全</h3>
          <p>提供海量的数据存储空间保存个人的数据，内置防病毒保护并且使用多重机制备份和冗余保证您的数据安全。</p>
        </div>
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-1.png"
						alt="易于使用" class="img-responsive"></a>
          <h3 class="title">易于使用</h3>
          <p>使用简洁现代的界面设计风格，支持移动终端如iPad等设备的访问，更加方便实现移动计算。</p>
        </div>
      </div>
    </div>
    </jsp:body>
</t:template>