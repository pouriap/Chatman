package com.pouria.chatman.messages;

import com.pouria.chatman.OutgoingMsgHandler;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestThemeMessage extends HiddenMessage {

	private final String themeName;

	private RequestThemeMessage(Direction direction, String themeName) {
		super(direction);
		this.themeName = themeName;
	}

	public static RequestThemeMessage getNewOutgoing(String themeName){
		return new RequestThemeMessage(Direction.OUT, themeName);
	}

	public static RequestThemeMessage getNewIncoming(JSONObject json) throws JSONException {
		String themeName = json.getString("theme_name");
		return new RequestThemeMessage(Direction.IN, themeName);
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

	@Override
	public void doOnReceive(){
		String themeDataBase64 = ChatFrame.getInstance().getCurrentTheme().getDataBase64();
		String themeName =ChatFrame.getInstance().getCurrentTheme().getFileName();
		ThemeFileMessage message = ThemeFileMessage.getNewOutgoing(themeName, themeDataBase64);
		OutgoingMsgHandler handler = new OutgoingMsgHandler(message);
		handler.sendAsync();
	}

	@Override
	public void doOnSend(){
		//nothing
	}
}
