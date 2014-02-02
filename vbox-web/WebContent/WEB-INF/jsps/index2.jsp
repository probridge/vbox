<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:template>

	<jsp:attribute name="header">
		<link rel="stylesheet" href="css/slider.css">
    </jsp:attribute>
    
    
	<jsp:attribute name="jscode">
		<script src="js/bootstrap-slider.js"></script>
       	<script type="text/javascript">
			$('#sl1').slider({
				formater : function(value) {
					switch (value) {
					case 1:
						return '一般';
					case 2:
						return '中等';
					case 3:
						return '较大';
					case 4:
						return '特大';
					}
				}
			});
		</script>
    </jsp:attribute>
    
    <jsp:body>


    </jsp:body>
</t:template>