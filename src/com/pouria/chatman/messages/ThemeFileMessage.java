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
import com.pouria.chatman.gui.CMTheme;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Base64;

public class ThemeFileMessage extends HiddenMessage {

	private final String themeName;
	private final String themeDataBase64;

	private ThemeFileMessage(Direction direction, String themeName, String themeDataBase64) {
		super(direction);
		this.themeName = themeName;
		this.themeDataBase64 = themeDataBase64;
	}

	public static ThemeFileMessage getNewOutgoing(String themeName, String themeDataBase64){
		return new ThemeFileMessage(Direction.OUT, themeName, themeDataBase64);
	}

	public static ThemeFileMessage getNewIncoming(JSONObject json) throws JSONException {
		String themeName = json.getString("theme_name");
		String themeDataBase64 = json.getString("theme_data_base64");
		return new ThemeFileMessage(Direction.IN, themeName, themeDataBase64);
	}

	@Override
	public CMType getType() {
		return CMType.THEME_FILE;
	}

	@Override
	public String getAsJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", CMType.THEME_FILE);
		json.put("theme_name", themeName);
		json.put("theme_data_base64", themeDataBase64);
		return json.toString();
	}

	@Override
	public void doOnReceive(){

		try{

			File themeFileToSave = new File(
					CMConfig.getInstance().get("themes-dir", CMConfig.DEFAULT_THEMES_DIR) + "\\" + themeName
			);

			byte[] themeFileData = Base64.getDecoder().decode(themeDataBase64);
			CMHelper.getInstance().createFile(themeFileToSave, themeFileData);

			CMTheme peerTheme = new CMTheme(themeFileToSave.getAbsolutePath());
			ChatFrame.getInstance().setPeerTheme(peerTheme);
			ChatFrame.getInstance().showNewMessagePopup();

		}catch(Exception e){
			CMHelper.getInstance().log("failed to receive peer theme: " + e.getMessage());
			ChatFrame.getInstance().setPeerTheme(CMTheme.getDefaultTheme());
		}

	}

	@Override
	public void doOnSend(){
		//nothing
	}
}
