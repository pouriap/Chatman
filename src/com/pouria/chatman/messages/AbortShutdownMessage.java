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

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AbortShutdownMessage extends DisplayableMessage {

	private final static String content = "[ABORT SHUTDOWN]";

	private AbortShutdownMessage(Direction direction, String sender, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
	}

	public static AbortShutdownMessage getNew(Direction direction, String sender, String senderTheme, long time) {
		return new AbortShutdownMessage(direction, sender, senderTheme, time);
	}

	public static AbortShutdownMessage getNewOutgoing(){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new AbortShutdownMessage(direction, sender, senderTheme, time);
	}

	public static AbortShutdownMessage getNewIncoming(JSONObject json) throws JSONException{
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new AbortShutdownMessage(CMMessage.Direction.IN, sender, senderTheme, time);
	}

	@Override
	public CMType getType() {
		return CMType.ABORT_SHUTDOWN;
	}

	@Override
	public String getDisplayableContent() {
		return getContent();
	}

	@Override
	public void doOnReceive() {

		String info;
		CMHelper.getInstance().log("remote-abort-shutdown received");
		try {
			CMHelper.getInstance().abortLocalShutdown();
			CMHelper.getInstance().log("shutdown aborted successfully");
			info = "[SHUTDOWN ABORTED SUCCESSFULLY]";
		} catch (IOException e) {
			CMHelper.getInstance().log("abort failed");
			info = "[ERROR: COULD NOT ABORT THE SHUTDOWN]";
		}
		//tell the other computer if abort was successfull
		TextMessage msg = TextMessage.getNewOutgoing(info);
		ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);

		super.doOnReceive();
	}

}
