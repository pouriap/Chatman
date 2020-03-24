package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public class TextMessage extends DisplayableMessage {


	public TextMessage(Direction direction, String sender, String content, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
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
