<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:template>

<jsp:attribute name="header">
<link rel="stylesheet" href="css/web_sprites.css">
<!-- blueimp Gallery styles -->
<link rel="stylesheet" href="css/blueimp-gallery.min.css">
<!-- CSS to style the file input field as button and adjust the Bootstrap progress bars -->
<link rel="stylesheet" href="css/jquery.fileupload-ui.css">
<style type="text/css">
#browser .file_header {
	-moz-box-shadow: 0 1px 1px rgba(0, 0, 0, 0.12), inset 0 0 0 #000;
	-webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, 0.12), inset 0 0 0 #000;
	box-shadow: 0 1px 1px rgba(0, 0, 0, 0.12), inset 0 0 0 #000;
	border-top: 1px solid #82cffa;
	border-bottom: 1px solid #96c4ea;
	border-left: 1px solid #e7f2fb;
	border-right: 1px solid #e7f2fb;
	background: #f0f9ff url(imgs/white_pixel.gif) repeat-x;
}

#browser .fileicon {
	width: 32px;
}

#browser .filename {
	font-size: 13px;
	vertical-align: middle;
}

#browser .clevertext {
	display: inline-block;
	white-space: nowrap;
	width: 100%;
	overflow: hidden;
	text-overflow: ellipsis;
}

#browser .filetype {
	width: 100px;
	font-size: 13px;
	vertical-align: middle; 
}

#browser .filesize {
	width: 100px;
	font-size: 13px;
	vertical-align: middle;
}

#browser .filedate {
	font-size: 13px;
	width: 160px;
	vertical-align: middle;
}

#browser .filedelete {
	font-size: 13px;
	width: 40px;
	vertical-align: middle;
}

.breadcrumb {
	background-color: white;
}

.table-hover>tbody>tr:hover>td img.s_web_show-deleted {
    display:inline;
}

.table-hover>tbody>tr>td img.s_web_show-deleted {
    display:none;
}
/**
 * Progress bars with centered text
 */
.progress {
    margin-bottom: 0px;
}
</style>
</jsp:attribute>

<jsp:attribute name="jscode">
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="active">
        <td class="fileicon">
            <i class="icon-upload icon-2"></i>
        </td>
		<td class="filename">
			<span class="clevertext">{%=file.name%}</span>
			{% if (file.error) { %}
				<div><span class="label label-danger">Error</span> {%=file.error%} </div>
        	{% } %}
		</td>
        <td class="filetype">--</td>
        <td class="filesize">
            <p class="size">{%=o.formatFileSize(file.size)%}</p>
        </td>
        <td class="filedate">
            {% if (!o.files.error) { %}
                <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="progress-bar progress-bar-success" style="width:0%;"></div></div>
            {% } %}
        </td>
		<td class="filedelete">
			<img alt="删除" src="imgs/icon_spacer.gif" class="sprite sprite_web s_web_show-deleted ">
		</td>
    </tr>
{% } %}
</script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
{% if (file.newfile==true) { %}
<tr class="success">
{% } else { %}
<tr>
{% } %}
{% if (file.size!=-1) { %}
        <td class="fileicon">
			<img class="sprite sprite_web s_web_page_white_word_gray_32 icon" src="imgs/icon_spacer.gif">
        </td>
		<td class="filename"><span class="clevertext" style="cursor: pointer" onclick="window.location='./upload-servlet?fn=getfile&rp=${param.rp}&f={%=file.hexname%}'">{%=file.name%}</span></td>
        <td class="filetype">{%=file.type%}文件 </td>
        <td class="filesize">{%=o.formatFileSize(file.size)%}</td>
        <td class="filedate">{%=file.lastModified%}</td>
{% } else { %}
        <td class="fileicon">
            <img class="sprite sprite_web s_web_folder_32 icon"	src="imgs/icon_spacer.gif">
        </td>
		<td class="filename"><span style="cursor: pointer" onclick="window.location='?rp=${param.rp}{%=file.hexname%}'">{%=file.name.slice(0, -1)%}</span></td>
        <td class="filetype">目录 </td>
        <td class="filesize">-</td>
        <td class="filedate">{%=file.lastModified%}</td>
{% } %}
		<td class="filedelete">
			<img alt="删除" src="imgs/icon_spacer.gif" class="sprite sprite_web s_web_show-deleted " style="cursor:pointer" onclick="del('{%=file.hexname%}','{%=file.path%}',{%=(file.size==-1)%});">
		</td>
    </tr>
{% } %}
</script>
<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
<script src="js/vendor/jquery.ui.widget.js"></script>
<!-- The Templates plugin is included to render the upload/download listings -->
<script src="js/tmpl.min.js"></script> 
<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
<!-- <script src="js/load-image.min.js"></script>  -->
<!-- The Canvas to Blob plugin is included for image resizing functionality -->
<!-- <script src="js/canvas-to-blob.min.js"></script>  -->
<!-- blueimp Gallery script -->
<!-- <script src="js/jquery.blueimp-gallery.min.js"></script>  -->
<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
<script src="js/jquery.iframe-transport.js"></script>
<!-- The basic File Upload plugin -->
<script src="js/jquery.fileupload.js"></script>
<!-- The File Upload processing plugin -->
<script src="js/jquery.fileupload-process.js"></script>
<!-- The File Upload image preview & resize plugin -->
<!-- <script src="js/jquery.fileupload-image.js"></script>  -->
<!-- The File Upload audio preview plugin -->
<!-- <script src="js/jquery.fileupload-audio.js"></script>  -->
<!-- The File Upload video preview plugin -->
<!-- <script src="js/jquery.fileupload-video.js"></script>  -->
<!-- The File Upload validation plugin -->
<script src="js/jquery.fileupload-validate.js"></script>
<!-- The File Upload user interface plugin -->
<script src="js/jquery.fileupload-ui.js"></script>
<!-- The XDomainRequest Transport is included for cross-domain file deletion for IE 8 and IE 9 -->
<!--[if (gte IE 8)&(lt IE 10)]>
<script src="js/cors/jquery.xdr-transport.js"></script>
<![endif]-->
<script type="text/javascript">
var rp='${param.rp}';
</script>
<script type="text/javascript" src="js/hexutil.js"></script>
<script type="text/javascript" src="js/myfiles.js"></script>
</jsp:attribute>

<jsp:body>
   <!-- The file upload form used as target for the file upload widget -->
   <form id="fileupload" action="" method="POST" enctype="multipart/form-data">
    <div class="row">
      <div class="col-lg-12">
      <h2>您的文件</h2>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-6 col-md-6 col-sm-6 col-xs-6">
      <t:breadcrumb rp="${param.rp}"></t:breadcrumb>
      </div>
      <div class="col-lg-6 col-md-6 col-sm-6 col-xs-6">
        <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
        <div class="row fileupload-buttonbar">
            <div class="col-lg-6 col-md-6 col-sm-7 col-xs-8" align="right">
                <!-- The fileinput-button span is used to style the file input field as button -->
                <span class="btn btn-primary fileinput-button btn-xs">
                    <i class="glyphicon glyphicon-upload"></i>
                    <span>文件</span>
                    <input type="file" name="files[]" multiple>
                </span>
				<!-- Button trigger modal -->
				<a data-toggle="modal" href="#createFolderWindow" class="btn btn-success btn-xs">
				<i class="glyphicon glyphicon-plus"></i>文件夹
				</a>
                <button type="reset" class="btn btn-warning cancel btn-xs">
                    <i class="glyphicon glyphicon-ban-circle"></i>
                    <span>取消</span>
                </button>
                <!-- The loading indicator is shown during file processing -->
                <span class="fileupload-loading"></span>
            </div>
            <!-- The global progress information -->
            <div class="col-lg-6 col-md-6 col-sm-5 col-xs-4 fileupload-progress fade">
                <!-- The global progress bar -->
                <div class="progress progress-striped active">
                    <div class="progress-bar progress-bar-success" style="width: 0%;"></div>
                </div>
                <!-- The extended global progress information -->
                <div class="progress-extended"></div>
            </div>
        </div>
      </div>
    </div>    
    <div class="row">
    <div class="col-lg-12">
    <!-- The table listing the files available for upload/download -->
    <table class="table table-hover" id="browser">
                <thead class="file_header">
                  <tr>
                    <th class="fileicon"></th>
                    <th class="filename">名称</th>
                    <th class="filetype">类型</th>
                    <th class="filesize">大小</th>
                    <th class="filedate">最后修改时间</th>
                    <th class="filedelete"></th>
                  </tr>
                </thead>
                <tbody class="files"></tbody>
    </table>
	</div>
	</div>
    <div class="row" id="dropzone">
    <div class="col-lg-12">
        <div style="overflow-y: auto; width: 100%; height: 85px; border: 1px dashed;">
        <div id="background" style="position: absolute; z-index: -1; overflow: hidden;">
				<h2 style="color: #CCCCCC">&nbsp;&nbsp;<i class="icon-cloud-upload"></i>&nbsp;&nbsp;上传文件拖到这里</h2>
		</div>
        </div>
    </div>
    </div>
</form>
<!-- Modal -->
<div class="modal fade" id="createFolderWindow" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>
        <h4 class="modal-title">新建文件夹</h4>
      </div>
      <div class="modal-body">
      <div class="form-group" id="divFolderName">
            <input type="text" class="form-control" id="folderName" placeholder="文件夹名称"/>
      </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default btn-sm" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary btn-sm" id="btnCreateFolder" data-loading-text="请稍等...">确定</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<div class="modal fade" id="deleteConfirmWindow" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>
        <h4 class="modal-title">确认删除</h4>
      </div>
      <div class="modal-body">
      <div class="form-group" id="divDelete">
      		您确实要删除“<label id="fileToDelete"></label>”<label id="fileToDelete1"></label>吗？
      </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-primary btn-sm" id="btnDelete" data-loading-text="请稍等...">删除</button>
        <button type="button" class="btn btn-default btn-sm" data-dismiss="modal">取消</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
</jsp:body>
</t:template>