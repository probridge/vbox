package com.probridge.vbox.fileupload;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

public class FileNameSorter implements Comparator<JSONObject> {

	@Override
	public int compare(JSONObject arg0, JSONObject arg1) {
		try {
			if (arg0.getLong("size") == -1L && arg1.getLong("size") > -1L)
				return -1;
			if (arg0.getLong("size") > -1L && arg1.getLong("size") == -1L)
				return 1;
			return arg0.getString("name").compareToIgnoreCase(arg1.getString("name"));
		} catch (JSONException e) {
			return 0;
		}
	}
}