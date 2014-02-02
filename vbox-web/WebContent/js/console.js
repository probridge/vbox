function connect(server, username, password, fullscreen) {
    document.getElementById("screenArea").style.display = "block";
    MsRdpClient.Server = server;
    MsRdpClient.UserName = username;
    MsRdpClient.AdvancedSettings2.ClearTextpassword = password;
    if (fullscreen) {
	MsRdpClient.FullScreen = true;
	MsRdpClient.DesktopWidth = screen.width;
	MsRdpClient.DesktopHeight = screen.height;
	MsRdpClient.Width = screen.width;
	MsRdpClient.Height = screen.height;
    } else {
	MsRdpClient.FullScreen = false;
    }
    MsRdpClient.FullScreenTitle = "vBox Connection";
    MsRdpClient.ConnectedStatusText = "Stay Connected";
    MsRdpClient.ColorDepth = 24;
    MsRdpClient.AdvancedSettings2.PerformanceFlags = 0x190;
    MsRdpClient.AdvancedSettings2.RedirectDrives = false;
    MsRdpClient.AdvancedSettings2.RedirectPrinters = false;
    MsRdpClient.AdvancedSettings2.RedirectClipboard = true;
    MsRdpClient.AdvancedSettings2.RedirectSmartCards = false;
    MsRdpClient.AdvancedSettings2.RedirectDirectX = false;
    MsRdpClient.AdvancedSettings2.MinutesToIdleTimeout = 1;
    MsRdpClient.Connect();
}

function onDisc(disconnectCode) {
    console.log("ondisc " + disconnectCode);
    window.location.reload();
}

$(document).ready(function() {
    console.log("document ready");
    if (typeof (MsRdpClient) === 'undefined' || MsRdpClient === null) {
	console.log('Unable to load ActiveX');
    } else {
	if (MsRdpClient.readyState == 4) {
	    console.log("control ready");
	}
	// attach events
	MsRdpClient.attachEvent('OnDisconnected', onDisc);
    }
});
