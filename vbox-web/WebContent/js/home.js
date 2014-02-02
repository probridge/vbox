function load_myfile_usage() {
    return $.ajax({
	url : 'upload-servlet',
	type : 'GET',
	dataType : 'json',
	cache : false,
	data : {
	    fn : 'getrepoinfo',
	}
    }).fail(function(jqXHR, textStatus, errorThrown) {
	if (!opEvent) {
	    if (jqXHR.status == 404) {
		$('#usageBar').addClass('progress-bar-info').css('width', '100%').text('您的文件服务尚未开通');
	    } else {
		$('#usageBar').addClass('progress-bar-warning').css('width', '100%').text('文件服务暂时不可用');
		$('#usage').addClass('progress-striped');
	    }
	    $('#myfile').addClass('text-muted').removeClass('clickable');
	    $('#myfile').unbind('click');
	}
    }).done(function(data) {
	if (data.status == 'OK') {
	    var percentage = Math.floor(data.usage * 100 / data.total);
	    $('#usageBar').removeClass('progress-bar-warning').removeClass('progress-bar-info').css('width', percentage + '%').text('');
	    $('#usage').removeClass('progress-striped').removeClass('active');
	    $('#usage').attr('title', "已用" + formatUsage(data.usage) + '/共计' + formatUsage(data.total)).tooltip();
	    $('#myfile').removeClass('text-muted').addClass('clickable');
	    $('#myfile').bind('click', function() {
		link('myfiles.do');
	    });
	} else if (data.status == 'TOSTART' && autostart) {
	    $('#myfile').addClass('text-muted').removeClass('clickable');
	    $('#usageBar').removeClass('progress-bar-warning').addClass('progress-bar-primary').css('width', '100%').text('正在等待vBox');
	    $('#usage').addClass('progress-striped').addClass('active');
	    $('#myfile').unbind('click');
	    setTimeout(function() {
		switchto(data.uuid);
	    }, 500);
	}
	autostart = false;
    }).always(function() {
	$('.img-myfile-refresh').addClass('hide');
    });
}

function formatUsage(bytes) {
    if (typeof bytes !== 'number') {
	return '';
    }
    if (bytes >= 1073741824) {
	return (bytes / 1073741824).toFixed(1) + 'GB';
    }
    if (bytes >= 1048576) {
	return (bytes / 1048576).toFixed(1) + 'MB';
    }
    return (bytes / 1000).toFixed(1) + 'KB';
}

function status_pooling() {
    if (!opEvent)
	toggle_progress_img(true);
    var deferred = update_status();
    $.when.apply(null, deferred.get()).then(function() {
	$.when(load_myfile_usage()).always(function() {
	    if (!opEvent)
		toggle_progress_img(false);
	    setTimeout(status_pooling, 10000);
	});
    }, function() {
	if (!opEvent)
	    toggle_progress_img(false);
    });
}

function update_status() {
    return $('.img-refresh').map(function(i, elem) {
	var statusIconDiv = $(this).parent().siblings('.statusIconDiv');
	return $.ajax({
	    url : 'vmm-servlet',
	    type : 'POST',
	    dataType : 'json',
	    data : {
		fn : 'getstatus',
		uuid : statusIconDiv.find(".status").attr("id")
	    },
	    context : $(this)
	}).fail(function(jqXHR, textStatus, errorThrown) {
	    if (jqXHR.status != 0)
		show_warning('Ooops.服务器暂时有点问题 ' + '[ERR=' + jqXHR.status + ']');
	}).done(function(data) {
	    $(this).addClass('hide');
	    statusIconDiv.find('.status').removeClass().addClass('status').addClass(getIconClass(data.state));
	    var network = false;
	    var heartbeat = data.hb == 2;
	    var passwd = data.pwd == 'yes';
	    var running = data.state == 'Running';
	    var init = data.init == 'yes';
	    //
	    var intermit = (data.state == 'Saving' || data.state == 'Pausing' || data.state == 'Stopping' || data.state == 'Starting' || data.state == 'Resuming');

	    if (typeof data.ip !== 'undefined' && data.ip.length > 0)
		for ( var i = 0; i < data.ip.length; i++)
		    if (data.ip[i].length > 0 && data.ip[i].substring(0, 3) != '169')
			network = true;

	    eval("statusIconDiv.find('.heartbeat')." + (heartbeat ? "remove" : "add") + "Class('hide');");
	    eval("statusIconDiv.find('.network')." + (network ? "remove" : "add") + "Class('hide');");
	    eval("statusIconDiv.find('.passwd')." + (passwd ? "remove" : "add") + "Class('hide');");

	    if (data.uuid == selected)
		$(this).parents('.vbox').addClass('blur');
	    else
		$(this).parents('.vbox').removeClass('blur');

	    var btn = $(this).parents('.vbox').find('.start-button');

	    if (intermit) {
		btn.find('i').removeClass().addClass('icon-refresh').addClass('icon-spin').addClass('icon-large');
		btn.removeClass('btn-success').removeClass('btn-warning').addClass('btn-primary');
		btn.find('span').text('等待');
		btn.unbind('click');
	    } else {
		if (!running) {
		    btn.removeAttr('counter');
		    btn.removeClass('btn-success').removeClass('btn-warning').addClass('btn-primary');
		    btn.find('i').removeClass().addClass('icon-cloud').addClass('icon-large');
		    btn.find('span').text('使用');
		    btn.unbind('click');
		    btn.bind('click', function() {
			switchto(data.uuid);
		    });
		} else { // running
		    if (!heartbeat || (heartbeat && !network) || (heartbeat && network && !passwd)) {
			var counter = btn.attr('counter');
			if (!counter) {
			    btn.attr('counter', (new Date()).valueOf());
			    counter = btn.attr('counter');
			}
			var timeout = 120000;
			if (data.init)
			    timeout = 300000;
			//
			if ((new Date()).valueOf() - counter > timeout) {
			    btn.find('i').removeClass().addClass('icon-wrench').addClass('icon-large');
			    btn.removeClass('btn-primary').removeClass('btn-success').addClass('btn-warning');
			    btn.find('span').text('修复');
			    btn.unbind('click');
			    btn.bind('click', function() {
				fixit(data.uuid);
			    });
			} else {
			    btn.removeClass('btn-success').removeClass('btn-warning').addClass('btn-primary');
			    btn.find('i').removeClass().addClass('icon-refresh').addClass('icon-spin').addClass('icon-large');
			    if (data.init)
				btn.find('span').text('初始化');
			    else
				btn.find('span').text('等待');
			    //
			    btn.unbind('click');
			}
		    } else {
			btn.removeAttr('counter');
			btn.removeClass('btn-warning');
			btn.removeClass('btn-primary').addClass('btn-success');
			btn.find('i').removeClass().addClass('icon-coffee').addClass('icon-large');
			btn.find('span').text('开始');
			btn.unbind('click');
			btn.bind('click', function() {
			    link('viewbox2.do?uuid=' + data.uuid);
			});
		    }
		}
	    }
	});
    });
}

function perform(ajax_req_data) {
    if (blockingUI)
	return false;
    opEvent = true;
    toggle_progress_img(true);
    toggle_blockui(true);
    $.ajax({
	url : 'vmm-servlet',
	type : 'POST',
	dataType : 'json',
	data : ajax_req_data
    }).fail(function(jqXHR, textStatus, errorThrown) {
	if (jqXHR.status == 503)
	    msg = "之前的操作还没完成，请稍等…";
	else 
	    msg = 'Ooops.服务器暂时有点问题 ' + '[ERR=' + jqXHR.status + ']';
	if (jqXHR.status != 0)
	    show_warning(msg);
	toggle_blockui(false);
	toggle_progress_img(false);
	opEvent = false;
    }).done(function(data) {
	var opid = data.opid;
	if (typeof data.init !== 'undefined' && data.init == 'yes')
	    show_info("vBox初始化需要几分钟时间，请耐心稍候。");
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
		if (jqXHR.status != 0)
		    show_warning(msg);
		toggle_blockui(false);
		toggle_progress_img(false);
		opEvent = false;
	    }).done(function(data) {
		if (data.rv == 0) {
		    msg = '操作成功：' + utf8.parse(fromHex(data.msg));
		    show_info(msg);
		    toggle_blockui(false);
		    toggle_progress_img(false);
		    update_progress_info('');
		    opEvent = false;
		} else if (data.rv == 1) {
		    msg = '很抱歉，操作失败！' + utf8.parse(fromHex(data.msg));
		    show_info(msg);
		    toggle_blockui(false);
		    toggle_progress_img(false);
		    update_progress_info('');
		    opEvent = false;
		} else {
		    update_progress_info(utf8.parse(fromHex(data.msg)));
		    setTimeout(pollOpResult, 3000);
		}
	    });
	}
	setTimeout(pollOpResult, 3000);
    });
}

function switchto(uuid) {
    selected = uuid;
    var req_data = {
	fn : 'switchvm',
	resume : uuid,
	suspend : vboxs.toString()
    };
    perform(req_data);
}

function fixit(uuid) {
    var req_data = {
	fn : 'fixvbox',
	uuid : uuid,
    };
    perform(req_data);
}

function mark_fav(uuid) {
    return $.ajax({
	url : 'vmm-servlet',
	type : 'POST',
	dataType : 'json',
	data : {
	    fn : 'markfav',
	    uuid : uuid,
	    unmark : preferred
	}
    }).fail(function(jqXHR, textStatus, errorThrown) {
	if (jqXHR.status != 0)
	    show_warning('Ooops.服务器暂时有点问题 ' + '[ERR=' + jqXHR.status + ']');
    }).done(function(data) {
	show_info("常用vBox设置好了，下次登录这个vBox就会显示在上面");
	link("landing.do");
    });
}

function show_detail(uuid) {
    $('#vBoxDetailModal').modal({
	remote : 'detail.do?uuid=' + uuid + '&cache=' + new Date().getTime()
    });
}

function update_progress_info(msg) {
    $('#progress-msg').text(msg);
}

function getIconClass(state) {
    switch (state) {
    case 'Suspended':
    case 'Paused':
    case 'Saving':
    case 'Pausing':
	return 'icon-pause';
    case 'Running':
	return 'icon-play';
    case 'Powered Off':
    case 'Stopping':
	return 'icon-stop';
    case 'Starting':
    case 'Resuming':
	return 'icon-forward';
    default:
	return 'icon-wrench';
    }
}

function link(url) {
    if (blockingUI)
	return false;
    window.location = url;
}

function toggle_progress_img(show) {
    eval("$('#progress-image')." + (show ? "remove" : "add") + "Class('hide');");
}

function toggle_blockui(block) {
    blockingUI = block;
    eval("$('#myfile')." + (block ? "add" : "remove") + "Class('notallowed');");
    eval("$('.userop')." + (block ? "add" : "remove") + "Class('notallowed');");
    eval("$('button.userop')." + (block ? "add" : "remove") + "Class('disabled');");
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
	    icon : 'icon-envelope',
	    addclass : 'alert-danger',
	    hide : false
	});
    });
}
