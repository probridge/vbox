var AllVboxDrawer = (function($, window, document) {
    var drawer, space, handle, body;

    var settings = {
	content_height : 160,
	handle_height : 30,
	drawer_height : 30,
    };

    /**
     * Initialize the drawer element handles and call setup functions.
     */
    var init = function() {
	// We will be moving the body when the drawer expands/collapses
	container = $('#drawer-container');
	// Container
	drawer = $('#drawer');
	// This is the content container of the drawer
	content = $('#drawer-content');
	// The button/handle the user clicks on to show/hide the drawer space
	handle = $('#drawer-handle');
	// The list of interests inside the drawer space

	mockCSS();
	bindUIEvents();
    };

    /**
     * Development only. This will be replaced by real CSS
     */
    var mockCSS = function() {
	drawer.css({
	    'height' : settings.drawer_height,
	    'width' : '100%',
	    'position' : 'relative',
	    'margin-bottom' : '0px'
	});

	content.css({
	    'position' : 'relative',
	    'height' : 0,
	    'padding-top' : "0px",
	    'padding-left' : "20px",
	    'padding-right' : "20px",		
	    'overflow' : 'hidden'
	});

	handle.css({
	    'position' : 'absolute',
	    'width' : '100%',
	    'height' : settings.handle_height,
	    'bottom' : '0',
	    'padding' : '0px',
	    'color' : '#AAAAAA',
	    'cursor' : 'pointer',
	    'text-align' : 'center'
	});
    };

    var collapseDrawer = function() {
	drawer.animate({
	    'height' : '-=' + settings.content_height
	});

	content.animate({
	    'height' : '-=' + (settings.content_height)
	});
	handle.html("<i class='icon-chevron-down'></i>&nbsp;查看全部&nbsp;<i class='icon-chevron-down'></i>");
    };

    var expandDrawer = function() {
	drawer.animate({
	    'height' : '+=' + settings.content_height
	});

	content.animate({
	    'height' : '+=' + (settings.content_height)
	});
	handle.html("<i class='icon-chevron-up'></i>&nbsp;收起&nbsp;<i class='icon-chevron-up'></i>");
    };

    /**
     * All event binding should be defined here
     */
    var bindUIEvents = function() {
	handle.on('click', function(e) {
	    // Flip the data-expanded value
	    drawer.data('expanded', drawer.data('expanded') === true ? false : true);

	    // Flip the data-visible value
	    handle.data('label', drawer.data('expanded') === true ? 'Hide' : 'Show');

	    if (drawer.data('expanded') === true) {
		expandDrawer();
	    } else {
		collapseDrawer();
	    }
	});
    };

    /**
     * Attached everything together and insert in to the DOM
     */
    var attachToDOM = function() {
	content.appendTo(drawer);
	handle.appendTo(drawer);

	var fragment = document.createDocumentFragment();
	drawer.appendTo(fragment);
	container.append(fragment);
    };

    // Public variables and methods
    return {
	init : init
    };
})(jQuery, window, document);
