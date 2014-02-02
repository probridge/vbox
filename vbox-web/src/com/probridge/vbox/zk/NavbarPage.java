package com.probridge.vbox.zk;

import java.io.Serializable;

public class NavbarPage implements Serializable {
	private static final long serialVersionUID = 1L;
	String name;
	String label;
	String iconUri;
	String uri;

	public NavbarPage(String name, String label, String iconUri,
			String uri) {
		this.name = name;
		this.label = label;
		this.iconUri = iconUri;
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getIconUri() {
		return iconUri;
	}

	public String getUri() {
		return uri;
	}
}