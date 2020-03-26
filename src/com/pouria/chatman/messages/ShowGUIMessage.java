package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONObject;

public class ShowGUIMessage extends HiddenMessage {

	private ShowGUIMessage(Direction direction) {
		super(direction);
	}

	public static ShowGUIMessage getNewOutgoing(){
		return new ShowGUIMessage(Direction.OUT);
	}

	public static ShowGUIMessage getNewIncoming(){
		return new ShowGUIMessage(Direction.IN);
	}

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

	@Override
	public void doOnReceive(){
		ChatFrame.getInstance().showWindow();
	}

	@Override
	public void doOnSend(){
		//nothing
	}

}
