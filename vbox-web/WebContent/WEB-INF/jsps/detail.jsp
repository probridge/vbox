<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h3 class="modal-title">
					<c:out value="${ requestScope.vmTitle }"></c:out>
					</h3>
				</div>
				<div class="modal-body">
<c:if test="${ not empty requestScope.vmDescription }">
				<h4>描述</h4><hr style="margin: 2px 0">
				<p><c:out value="${ requestScope.vmDescription }"></c:out></p>
</c:if>
<c:if test="${ not empty requestScope.vmCourseCode }">
				<h4>课程</h4><hr style="margin: 2px 0">
				<p><c:out value="${ requestScope.vmCourseCode }"></c:out></p>
</c:if>
<c:if test="${ not empty requestScope.vmGMInfo }">
				<h4>母盘</h4><hr style="margin: 2px 0">
				<p><c:out value="${ requestScope.vmGMInfo }"></c:out></p>
</c:if>
				<div class="panel-group" id="accordion" style="margin-top: 30px">
				    <div class="panel panel-default">
				      <div class="panel-heading">
				        <h4 class="panel-title">
				          <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
				            	当前状态
				          </a>
				        </h4>
				      </div>
				      <div id="collapseOne" class="panel-collapse collapse" style="height: 0px;">
				      <div class="panel-body">
						<table class="table">
						  <thead>
						    <tr>
						      <th>状态</th>
						      <th>操作系统</th>
						      <th>网络地址</th>
						      <th>登录帐号</th>
						    </tr>
						  </thead>
						  <tbody>
						    <tr>
						      <td>${requestScope.vmStatus}</td>
						      <td>${requestScope.vmHeartbeat}</td>
						      <td>${requestScope.vmIpAddress}</td>
						      <td>${requestScope.vmGuestPassword}</td>
						    </tr>
						  </tbody>
						</table>
						<div class="text-right">
						<small style="margin-right: 20px"><em>更新于：${requestScope.vmLastUpdateTimestamp}</em></small>
						</div>
				      </div>
				    </div>
				  </div>
				  <div class="panel panel-default">
				    <div class="panel-heading">
				      <h4 class="panel-title">
				        <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">
				          	技术信息
				        </a>
				      </h4>
				    </div>
				    <div id="collapseTwo" class="panel-collapse collapse">
				      <div class="panel-body">
						<p>标识符：${requestScope.vmName}</p>
						<p>UUID：${requestScope.vmId}</p>
						<table class="table">
						  <thead>
						    <tr>
						      <th>核心数</th>
						      <th>内存容量</th>
						      <th>网络类型</th>
						      <th>分配策略</th>
						    </tr>
						  </thead>
						  <tbody>
						    <tr>
						      <td>${requestScope.vmCores}</td>
						      <td>${requestScope.vmMemory}</td>
						      <td>${requestScope.vmNetwork}</td>
						      <td>${requestScope.vmPersistance}</td>
						    </tr>
						  </tbody>
						</table>
				      </div>
				    </div>
				  </div>
				</div>
				</div>
				<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
				</div>
			</div><!-- /.modal-content -->
		</div><!-- /.modal-dialog -->
<script type="text/javascript">
$('.tooltips').tooltip({
    html: true
});
</script>