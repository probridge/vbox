<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
<jsp:attribute name="header">
<link rel="stylesheet" href="css/jquery-als.css">
<style type="text/css">
.carousel-control.right {
	background-image: none;
}

.carousel-control.left {
	background-image: none;
}

.icon-next {
	color: #446e9b;
}

.icon-prev {
	color: #446e9b;
}

.carousel-indicators li {
	border: 1px solid #446e9b;
}

.carousel-indicators .active {
	background-color: #446e9b;
}

.carousel-caption {
	right: 15%;
	bottom: 30px;
	left: 15%;
	z-index: 10;
	padding-top: 20px;
	padding-bottom: 20px;
	color: #ffffff;
	text-align: right;
	text-shadow: 0 2px 2px rgba(0, 0, 0, 0.6);
}

body {
	background-image: url("imgs/mobile_hills.png");
	background-repeat: no-repeat;
	background-position: bottom center;
	background-size: 1200px 300px;
	background-attachment: fixed;
}

.headline {
	height: 340px
}

.show {
	display: block;
}

.notallowed {
	cursor: not-allowed;
}

.clickable {
	cursor: pointer;
}

#usageBar {
	-webkit-transition: width 1.0s ease !important;
	-moz-transition: width 1.0s ease !important;
	-o-transition: width 1.0s ease !important;
	transition: width 1.0s ease !important;
}

.blur {
	box-shadow: 0px 0px 10px #66CD00;
	-moz-box-shadow: 0px 0px 10px #66CD00;
	-webkit-box-shadow: 0px 0px 10px #66CD00;
}

.clevertext {
	display: inline-block;
	white-space: nowrap;
	width: 100%;
	overflow: hidden;
	text-overflow: ellipsis;
}

.tooltip-inner {
    white-space:pre;
    max-width:none;
}

#wrap {
	/* Negative indent footer by its height */
	margin: 0 auto -120px; 
	/* Pad bottom by footer height */
	padding: 0 0 30px;
}

#footer-wofd {
	height: 50px;
}
</style>
</jsp:attribute>
	<jsp:attribute name="jscode">
<script type="text/javascript" src="js/hexutil.js"></script>
<script type="text/javascript" src="js/home.js"></script>
<script type="text/javascript" src="js/drawer.js"></script>
<script type="text/javascript" src="js/jquery.als-1.2.min.js"></script>
<script type="text/javascript">
    var opEvent = false;
    var blockingUI = false;
    var autostart = true;
    var vboxs = [];
    <c:forEach var="v" items="${requestScope.vboxlist}">vboxs.push('${v.vmId}');
    </c:forEach>

    var selected = '${requestScope.selected.vmId}';
    
    var preferred;
    <c:if test="${ not empty requestScope.preferred }">
    preferred = '${requestScope.preferred.vmId}';
    </c:if>
    
    //
    $('document').ready(function() {
	$('.tooltips').tooltip();
	//
	$('#footer-wofd').removeClass('hide');
	//
	AllVboxDrawer.init();
	//
	$("#all-vbox-list").als({
	    circular : "yes"
	});
	//
	$(".extended").on('mouseenter',function () {
	    $(this).find('.FavIconDiv').removeClass('hide');
	});
	$(".extended").on('mouseleave',function () {
	    $(this).find('.FavIconDiv').addClass('hide');
	});
	//
	$('body').on('hidden.bs.modal', '.modal', function() {
	    $(this).removeData('bs.modal');
	});
	//
	status_pooling();
    });
</script>
</jsp:attribute>
<jsp:body>	
<c:if test="${not empty requestScope.notify_info}">
<div class="row">
	<div class="col-lg-12">
		<div class="alert alert-dismissable alert-info">
			<button type="button" class="close" data-dismiss="alert">×</button>
            <c:out value="${requestScope.notify_info}" escapeXml="false"/>
		</div>
	</div>
</div>
</c:if>
<div class="row">
	<div class="col-lg-4 col-md-6 col-sm-8 col-xs-12">
		<h2 style="margin-top: 0px">今天您想从哪儿开始？</h2>
		<h5>
		您可以选择访问文件或者在vBox中工作
		<span class="tooltips" id="vboxmissing" data-toggle="tooltip" title="" data-original-title="没找到您的vBox?" data-placement="right"><i class="icon-question-sign"></i></span>
		</h5>
	</div>
	<div class="col-lg-4 col-lg-offset-4 col-md-6 col-md-offset-0 col-sm-4 col-sm-offset-0 hidden-xs col-xs-offset-0" style="padding-top: 20px">
		<div id="progress-zone" style="text-align: right;">
			<span id="progress-msg" class="text-left text-muted"></span>
			<img src="imgs/wait.gif" id="progress-image" />
		</div>
	</div>
</div>
<div class="row">
	<div class="col-xs-12 col-sm-4 col-md-3 col-lg-3">
		<div class="thumbnail headline">
            <img src="imgs/vbox_myfile.png">
			<div style="position: absolute; top: 5px; right: 25px" class="MyFileIconDiv">
				<img src="imgs/ajax-loader.gif" alt="Loading" class="img-myfile-refresh">
			</div>
			<div class="caption">
				<h3><span class="clickable userop text-muted clevertext" id="myfile">我的文件</span></h3>
				<p><i class="icon-folder-open"></i>管理我的个人空间，这些文件可以在所有的vBox中访问。</p>
				<div class="progress" id="usage">
					<div class="progress-bar" style="width: 0%;" id="usageBar"></div>
				</div> <!-- End of Usage Bar -->
			</div>
		</div>
	</div> <!-- End Of MyFile -->
<c:choose>
<c:when test="${ empty requestScope.personal }">
	<div class="col-xs-12 col-sm-4 col-md-3 col-lg-3" style="margin-bottom: 10px;">
		<div class="thumbnail headline" style="border: dashed 2px #EEEEEE;">
            <img src="imgs/vbox_personal_empty.png">
			<div class="caption">
				<h3><span class="clevertext" style="color: #BBBBBB">未来的个人空间</span></h3>
				<p style="color: #BBBBBB">今后您申请获得了个人vBox，将会在这里显示。</p>
			</div>
		</div>
	</div> <!-- End of Empty Personal Vbox -->
</c:when>
<c:otherwise>
        <div class="col-xs-12 col-sm-4 col-md-3 col-lg-3 hidden-sm" style="margin-bottom: 10px">
          <div class="thumbnail vbox headline">
            <img src="imgs/vbox_personal.png">
            <div style="position: absolute; top: 5px; right: 25px" class="OpIconDiv">
            <img src="imgs/ajax-loader.gif" alt="Loading" class="img-refresh">
            </div>
            <div class="caption">
              <h3><span class="clickable userop clevertext" onclick="show_detail('<c:out value="${requestScope.personal.vmId}"/>');">
			  <c:out value="${requestScope.personal.vmTitle}" />
			  </span></h3>
              <p class="clevertext"><c:out value="${requestScope.personal.vmDescription}" /></p>
              <p align="center">
              <button class="btn btn-primary btn-sm start-button userop">
              <i class="glyphicon glyphicon-time"></i>&nbsp;<span>等待</span>
			  </button></p>
            </div>
            <div style="position: absolute; bottom: 5px; right: 25px; width: 70px; text-align: right;" class="statusIconDiv">
	            <i class="icon-refresh hide status"	id="<c:out value="${requestScope.personal.vmId}"/>"></i>
	            <i class="icon-bolt hide heartbeat"></i>
	            <i class="icon-code-fork hide network"></i>
	            <i class="icon-ok hide passwd"></i>
            </div>
          </div>
        </div> <!-- End of Empty Preferred Vbox -->
</c:otherwise>
</c:choose>
<c:choose>
<c:when test="${ empty requestScope.preferred }">
        <div class="col-xs-12 col-sm-4 col-md-3 col-lg-3 hidden-sm" style="margin-bottom: 10px;">
          <div class="thumbnail headline" style="border: dashed 2px #EEEEEE;">
            <img src="imgs/vbox_course_empty.png">
            <div class="caption">
              <h3><span class="clevertext" style="color: #BBBBBB">未来的课程空间</span></h3>
              <p style="color: #BBBBBB">如果以后您参加的课程分配了vBox，将会在这里显示。</p>
            </div>
          </div>
        </div> <!-- End of Empty Course Vbox -->
</c:when>
<c:otherwise>
        <div class="col-xs-12 col-sm-4 col-md-3 col-lg-3" style="margin-bottom: 10px">
          <div class="thumbnail vbox headline">
            <img src="imgs/vbox_course.png">
            <div style="position: absolute; top: 5px; right: 25px" class="OpIconDiv">
            <img src="imgs/ajax-loader.gif" alt="Loading" class="img-refresh">
            </div>
            <div class="caption">
              <h3><span class="clickable userop clevertext" onclick="show_detail('<c:out value="${requestScope.preferred.vmId}"/>');">
			  <c:out value="${requestScope.preferred.vmTitle}" />
			  </span></h3>
              <p class="clevertext"><c:out value="${requestScope.preferred.vmDescription}" /></p>
              <p align="center">
              <button class="btn btn-primary btn-sm start-button userop">
              <i class="glyphicon glyphicon-time"></i>&nbsp;<span>等待</span>
			  </button></p>
            </div>
            <div style="position: absolute; bottom: 5px; right: 25px; width: 70px; text-align: right;" class="statusIconDiv">
	            <i class="icon-refresh hide status"	id="<c:out value="${requestScope.preferred.vmId}"/>"></i>
	            <i class="icon-bolt hide heartbeat"></i>
	            <i class="icon-code-fork hide network"></i>
	            <i class="icon-ok hide passwd"></i>
            </div>
          </div>
        </div> <!-- End of Preferred Box -->
</c:otherwise>
</c:choose>
        <div class="col-xs-12 col-sm-4 col-md-3 col-lg-3">
          <div class="thumbnail headline" style="border: dashed 2px #EEEEEE;">
			<img src="imgs/apply_logo.png">
            <div class="caption">
            	<h3><span class="clickable clevertext" onclick="link('applyaccess.do');">申请一个vBox</span></h3>
            	<p>需要一个vBox吗？如果您有课程代码，还可以申请一个课程专用vBox哦！</p>
            </div>
          </div>
        </div> <!-- End of Apply -->
	</div> <!-- End of Headline -->
<c:if test="${ not empty requestScope.extended }">
    <div class="row">
        <div id="drawer" data-expanded="false">
			<div id="drawer-handle"><i class='icon-chevron-down'></i>&nbsp;查看全部&nbsp;<i class='icon-chevron-down'></i></div>
			<div id="drawer-content">
				<div class="row als-container" id="all-vbox-list">
				<span class="als-prev"><img src="imgs/prev.png" alt="prev" title="previous" /></span>
				<div class="als-viewport">
				<div class="als-wrapper">
<c:forEach var="vbox" items="${requestScope.extended}">
				<div class="als-item vbox extended" style="background-color: rgba(255, 255, 255, .8); border: solid 1px #DDD; padding: 5px; border-radius: 4px; width: 220px"> 
		            <div style="position: absolute; top: 5px; right: 5px" class="OpIconDiv hide">
			            <img src="imgs/ajax-loader.gif" alt="Loading" class="img-refresh">
		            </div>
		            <div style="position: absolute; top: 3px; left: 5px" class="FavIconDiv hide">
			            <i class="icon-heart" onclick="mark_fav('<c:out value="${vbox.vmId}"/>')" style="cursor: pointer; color: #ffb6c1"></i>
		            </div>
				    <div class="caption">
						<h3><span class="clickable userop clevertext" onclick="show_detail('<c:out value="${vbox.vmId}"/>');"><c:out value="${vbox.vmTitle}" /></span></h3>
						<p class="clevertext"><c:out value="${vbox.vmDescription}" /></p>
						<p><button class="btn btn-primary btn-sm start-button userop"><i class="glyphicon glyphicon-time"></i>&nbsp;<span>等待</span></button></p>
				     </div>
	                 <div style="position: absolute; bottom: 3px; right: 5px; width: 70px; text-align: right;" class="statusIconDiv">
			            <i class="icon-refresh hide status"	id="<c:out value="${vbox.vmId}"/>"></i>
	    				<i class="icon-bolt hide heartbeat"></i>
	            		<i class="icon-code-fork hide network"></i>
			            <i class="icon-ok hide passwd"></i>
		             </div>
				</div> <!-- End of each extended vbox -->
</c:forEach>
<c:forEach var="repeat" varStatus="varStatusName" begin="${ fn:length(requestScope.extended) }" end="3"	step="1">
				   <div class="als-item" style="background-color: rgba(255, 255, 255, .8); border: dashed 2px #AAAAAA; padding: 4px; border-radius: 4px; width: 220px"> 
				    <div class="caption">
				     <h3><span class="clickable clevertext" style="color: #BBBBBB">虚位以待</span></h3>
				       <p class="clevertext" style="color: #BBBBBB">今后的课程会显示在这里</p>
				       <p><button class="btn btn-primary btn-sm disabled"><i class="glyphicon glyphicon-time"></i>&nbsp;<span>开始</span></button></p>
				     </div>
				   </div> <!-- End of each empty placeholder -->
</c:forEach>
				</div>
				</div> <!-- End of viewport -->
				<span class="als-next"><img src="imgs/next.png" alt="next" title="next" /></span>
				</div> <!-- End of ALS support -->
			</div> <!-- End of Drawer Content -->
	  	</div> <!-- End of Drawer -->
    </div> <!-- End of Row -->
</c:if>
	<div class="modal fade" id="vBoxDetailModal" tabindex="-1">
	</div><!-- /.modal -->
</jsp:body>
</t:template>