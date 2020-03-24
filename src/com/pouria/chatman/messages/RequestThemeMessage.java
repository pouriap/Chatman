package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import org.json.JSONObject;

public class RequestThemeMessage extends HiddenMessage {

	private final String themeName;

	public RequestThemeMessage(String themeName) {
		this.themeName = themeName;
	}

	@Override
	public CMType getType() {
		return CMType.REQUEST_THEME_FILE;
	}

	@Override
	public String getAsJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", CMType.REQUEST_THEME_FILE);
		json.put("theme_name", themeName);
		return json.toString();
	}
}
