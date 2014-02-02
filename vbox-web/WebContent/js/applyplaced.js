function start_process(cmdkey) {
    opEvent = true;
    toggle_progress_img(true);
    $.ajax({
	url : 'vmm-servlet',
	type : 'POST',
	dataType : 'json',
	data : {
	    fn : 'preapprove',
	    cmdkey : cmdkey
	}
    }).fail(function(jqXHR, textStatus, errorThrown) {
	if (jqXHR.status == 503)
	    msg = "之前的操作还没完成，请稍等…";
	else
	    msg = 'Ooops.服务器暂时有点问题 ERR=[' + jqXHR.status + ']';
	show_warning(msg);
	toggle_progress_img(false);
    }).done(function(data) {
	var opid = data.opid;
	function pollOpResult() {
	    $.ajax({
		url : 'vmm-servlet',
		type : 'POST',
		dataType : 'json',
		data : {
		    fn : 'getopmsg',
		    opid : opid
		}
	    }).fail(function(jqXHR, textStatus, errorThrown) {
		msg = 'Ooops.服务器暂时有点问题 ' + '[ERR=' + jqXHR.status + ']';
		show_warning(msg);
		toggle_progress_img(false);
	    }).done(function(data) {
		if (data.rv == 0) {
		    msg = '操作成功：' + utf8.parse(fromHex(data.msg));
		    show_info(msg);
		    toggle_progress_img(false);
		    update_progress_info('操作完成');
		} else if (data.rv == 1) {
		    msg = '很抱歉，操作失败！' + utf8.parse(fromHex(data.msg));
		    show_info(msg);
		    toggle_progress_img(false);
		    update_progress_info('操作失败');
		} else {
		    update_progress_info(utf8.parse(fromHex(data.msg)));
		    setTimeout(pollOpResult, 3000);
		}
	    });
	}
	setTimeout(pollOpResult, 3000);
    });
};

function update_progress_info(msg) {
    $('#progress-msg').text(msg);
}

function toggle_progress_img(show) {
    eval("$('#progress-image')." + (show ? "remove" : "add") + "Class('hide');");
    eval("$('#btnContinue')." + (show ? "add" : "remove") + "Class('hide');");    
}

function show_info(msg) {
    $(function() {
	$.pnotify({
	    title : '信息',
	    text : msg,
	    type : 'success',
	    icon : 'icon-envelope'
	});
    });
}

function show_warning(msg) {
    $(function() {
	$.pnotify({
	    title : '问题',
	    text : msg,
	    type : 'error',
	    icon : 'icon-envelope'
	});
    });
}

