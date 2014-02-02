<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
	<jsp:attribute name="header">
<style type="text/css">
.guac-hide-cursor {
	cursor: url('imgs/dot.gif'), default;
}
</style>
</jsp:attribute>
	<jsp:attribute name="jscode">
	<!-- Input abstractions -->
	<script type="text/javascript" src="js/guacamole-common-js/keyboard.js"></script>
	<script type="text/javascript" src="js/guacamole-common-js/mouse.js"></script>
	<!-- Client core scripts -->
	<script type="text/javascript" src="js/guacamole-common-js/layer.js"></script>
	<script type="text/javascript" src="js/guacamole-common-js/tunnel.js"></script>
	<script type="text/javascript" src="js/guacamole-common-js/guacamole.js"></script>
	<!-- Init -->
	<script type="text/javascript">
	<c:choose>
	<c:when test="${'1'==param.fullscreen}">
	$('.navbar').remove();
	$('#footer').remove();
	$('.container').removeClass();
	$('.row').css('margin','0px');
	$('#wrap').css('padding', '0px').css('margin','0px');	
	var optimal_width = $("#display").width();
	var optimal_height = ($(window).height()) - 33;	
	</c:when>
	<c:otherwise>
	var optimal_width = $("#display").width();
	var optimal_height = ($(window).height()) - 127;
	</c:otherwise>
	</c:choose>
	// Get display div from document
	var display = document.getElementById("display");

	// Instantiate client, using an HTTP tunnel for communications.
	var guac = new Guacamole.Client(new Guacamole.HTTPTunnel("tunnel"));

	// Add client to display div
	display.appendChild(guac.getDisplay());

	// Error handler
	guac.onerror = function(error) {
	    // alert(error);
	    window.location = "home.do";
	};


	// Get entire query string, and pass to connect().
	// Normally, only the "id" parameter is required, but
	// all parameters should be preserved and passed on for
	// the sake of authentication.

	var connect_string = "width=" + optimal_width + "&height=" + optimal_height;

	// Connect
	guac.connect(connect_string);

	// Disconnect on close
	window.onunload = function() {
	    guac.disconnect();
	}

	// Mouse
	var mouse = new Guacamole.Mouse(guac.getDisplay());

	mouse.onmousedown = mouse.onmouseup = mouse.onmousemove = function(mouseState) {
	    guac.sendMouseState(mouseState);
	};

	var touch = new Guacamole.Mouse.Touchscreen(guac.getDisplay());

	touch.onmousedown =	touch.onmousemove =	touch.onmouseup   = function(touchState) {
	    guac.sendMouseState(touchState);
	};
	
	// Keyboard
	var keyboard = new Guacamole.Keyboard(document);

	keyboard.onkeydown = function(keysym) {
	    guac.sendKeyEvent(1, keysym);
	};

	keyboard.onkeyup = function(keysym) {
	    guac.sendKeyEvent(0, keysym);
	};

	function fullscreen() {
	    var element = document.body; // Make the body go full screen.
	    var requestMethod = element.requestFullScreen || element.webkitRequestFullScreen || element.mozRequestFullScreen || element.msRequestFullScreen;

	    if (requestMethod) { // Native full screen.
	        requestMethod.call(element);
	    } else if (typeof window.ActiveXObject !== "undefined") { // Older IE.
	        var wscript = new ActiveXObject("WScript.Shell");
	        if (wscript !== null) {
	            wscript.SendKeys("{F11}");
	        }
	    }
	}
	
	function enlarge() {
	    window.location = 'viewbox2.do?uuid=${param.uuid}&fullscreen=1';
	}
	
	function shrink() {
	    window.location = 'viewbox2.do?uuid=${param.uuid}';
	}
	
	function rdpclient() {
		window.location = 'viewbox.do?uuid=${param.uuid}';
	}
	//
	$(document).ready(function() {
		$('.tooltips').tooltip();
	});
    </script>
</jsp:attribute>
<jsp:body>
<div class="row">
<div class="col-lg-6 col-md-6 col-sm-6 col-xs-6">
<span style="cursor: pointer;" onclick="window.location='home.do'">
<i class="icon-arrow-left"></i>&nbsp;返回&nbsp;</span>
<span style="cursor: pointer;" onclick="connect()"><i
					class="icon-play"></i>&nbsp;开始&nbsp;</span>
</div>
<div class="col-lg-6 col-md-6 col-sm-6 col-xs-6" align="right">
<span class="tooltips" style="cursor: pointer;" onclick="rdpclient()" data-toggle="tooltip" title="" data-original-title="中院内网可使用IE加速直连" data-placement="top"><i
					class="icon-external-link"></i>&nbsp;内网访问</span>
<c:choose>
<c:when test="${'1'==param.fullscreen}">
<span style="cursor: pointer;" onclick="shrink()"><i
					class="icon-resize-small"></i>&nbsp;缩小</span>
</c:when>
<c:otherwise>
<span style="cursor: pointer;" onclick="enlarge()"><i
					class="icon-fullscreen"></i>&nbsp;放大</span>
</c:otherwise>
</c:choose>
</div>
</div>
<div class="row">
<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
<div id="display" width="100%"></div>
</div> 
</div>
</jsp:body>
</t:template>