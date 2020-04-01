/*
 * Copyright (c) 2020. Pouria Pirhadi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.pouria.chatman.messages;

import com.pouria.chatman.MessageDisplayer;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DisplayableMessage extends CMMessage {

	private final String sender;
	private final String content;
	private final String senderTheme;
	private final long time;

	private boolean isSaved = false;

	public DisplayableMessage(Direction direction, String sender, String content, String senderTheme, long time) {
		super(direction);
		this.sender = sender;
		this.content = content;
		this.senderTheme = senderTheme;
		this.time = time;
	}


	//each displayable message is displayed differently
	//so each subclass provides its own displayable content
	protected abstract String getDisplayableContent();


	@Override
	public String getAsJSONString(){
		JSONObject json = new JSONObject();
		// we use getter methods so that all of them ca be overridable by child classes
		json.put("type", getType());
		json.put("content", getContent());
		json.put("sender", getSender());
		json.put("sender_theme", getSenderTheme());
		json.put("time", getTime());
		return json.toString();
	}

	@Override
	protected void doOnReceive() {
		MessageDisplayer displayer = new MessageDisplayer(this);
		displayer.display();
	}

	@Override
	protected void doOnSend() {
		MessageDisplayer displayer = new MessageDisplayer(this);
		displayer.display();
	}

	public String getAsHTMLString(){

		Date d = new Date(getTime());
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		String timeTxt = dateFormat.format(d);
		String messageHTML = getDisplayableContent();

		String senderNameClass = (getStatus() == Status.SENDFAIL)? "username-unsent" : "username-sent";
		String senderName = (getDirection() == Direction.OUT)? "You" : getSender();

		//each message is a div
		messageHTML =
			"<div class='message-div'>" +
				"<span class='time'>[" + timeTxt + "]  |  </span>" +
				"<b class='" + senderNameClass + "'>" + senderName + ":</b> " +
				messageHTML +
			"</div>";


		return messageHTML;
	}

	public String getSender(){
		return sender;
	}

	public String getContent(){
		return content;
	}

	public String getSenderTheme() {
		return senderTheme;
	}

	public long getTime() {
		return time;
	}

	public boolean isSaved() {
		return isSaved;
	}

	public void setIsSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}

}
