<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
	<jsp:attribute name="header">
		<link rel="stylesheet" href="css/slider.css">
		<script src="js/hexutil.js"></script>
    </jsp:attribute>
	<jsp:attribute name="jscode">
		<script src="js/bootstrap-slider.js"></script>
       	<script type="text/javascript">
				    $('#sl1').slider({
					formater : function(value) {
					    switch (value) {
					    case 0:
						return '一般';
					    case 1:
						return '中等';
					    case 2:
						return '较大';
					    case 3:
						return '特大';
					    }
					}
				    });
				    <c:if test="${ requestScope.TooManyRequests }">
				    $('#requestForm').bind('submit', function() {
					return false;
				    });
				    </c:if>
				    <c:if test="${ not empty requestScope.ScopeerrorMsg }">
				    $('#inputCode').parents('.form-group').addClass('has-error');
				    </c:if>
				</script>
    </jsp:attribute>
	<jsp:body>
<c:if test="${ requestScope.TooManyRequests }">
<div class="row">
<div class="col-lg-12">
<div class="alert alert-dismissable alert-warning">
<button type="button" class="close" data-dismiss="alert">×</button>
<p>抱歉告诉您，您需要等待之前的请求处理完毕后才能继续申请。如有问题请<a href="#" class="alert-link">联系我们</a>。</p>
</div>
</div>
</div>
</c:if>

<c:if test="${ not empty requestScope.errorMsg }">
<div class="row">
<div class="col-lg-12">
<div class="alert alert-dismissable alert-warning">
<button type="button" class="close" data-dismiss="alert">×</button>
<p>
							<c:out value="${ requestScope.errorMsg }" />。如有问题请<a href="#"
								class="alert-link">联系我们</a>。</p>
</div>
</div>
</div>
</c:if>



		<div class="row">
			<div class="col-lg-6 col-lg-offset-1 col-md-8 col-sm-12 col-xs-12">
				<div class="well" style="margin-top: 50px">
					<form class="bs-example form-horizontal" method="POST"
						accept-charset="utf-8" id="requestForm" action="applyaccess.do">
						<fieldset>
							<legend>花几秒钟，申请vBox!</legend>
<c:if test="${ requestScope.firstTimeUser }">
<div class="fixlegend"></div>
<div class="panel panel-success">
<div class="panel-heading"><h3 class="panel-title">欢迎您第一次来，作为礼物我们会为您分配一个自己的vBox!</h3></div>
<div class="panel-body">
<div class="form-group">
	<label for="inputID" class="col-lg-2 col-md-2 col-sm-2 control-label" for="inputID">您是</label>
	<div class="col-lg-10 col-md-10 col-sm-10">
	<div class="input-group">
		<span class="input-group-addon"><i class="icon-envelope"></i></span>
		<input type="text" class="form-control" id="inputID"
													placeholder="Your ID" name="UserID"
													value="<c:out value="${requestScope.identity}"/>" disabled>
        </div>
	</div>
</div>
<div class="form-group">
<label class="col-lg-2 col-md-2 col-sm-2 control-label" for="sl1">空间</label> 
<div class="col-lg-10 col-md-10 col-sm-10">
<span class="help-block">这个是您保存个人数据的空间，在您所有的vBox中都可使用。</span>
<input type="text" data-slider-min="0" data-slider-max="3"
													data-slider-step="1" data-slider-value="1" id="sl1"
													class="form-control" style="width: 100%"
													name="RequestedQuota"
													value="<c:out value="${requestScope.RequestedQuota}"/>">
</div>
</div>
</div>
</div>
</c:if>
							<div class="form-group">
								<label class="col-lg-2 col-md-2 col-sm-2 control-label" for="inputCode">课程代码</label>
								<div class="col-lg-10 col-md-10 col-sm-10">
									<input type="text" class="form-control" id="inputCode"
										placeholder="如有课程代码请填写在这里" name="CourseCode"
										value="${requestScope.CourseCode}" maxlength="10" data-validation-maxlength-message="课程代码太长了">
								</div>
							</div>
							<div class="form-group">
								<label for="textArea" class="col-lg-2 col-md-2 col-sm-2 control-label"
									for="inputJustification">备注</label>
								<div class="col-lg-10 col-md-10 col-sm-10"> 
									<textarea class="form-control" rows="3" id="inputJustification"
										placeholder="请填写您认为任何对您的课程申请有帮助的信息。" name="Justification" maxlength="500">${requestScope.Justification}</textarea>
								</div>
							</div>
							<div class="form-group">
								<div class="col-lg-10 col-lg-offset-2">
									<button type="button" class="btn btn-default" onclick="window.location='home.do'">返回</button>
<c:choose>
<c:when test="${ not requestScope.TooManyRequests }">
									<button type="submit" class="btn btn-primary">提交申请</button>
</c:when>
<c:otherwise>
									<button type="button" class="btn btn-primary" disabled>不能提交</button>
</c:otherwise>
</c:choose>
								</div>
							</div>
						</fieldset>
					</form>
				</div>
			</div>
			<div class="col-lg-3 col-lg-offset-1 col-md-4 hidden-sm hidden-xs">
				<img src="imgs/apply.png">								
			</div>
		</div>
    </jsp:body>
</t:template>
