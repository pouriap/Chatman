package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import org.json.JSONObject;

public class PingMessage extends HiddenMessage{

	private final String senderTheme;

	public PingMessage(String senderTheme){
		this.senderTheme = senderTheme;
	}

	@Override
	public CMType getType(){
		return CMType.PING;
	}

	@Override
	public String getAsJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", CMType.PING);
		json.put("sender_theme", senderTheme);
		return json.toString();
	}
}
