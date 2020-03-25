package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

public class PingMessage extends HiddenMessage{

	private final String senderTheme;

	private PingMessage(Direction direction, String senderTheme){
		super(direction);
		this.senderTheme = senderTheme;
	}

	public static PingMessage getNew(Direction direction, String senderTheme){
		return new PingMessage(direction, senderTheme);
	}

	public static PingMessage getNewOutgoing(){
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		return new PingMessage(Direction.OUT, senderTheme);
	}

	public static PingMessage getNewIncoming(JSONObject json) throws JSONException {
		String senderTheme = json.getString("sender_theme");
		return new PingMessage(Direction.IN, senderTheme);
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
