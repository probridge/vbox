<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
	<jsp:attribute name="header">
<script type="text/javascript">
    function connect() {
	console.log("connecting state=" + MsRdpClient.readyState);
	document.getElementById("screenArea").style.display = "block";
	MsRdpClient.Server = "${requestScope.server}";
	MsRdpClient.UserName = "${requestScope.username}";
	MsRdpClient.AdvancedSettings2.ClearTextpassword = "${requestScope.password}";
	<c:choose>
	<c:when test="${'1'==param.fullscreen}">
	MsRdpClient.FullScreen = true;
	MsRdpClient.DesktopWidth = screen.width;
	MsRdpClient.DesktopHeight = screen.height;
	MsRdpClient.Width = screen.width;
	MsRdpClient.Height = screen.height;
	</c:when>
	<c:otherwise>
	MsRdpClient.FullScreen = false;
	</c:otherwise>
	</c:choose>
	MsRdpClient.FullScreenTitle = "vBox Connection";
	MsRdpClient.ConnectedStatusText = "Stay Connected";
	MsRdpClient.ColorDepth = 24;
	MsRdpClient.AdvancedSettings2.PerformanceFlags = 0x190;
	MsRdpClient.AdvancedSettings2.RedirectDrives = false;
	MsRdpClient.AdvancedSettings2.RedirectPrinters = false;
	MsRdpClient.AdvancedSettings2.RedirectClipboard = true;
	MsRdpClient.AdvancedSettings2.RedirectSmartCards = false;
	MsRdpClient.AdvancedSettings2.RedirectDirectX = false;
	MsRdpClient.AdvancedSettings2.MinutesToIdleTimeout = 1;
	MsRdpClient.Connect();
    }

    function onConn() {
	console.log("onconnect");
	stopTimer = true;
    }

    function onDisc(disconnectCode) {
	console.log("ondisc " + disconnectCode);
	stopTimer = false;
	window.location = 'home.do';
    }

    function onLeaveFS() {
	console.log("on leave full screen");
	window.location = 'viewbox.do?uuid=${param.uuid}&fullscreen=0';
    }

    function onLogonError() {
	console.log("on logon error");
    }

    function onIdleTimeout() {
	console.log("inactivity timeout");
    }

    function onControlLoad() {
	console.log("on control load");
	if (typeof (MsRdpClient) === 'undefined' || MsRdpClient === null) {
	    console.log('Unable to load ActiveX');
	} else {
	    if (MsRdpClient.readyState == 4) {
		console.log("control ready");
	    }
	    // attach events
	    MsRdpClient.attachEvent('OnConnected', onConn);
	    MsRdpClient.attachEvent('OnDisconnected', onDisc);
	    MsRdpClient.attachEvent('OnLeaveFullScreenMode', onLeaveFS);
	    MsRdpClient.attachEvent('OnLogonError', onLogonError);
	    MsRdpClient.attachEvent('OnIdleTimeoutNotification', onIdleTimeout);
	}
    }

    function onControlLoadError() {
	console.log("onLoadError");
    }
</script>
</jsp:attribute>
	<jsp:attribute name="jscode">
<script type="text/javascript">
    var resizeTimer;
    var scheduleReconnect = false;
    $(document).ready(function() {
	$('.tooltips').tooltip();
	//
	console.log("document ready");
	console.log('undefined' == typeof MsRdpClient.readyState);
	if ('undefined' == typeof MsRdpClient.readyState) {
	    $("#browserNotSupport").removeClass("hide");
	    window.location = 'viewbox2.do?uuid=${param.uuid}';
	} else {
	    $(window).bind("resize", onResize);
	    doResize();
	    setTimeout(connect, 500);
	}
	keepalive();
    });

    function onResize() {
	console.log("onresize");
	clearTimeout(resizeTimer);
	resizeTimer = setTimeout(doResize, 100);
    }

    function doResize() {
	console.log("doresize");
	$(window).unbind("resize", onResize);
	var h = ($(window).height()) - 127;
	if ($('#screenArea').height() != h) {
	    $('#screenArea').height(h).resize();
	}
	$(window).bind("resize", onResize);
    }

    function keepalive() {
	$.ajax({
	    url : 'vmm-servlet',
	    type : 'POST',
	    dataType : 'json',
	    data : {
		fn : 'ping'
	    }
	});
	setTimeout(keepalive, 60000);
    }

    function fullscreen_ie() {
	window.location = 'viewbox.do?uuid=${param.uuid}&fullscreen=1';
    }

    function html5client() {
	window.location = 'viewbox2.do?uuid=${param.uuid}';
    }
</script>
</jsp:attribute>
	<jsp:body>
<div class="row hide" id="browserNotSupport">
<div class="col-lg-12">
	<div class="alert alert-dismissable alert-warning" id="alert-warning">
		<button type="button" class="close"
						onclick="$(this).parent().addClass('hide');">×</button>
		<p class="text-center">抱歉，目前vBox的访问只支持使用Internet Explorer浏览器，更多浏览器支持敬请期待！</p>
	</div>
</div>
</div>
<div class="row">
<div class="col-lg-6">
<span style="cursor: pointer;" onclick="window.location='home.do'">
<i class="icon-arrow-left"></i>&nbsp;返回&nbsp;</span>
<span style="cursor: pointer;" onclick="connect()"><i
					class="icon-play"></i>&nbsp;开始&nbsp;</span>
</div>
<div class="col-lg-6" align="right">
<span class="tooltips" style="cursor: pointer;" onclick="html5client()" data-toggle="tooltip" title="" data-original-title="若无法连接请切换至外网模式" data-placement="top"><i
					class="icon-external-link"></i>&nbsp;外网访问</span>
<span style="cursor: pointer;" onclick="fullscreen_ie()"><i
					class="icon-fullscreen"></i>&nbsp;全屏</span>
</div>
</div>
<div class="row">
<div class="col-lg-12" id="screenArea" style="display: none;">
<object id="MsRdpClient"
					classid="CLSID:4eb89ff4-7f78-4a0f-8b8d-2bf02e94e4b2" width="100%"
					height="100%" onreadystatechange="onControlLoad();"
					onerror="onControlLoadError();"></object>
</div>
</div>
</jsp:body>
</t:template>