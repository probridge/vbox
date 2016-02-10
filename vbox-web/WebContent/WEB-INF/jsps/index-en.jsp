<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template_en>
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
	<jsp:body>
		<c:if test="${ not empty requestScope.globalNotice }">
		<div class="row"><div class="col-lg-12"><div class="alert alert-dismissable alert-warning">
		<button type="button" class="close" data-dismiss="alert">×</button>
		<p><c:out value="${ requestScope.globalNotice }" /></p>
		</div></div></div>
		</c:if>
		<!-- Jumbotron -->
		<div class="jumbotron" style="height: 320px">
			<h1>Hello!</h1>
			<p class="lead">Welcome to use vBox. You may connect to your exclusive research desk to study and work anytime anywhere.</p>
			<p><a class="btn btn-success" href="jaccount.do">Get started &raquo;</a></p>
			<h5><i class="icon-lock"></i>&nbsp;<em>Use jAccount SSO to login</em></h5>
		</div>
		<div class="block features">
      <h2 class="title-divider">
				<span>Platform features</span><small>Leading cloud computing table in virtual lab</small>
			</h2>
      <div class="row">
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-4.png"
						alt="专属的云计算平台" class="img-responsive"></a>
          <h3 class="title">Exclusive cloud computing platform</h3>
          <p>Establish your personal computing space in a few seconds, with the pre-installed softs. No longer bothered by the fussy installing and configuration.</p>
        </div>
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-2.png"
						alt="随时随地访问" class="img-responsive"></a>
          <h3 class="title">Visit at any time</h3>
          <p>7/24 accessible, source allocated intelligently and table last-used recovered automatically.</p> 
        </div>
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-3.png"
						alt="数据安全" class="img-responsive"></a>
          <h3 class="title">Data Secure</h3>
          <p>Provide massive data storage space to store personal data, with Virus protection, multi-mechanism backup and redundancy to insure your data</p>
        </div>
        <div class="feature col-sm-6 col-md-3"> <a href="#"
						style="cursor: default;"><img src="imgs/feature-1.png"
						alt="易于使用" class="img-responsive"></a>
          <h3 class="title">Easy to use</h3>
          <p>Base on brief and modern style, and support terminals such as mobiles and tablets.</p>
        </div>
      </div>
    </div>
    </jsp:body>
</t:template_en>