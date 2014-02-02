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
<c:if test="${ empty requestScope.error }">
<div class="row">
<div class="col-lg-12">
<div class="alert alert-dismissable alert-info">
<button type="button" class="close" data-dismiss="alert">×</button>
<p>除非我们告知您，通常您需要使用上海交大jAccount统一认证登录系统，而不需要在此注册。</p>
</div>
</div>
</div>
</c:if>
<div class="row">
	<div class="col-lg-4 col-lg-offset-1 col-md-4 col-md-offset-1 col-sm-5 hidden-xs"> 
		<img src="imgs/register.png">
	</div>
	<div class="col-lg-5 col-lg-offset-1 col-md-5 col-md-offset-1 col-sm-6 col-sm-offset-0 col-xs-12"> 
		<div class="row well" style="margin-top: 50px">
		<form class="bs-example form-horizontal" method="post" action="">
			<fieldset>
				<legend>注册帐号</legend>
				<div class="form-group">
					<label for="inputEmail" class="col-lg-3 col-md-3 col-sm-4 control-label">Email</label>
					<div class="col-lg-8 col-md-8 col-sm-8">
					<div class="input-group" style="width: 100%">
						<input type="email" class="form-control" id="inputEmail" name="inputEmail"
							placeholder="您的邮箱地址" maxlength="30" required>
			         </div>
					</div>
				</div>
				<div class="form-group">
					<label for="inputPassword1" class="col-lg-3 col-md-3 col-sm-4 control-label">请输入密码</label>
					<div class="col-lg-8 col-md-8 col-sm-8">
					<div class="input-group" style="width: 100%">
						<input type="password" class="form-control" id="inputPassword1" name="inputPassword1" required maxlength="30">
			         </div>
					</div>
				</div>
				<div class="form-group">
					<label for="inputPassword2" class="col-lg-3 col-md-3 col-sm-4 control-label">请再输入一次</label>
					<div class="col-lg-8 col-md-8 col-sm-8">
					<div class="input-group" style="width: 100%">
						<input type="password" class="form-control" id="inputPassword2" name="inputPassword2" required maxlength="30" data-validation-match-match="inputPassword1">
			         </div>
					</div>
				</div>
				<div class="form-group">
					<div class="col-lg-4 col-lg-offset-3 col-md-4 col-md-offset-3 col-sm-4 col-sm-offset-4">
						<button type="submit" class="btn btn-primary btn-sm" style="width: 80px;"><i class="icon-lock"></i>&nbsp;&nbsp;注册</button>
					</div>
				</div>
				</fieldset>
			</form>
		</div>
		<div class="row">
		<div class="col-lg-12">
		无法访问您的帐户？
		</div>
		</div>				
	</div>
</div>
</jsp:body>
</t:template>
