<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
	<jsp:attribute name="header">

</jsp:attribute>
	<jsp:attribute name="jscode">
<script type="text/javascript" src="js/hexutil.js"></script>
<script type="text/javascript" src="js/applyplaced.js"></script>
<c:if test="${not empty requestScope.cmdkey }">
<script type="text/javascript">
    $('document').ready(function() {
	start_process('${requestScope.cmdkey}');
    });
</script>
</c:if>
</jsp:attribute>
	<jsp:body>
<c:if test="${ empty requestScope.cmdkey }">
<div class="row">
<div class="col-lg-12">
  <div class="page-header">
    <h1 id="type">
						<i class="icon-ok"></i>申请成功</h1>
  </div>
</div>
</div>
<div class="row">
<div class="col-lg-12">
    <p>我们通常会在一个工作日内审核您的请求，如有任何特殊需要请<a href="#">联系我们</a>。</p>
    <p>如果您是第一次来，我们还会根据你的要求分配给您一个随时供您个人使用的vBox，之后您还可以陆续增加课程专用的vBox。</p>
    <p>您请求的数据空间的大小我们会尽量满足，这部分空间您可以在您所有的vBox中使用，也可以通过“我的文件”来访问。</p>
</div>
</div>
</c:if>
<c:if test="${not empty requestScope.cmdkey }">
<div class="row">
<div class="col-lg-12">
  <div class="page-header">
    <h1 id="type">
						<i class="icon-ok"></i>申请已批准</h1>
  </div>
</div>
</div>
<div class="row">
<div class="col-lg-12">
    <p>恭喜您，看来您的请求已经得到预先批准，请稍等不要离开，我们正在为您分配资源。</p>
    <p>如果您是第一次来，我们还会根据你的要求分配给您一个随时供您个人使用的vBox，之后您还可以陆续增加其他课程专用的vBox。</p>
    <p>您的数据空间将会按照预批准标准分配，这部分空间您可以在您所有的vBox中使用，也可以通过“我的文件”来访问。</p>
	<p>如有任何特殊需要请<a href="#">联系我们</a>。</p>
</div>
</div>
<div class="row">
<div class="col-lg-12 text-center">
<img src="imgs/processing.gif" style="margin-top: 50px" id="progress-image"/>
<br/>
<span id="progress-msg" class="text-muted"></span>
<br/>
<button id="btnContinue" type="button" class="btn btn-primary hide" onclick="window.location='landing.do'"><i class="icon-home"></i>&nbsp;继续</button>
</div>
</div>
</c:if>
</jsp:body>
</t:template>