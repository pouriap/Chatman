package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import org.json.JSONException;
import org.json.JSONObject;

public class ThemeFileMessage extends HiddenMessage {

	private final String themeName;
	private final String themeDataBase64;

	private ThemeFileMessage(Direction direction, String themeName, String themeDataBase64) {
		super(direction);
		this.themeName = themeName;
		this.themeDataBase64 = themeDataBase64;
	}

	public static ThemeFileMessage getNewOutgoing(String themeName, String themeDataBase64){
		return new ThemeFileMessage(Direction.OUT, themeName, themeDataBase64);
	}

	public static ThemeFileMessage getNewIncoming(JSONObject json) throws JSONException {
		String themeName = json.getString("theme_name");
		String themeDataBase64 = json.getString("theme_data_base64");
		return new ThemeFileMessage(Direction.IN, themeName, themeDataBase64);
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
