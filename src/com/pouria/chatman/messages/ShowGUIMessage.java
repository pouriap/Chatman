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
