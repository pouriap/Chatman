package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import org.json.JSONObject;

public class ShowGUIMessage extends HiddenMessage {

	@Override
	public CMType getType() {
		return CMType.SHOWGUI;
	}

	@Override
	public String getAsJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", CMType.SHOWGUI);
		return json.toString();
	}

}
