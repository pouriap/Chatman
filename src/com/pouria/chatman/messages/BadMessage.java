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

import com.pouria.chatman.CMConfig;
import com.pouria.chatman.CMHelper;
import com.pouria.chatman.enums.CMType;

public class BadMessage extends DisplayableMessage {

	public BadMessage(String content) {
		super(Direction.UKNOWN, "Bad Message", content, CMConfig.DEFAULT_THEME, System.currentTimeMillis());
	}

	@Override
	public CMType getType() {
		return CMType.BADMESSAGE;
	}

	@Override
	public String getDisplayableContent() {
		return getContent();
	}

	@Override
	public void doOnReceive(){
		CMHelper.getInstance().log("bad message received: " + getContent());
		super.doOnReceive();
	}

	@Override
	public void doOnSend(){
		//nothing
	}
}
