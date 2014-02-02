
package com.probridge.vbox.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.net.InetGuacamoleSocket;
import org.glyptodon.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.glyptodon.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.glyptodon.guacamole.servlet.GuacamoleSession;

import com.probridge.vbox.VBoxConfig;
import com.probridge.vbox.utils.Utility;

/*
 *  Guacamole - Clientless Remote Desktop
 *  Copyright (C) 2010  Michael Jumper
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class RemoteDesktopTunnelServlet extends GuacamoleHTTPTunnelServlet {

	private static final long serialVersionUID = 6892795465933568450L;

	@Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {

        HttpSession httpSession = request.getSession(true);

        // guacd connection information
        String hostname = VBoxConfig.gatewayServerName;
        int port = Integer.parseInt(VBoxConfig.gatewayServerPort);

		Session sess = SecurityUtils.getSubject().getSession();		
		String rdpHost = Utility.getStringVal(sess.getAttribute("rdp_target"));
		String rdpUserName = Utility.getStringVal(sess.getAttribute("rdp_username"));
		String rdpPassword = Utility.getStringVal(sess.getAttribute("rdp_password"));

        // RDP connection information
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol("rdp");
        config.setParameter("hostname", rdpHost);
        config.setParameter("port", "3389");
        config.setParameter("username", rdpUserName);
        config.setParameter("password", rdpPassword);
        config.setParameter("width", request.getParameter("width"));
        config.setParameter("height", request.getParameter("height"));

        // Connect to guacd, proxying a connection to the VNC server above
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(hostname, port),
                config
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new GuacamoleTunnel(socket);

        // Attach tunnel
        GuacamoleSession session = new GuacamoleSession(httpSession);
        session.attachTunnel(tunnel);

        return tunnel;

    }

}
