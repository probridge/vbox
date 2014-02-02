<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<t:template>
	<jsp:attribute name="header">
    </jsp:attribute>
	<jsp:attribute name="jscode">
    </jsp:attribute>
	<jsp:body>
<c:if test="${ not empty requestScope.error }">
<div class="row">
<div class="col-lg-12">
<div class="alert alert-dismissable alert-warning">
<button type="button" class="close" data-dismiss="alert">×</button>
<p><c:out value="${ requestScope.error }"/>如有问题请<a href="#" class="alert-link">联系我们</a>。</p>
</div>
</div>
</div>
</c:if>
		<div class="row">
			<div class="col-lg-5 col-lg-offset-1 col-md-6 col-md-offset-1 col-sm-8 col-xs-12">
				<div class="well" style="margin-top: 50px"> 
					<form class="bs-example form-horizontal" method="POST"
						accept-charset="utf-8" id="requestForm">
						<fieldset>
							<legend>您的密码已经过期，请重新设置…</legend>
							<div class="form-group">
								<label class="col-lg-4 col-md-4 col-sm-4 col-xs-4 control-label"
									for="inputPassword1">请输入新密码：</label>
								<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8"> 
									<input type="password" class="form-control" id="inputPassword1"
										name="inputPassword1">
								</div>
							</div>
							<div class="form-group">
								<label class="col-lg-4 col-md-4 col-sm-4 col-xs-4 control-label"
									for="inputPassword2">请再输入一次：</label>
								<div class="col-lg-8 col-md-8 col-sm-8 col-xs-8"> 
									<input type="password" class="form-control" id="inputPassword2"
										name="inputPassword2">
								</div>
							</div>
							<div class="form-group">
								<div class="col-lg-8 col-lg-offset-4 col-md-8 col-md-offset-4 col-sm-8 col-sm-offset-4 col-xs-8 col-xs-offset-4">
									<button type="submit" class="btn btn-primary">修改密码</button>
								</div>
							</div>
						</fieldset>
					</form>
				</div>
			</div>
			<div class="col-lg-3 col-lg-offset-2 col-md-4 col-md-offset-1 hidden-sm hidden-xs"> 
				<img data-src="./js/holder.js/280x500"
					alt="Generic placeholder thumbnail" src="data:image/png;base64,">
			</div> 
		</div>
		   </jsp:body>
</t:template>