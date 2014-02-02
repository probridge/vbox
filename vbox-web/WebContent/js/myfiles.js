$('document').ready(function() {
    if (!Modernizr.filereader) {
	$('#dropzone').css('display','none');
    }

    $('#fileupload').fileupload({
	url : 'upload-servlet'
    });

    $('#fileupload').fileupload('option', {
	autoUpload : true,
	maxFileSize : 1048576000,
	sequentialUploads : true,
	progressInterval : 500,
	bitrateInterval : 1000,
	formData : {
	    rp : rp
	}
    });

    $.ajax({
	url : $('#fileupload').fileupload('option', 'url'),
	type : 'GET',
	cache : false,
	data : {
	    fn : 'ping',
	    rp : rp
	}
    }).fail(function(jqXHR, textStatus, errorThrown) {
	$('<div class="alert alert-danger"/>').text('Ooops.服务器暂时有点问题 ' + '[ERR=' + jqXHR.status + ']').appendTo('#fileupload');
    }).done(function() {
	// Load files
	$('#fileupload').addClass('fileupload-processing');
	$.ajax({
	    url : $('#fileupload').fileupload('option', 'url'),
	    cache : false,
	    data : {
		fn : 'listfile',
		rp : rp
	    },
	    dataType : 'json',
	    type : 'GET',
	    context : $('#fileupload')[0]
	}).always(function() {
	    $(this).removeClass('fileupload-processing');
	}).done(function(result) {
	    $(this).fileupload('option', 'done').call(this, null, {
		result : result
	    });
	});
    });

    $('#fileupload').bind('fileuploadprogress', function(e, data) {
	var progress = Math.floor(data.loaded / data.total * 100);
	if (data.context) {
	    if (progress < 100) {
		data.context.each(function() {
		    $(this).find('.progress-bar').text(progress + '%');
		});
	    } else {
		data.context.each(function() {
		    $(this).find('.progress-bar').text('等待完成');
		});
	    }
	}
    });

    $('#fileupload').bind('fileuploadstart', function(e, data) {
	stopTimer = true;
    });

    $('#fileupload').bind('fileuploadstop', function(e, data) {
	stopTimer = false;
    });

    $('#btnCreateFolder').bind("click", function() {
	$('#divFolderName').removeClass("has-error");
	$('#divFolderName > label').remove();
	$.ajax({
	    url : $('#fileupload').fileupload('option', 'url'),
	    type : 'POST',
	    data : {
		fn : 'mkdir',
		rp : rp,
		f : toHex(utf8.toByteArray($('#folderName').val()))
	    },
	}).done(function() {
	    $('#createFolderWindow').modal('hide');
	    window.location.reload();
	}).fail(function() {
	    $('<label class="control-label" for="folderName"/>').text('操作失败').appendTo('#divFolderName');
	    $('#divFolderName').addClass("has-error");
	}).always(function() {
	    //
	});
    });
});

function del(hexname, path, isdir) {
    $('#btnDelete').unbind();
    $('#fileToDelete').text(utf8.parse(fromHex(hexname)));
    if (isdir) {
	$('#fileToDelete1').text('以及里面所有内容');
    } else {
	$('#fileToDelete1').text('');
    }
    $('#deleteConfirmWindow').modal('show');
    $('#btnDelete').bind("click", function() {
	$('#divDelete > p').remove();
	$.ajax({
	    url : $('#fileupload').fileupload('option', 'url'),
	    data : {
		fn : 'del',
		rp : rp,
		f : hexname
	    },
	    type : 'POST'
	}).done(function() {
	    $('#deleteConfirmWindow').modal('hide');
	    window.location.reload();
	}).fail(function() {
	    $('<p class="text-danger"/>').text('删除失败').appendTo('#divDelete');
	}).always(function() {
	    //
	});
    });
}