<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<%@ attribute name="header" fragment="true" required="false"%>
<%@ attribute name="jscode" fragment="true" required="false"%>
<!DOCTYPE html>
<html>
<head>
<title>vBox</title>
<!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="apple-mobile-web-app-capable" content="yes">
<link rel="icon" href="./favicon.ico" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<!-- Bootstrap -->
<link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
<link rel="stylesheet" href="css/font-awesome.min.css">
<link rel="stylesheet" href="css/bootswatch.min.css">
<link href="css/jquery.pnotify.default.css" media="all" rel="stylesheet" type="text/css" />
<!-- Customize -->
<link rel="stylesheet" href="css/footer.css">
<link rel="stylesheet" href="css/chinese.css">
<jsp:invoke fragment="header" />
<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="js/html5shiv.js"></script>
      <script src="js/respond.min.js"></script>
<![endif]-->
<script src="js/modernizr.custom.44054.js"></script>
</head>
<body>
	<div class="container" id="wrap">
		<div class="navbar navbar-default">
			<div class="container">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse"
						data-target=".navbar-responsive-collapse">
						<span class="icon-bar"></span> <span class="icon-bar"></span> <span
							class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="./" style="padding-top: 5px; padding-bottom: 0px"><img src="imgs/vbox_logo.png" title="${ requestScope.version }"></a>
				</div>
				<div class="navbar-collapse collapse navbar-responsive-collapse">
					<ul class="nav navbar-nav">
						<li><a href="home.do">工作空间</a></li>
						<li><a href="#">使用方法</a></li>
						<li><a href="#">联系我们</a></li>
						<li><a href="#">关于</a></li>
					</ul>
					<shiro:authenticated>
						<ul class="nav navbar-nav navbar-right">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> <shiro:principal /><b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="logout.do"><i class="icon-unlock"></i>&nbsp;安全退出</a></li>
								</ul></li>
						</ul>
					</shiro:authenticated>
					<shiro:notAuthenticated>
						<ul class="nav navbar-nav navbar-right">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> 登录<b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="jaccount.do"><i class="icon-lock"></i>&nbsp;jAccount统一认证</a></li>
									<li><a href="login.do?other=1"><i class="icon-lock"></i>&nbsp;其他用户</a></li>
								</ul></li>
						</ul>
					</shiro:notAuthenticated>
				</div>
				<!-- /.nav-collapse -->
			</div>
			<!-- /.container -->
		</div>
		<!-- /.navbar -->
		<jsp:doBody />
	</div>
	<div id="divTimeout" style="display: none; margin-bottom: 0px;" class="alert alert-warning">
	<h4>您很久没有运动了</h4>
	请点击鼠标继续操作，为了您的安全我们将<label id="lbTimeRemain">在一段时间后</label>退出您的会话
	</div>
	<!-- /container -->	
<c:if test="${ not empty requestScope.wofd }">
	<div class="container">
		<div id="footer-wofd" class="hide">
		<blockquote class="pull-right" style="border-right: 5px solid #8A8A8A;">
			<p>${ requestScope.wofd }</p>
			<small>${ requestScope.wofdname }</small>
		</blockquote>
		</div>
	</div>
</c:if>
	<!-- /container -->
	<div class="container">
		<div id="footer" class="row">
			<div id="footer-content">
				上海交大安泰经管学院实验中心
			</div>
		</div>
	</div>
	<!-- /container -->	
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
	<script src="js/jquery.js"></script>
	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="js/bootstrap.min.js"></script>
	<script src="js/jquery.blockUI.js"></script>
	<script src="js/holder.js"></script>
	<script type="text/javascript" src="js/jquery.pnotify.min.js"></script>
	<script type="text/javascript">
	$.pnotify.defaults.history = false;
	//
	if (!Modernizr.canvas)
		complain_browser();
	//
	var stack_bar_bottom = {"dir1": "up", "dir2": "right", "spacing1": 0, "spacing2": 0};
	function complain_browser() {
		var opts = {
			title: "<b>您的浏览器太久没有更新了</b>",
			text: "要正常使用vBox, 我们强烈建议您将浏览器升级到更加快速、安全的新版本，如Internet Explorer 9或者Chrome 29以上版本！<a href='download/ChromeStandaloneSetup.exe' class='pull-right'>点击下载Chrome浏览器</a>",
			type: "error",
			hide: false,
			addclass: "stack-bar-bottom alert-danger",
			cornerclass: "",
			width: "100%",
			stack: stack_bar_bottom
		};
		$.pnotify(opts);
	}
	</script>
<shiro:authenticated>
<script type="text/javascript">
	var idleTime = 0;
	var valWarning = 15;
	var valLogout = 20;
	var stopTimer = false;
	$.blockUI.defaults.overlayCSS = { 
	        backgroundColor: '#FFFFFF', 
	        opacity:         0.8, 
	        cursor:          'wait'
	};
	//
	$.blockUI.defaults.css.border = '0px'; 
	$(document).ready(function() {
	    //Increment the idle time counter every minute.
	    setInterval("timerIncrement()", 60000);
	    //Zero the idle timer on mouse movement.
	    $(this).on('click touchstart keypress',function() {
			idleTime = 0;
			$.unblockUI();
	    });
	});
	//
	function timerIncrement() {
	    if (stopTimer) {
			idleTime=0;
	    	return;
		}
	    idleTime += 1;
	    if (idleTime >= valWarning) {
			if (idleTime == valWarning)
				$.blockUI({ message : $('#divTimeout') });
			var strRemain = valLogout - idleTime;
			if (strRemain <=0 )
			    strRemain = "立即";
			else
			    strRemain = "在" + strRemain + "分钟后";
			//
			$("#lbTimeRemain").text(strRemain);
		}
	    if (idleTime >= valLogout) {
			window.location="logout.do";
	    }
	}	
</script>
</shiro:authenticated>
	<jsp:invoke fragment="jscode" />
</body>
</html>
