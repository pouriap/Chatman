package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

public class TextMessage extends DisplayableMessage {


	private TextMessage(Direction direction, String sender, String content, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
	}

	public static TextMessage getNew(Direction direction, String sender, String content, String senderTheme, long time){
		return new TextMessage(direction, sender, content, senderTheme, time);
	}

	public static TextMessage getNewOutgoing(String content){
		Direction direction = Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new TextMessage(direction, sender, content, senderTheme, time);
	}

	public static TextMessage getNewIncoming(JSONObject json) throws JSONException {
		String content = json.getString("content");
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return TextMessage.getNew(CMMessage.Direction.IN, sender, content, senderTheme, time);
	}

	@Override
	public CMType getType() {
		return CMType.TEXT;
	}

	@Override
	public String getDisplayableContent(){
		String text = getContent();
		//parse web links
		text = text.replaceAll("((http|https)://[^\\s]*)\\s?", "<a style='color:#dee3e9;font-weight:bold;' href='$1'><u>$1</u></a> ");
		//parse emoticons
		String url = getClass().getResource("/resources/emoticons_large/").toString();
		text = text.replaceAll("src=\"[^\"]*emoticons_large\\/([^\"]*\\.gif)\"", "src=\"" + url + "$1\"");

		return text;
	}

}
