<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>
	<jsp:attribute name="header">
<style type="text/css">
/*****************************************************
 * generic styling for ALS elements: outer container
 ******************************************************/
.als-container {
	position: relative;
	width: 100%;
	margin: 0px auto;
	z-index: 0;
}

/****************************************
 * viewport styling
 ***************************************/
.als-viewport {
	position: relative;
	overflow: hidden;
	margin: 0px auto;
}

/***************************************************
 * wrapper styling
 **************************************************/
.als-wrapper {
	position: relative;
}

/*************************************
 * item: single list element
 ************************************/
.als-item {
	position: relative;
	display: block;
	text-align: center;
	cursor: pointer;
	float: left;
}

/***********************************************
 * prev, next: buttons styling
 **********************************************/
.als-prev, .als-next {
	position: absolute;
	cursor: pointer;
	clear: both;
}

#my-als-list {
	margin: 40px auto;
}

#my-als-list .als-item {
	margin: 0px 5px;
	padding: 4px 0px;
	text-align: center;
}

#my-als-list .als-prev {
	top: 70px;
	left: 150px;
}

#my-als-list .als-next {
	top: 70px;
	right: 150px;
}
</style> 
</jsp:attribute>
	<jsp:attribute name="jscode">
<script type="text/javascript" src="js/jquery.als-1.2.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
	$("#my-als-list").als({
	    circular : "yes"
	});
    });
</script>
</jsp:attribute>
	<jsp:body>
<div class="row als-container" id="my-als-list">
<span class="als-prev"><img src="imgs/prev.png" alt="prev"
				title="previous" /></span>
<div class="als-viewport">
<div class="als-wrapper">
<c:forEach var="repeat" varStatus="varStatusName" begin="1" end="8"
						step="1">
   <div class="als-item" style="border: dashed 2px #AAAAAA; padding: 5px; border-radius: 4px; width: 250px"> 
    <div class="caption">
     <h3>
									<span class="clickable" onclick="link('applyaccess.do');">其他的vBox</span>
								</h3>
       <p>Windows 7 64bit / 1 CPU / 512M</p>
       <p>
									<button class="btn btn-primary btn-sm">Button</button>
								</p>
     </div>
   </div>
</c:forEach>
</div>
</div>
<span class="als-next"><img src="imgs/next.png" alt="next"
				title="next" /></span>
</div>
</jsp:body>
</t:template>