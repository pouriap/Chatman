package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import org.json.JSONObject;

public class ThemeFileMessage extends HiddenMessage {

	private final String themeName;
	private final String themeDataBase64;

	public ThemeFileMessage(String themeName, String themeDataBase64) {
		this.themeName = themeName;
		this.themeDataBase64 = themeDataBase64;
	}

	@Override
	public CMType getType() {
		return CMType.THEME_FILE;
	}

	@Override
	public String getAsJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", CMType.THEME_FILE);
		json.put("theme_name", themeName);
		json.put("theme_data_base64", themeDataBase64);
		return json.toString();
	}

	public String getThemeName() {
		return themeName;
	}

	public String getThemeDataBase64() {
		return themeDataBase64;
	}

}
