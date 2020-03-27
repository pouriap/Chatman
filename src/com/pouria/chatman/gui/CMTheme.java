/*
 * Copyright (C) 2020 Pouria
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.gui;

import com.pouria.chatman.CMConfig;
import com.pouria.chatman.CMHelper;
import com.pouria.chatman.commands.CmdFatalErrorExit;
import com.pouria.chatman.commands.CmdInvokeLater;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author Pouria
 */
public class CMTheme {
	
	private final File file;
	private final ImageIcon bgImage;
	private final int popupRightMargin;
	private final int popupBottomMargin;
	private final String userName;
	private final String buttonsTheme;
	private final String textAreasTheme;
	
	private final byte[] popupData;
	private final byte[] soundData;

	private final static String USERNAME = "username";
	private final static String BG_IMAGE = "bg-image";
	private final static String POPUP_IMAGE = "notification-image";
	private final static String POPUP_SOUND = "notification-sound";
	private final static String POPUP_MARGIN_RIGHT = "notification-margin-right";
	private final static String POPUP_MARGIN_BOTTOM = "notification-margin-bottom";
	private final static String BUTTONS_THEME = "buttons-theme";
	private final static String TEXTAREAS_THEME = "textareas-theme";

	private final static String DEFAULT_POPUP_IMAGE = "default_popup_image.gif";
	private final static String DEFAULT_POPUP_SOUND = "default_popup_sound.wav";
	private final static int DEFAULT_POPUP_MARGIN_RIGHT = 10;
	private final static int DEFAULT_POPUP_MARGIN_BOTTOM = 0;

	//todo: add option to override popup sound and image

	public CMTheme(String themeFilePath) throws Exception{
		
		file = new File(themeFilePath);
		
		//try faghat baraye ine ke zipFile ro bebande age exception shod ya nashod
		try(ZipFile zipFile = new ZipFile(file)){
			
			ZipEntry jsonFile = zipFile.getEntry("data.json");
			InputStream jsonIn = zipFile.getInputStream(jsonFile);
			byte[] jsonData = CMHelper.readStreamAsByteArray(jsonIn);
			String jsonString = new String(jsonData);

			JSONObject json = new JSONObject(jsonString);
			//mandatory properties
			userName = json.getString(USERNAME);
			String bgFilename = json.getString(BG_IMAGE);
			//optional properties
			String popupFilename = json.has(POPUP_IMAGE)?
					json.getString(POPUP_IMAGE) : "";
			String soundFilename = json.has(POPUP_SOUND)?
					json.getString(POPUP_SOUND) : "";
			popupRightMargin = json.has(POPUP_MARGIN_RIGHT)?
					json.getInt(POPUP_MARGIN_RIGHT) :
					json.has(POPUP_IMAGE)? 0 : DEFAULT_POPUP_MARGIN_RIGHT;
			popupBottomMargin = json.has(POPUP_MARGIN_BOTTOM)?
					json.getInt(POPUP_MARGIN_BOTTOM) :
					json.has(POPUP_IMAGE)? 0 : DEFAULT_POPUP_MARGIN_BOTTOM;
			buttonsTheme = json.has(BUTTONS_THEME)?
					json.getString(BUTTONS_THEME) : "dark";
			textAreasTheme = json.has(TEXTAREAS_THEME)?
					json.getString(TEXTAREAS_THEME) : "dark";

			ZipEntry bgFile = zipFile.getEntry(bgFilename);
			InputStream bgIn = zipFile.getInputStream(bgFile);
			byte[] bgData = CMHelper.readStreamAsByteArray(bgIn);
			bgImage = new ImageIcon(bgData);

			ZipEntry popupFile = zipFile.getEntry(popupFilename);
			if(popupFile != null) {
				InputStream popupIn = zipFile.getInputStream(popupFile);
				popupData = CMHelper.readStreamAsByteArray(popupIn);
			}
			else{
				InputStream popupIn = getClass().getResourceAsStream("/resources/" + DEFAULT_POPUP_IMAGE);
				popupData = CMHelper.readStreamAsByteArray(popupIn);
			}

			ZipEntry soundFile = zipFile.getEntry(soundFilename);
			if(soundFile != null) {
				InputStream soundIn = zipFile.getInputStream(soundFile);
				soundData = CMHelper.readStreamAsByteArray(soundIn);
			}
			else{
				InputStream soundIn = getClass().getResourceAsStream("/resources/" + DEFAULT_POPUP_SOUND);
				soundData = CMHelper.readStreamAsByteArray(soundIn);
			}

		}
		
	}
	
	public ImageIcon getBgImage() {
		return bgImage;
	}
	
	public ImageIcon getPopupImage(){
		//to re-draw one-loop gifs
		return new ImageIcon(popupData);
	}

	public byte[] getPopupImageBytes() {
		return popupData;
	}

	public byte[] getSoundFileBytes(){
		return soundData;
	}

	public int getPopupRightMargin() {
		return popupRightMargin;
	}

	public int getPopupBottomMargin() {
		return popupBottomMargin;
	}
	
	public String getUsername(){
		return userName;
	}

	public String getButtonsTheme() {
		return buttonsTheme;
	}

	public String getTextAreasTheme() {
		return textAreasTheme;
	}
	
	public String getFileName(){
		return file.getName();
	}
	
	public String getDataBase64(){
		try{
			byte[] data = Files.readAllBytes(file.toPath());
			return Base64.getEncoder().encodeToString(data);
		}catch(Exception e){
			CMHelper.getInstance().log("failed to base64 encode theme: " + file.getName());
			return "";
		}
	}

	public static CMTheme getDefaultTheme(){
		try{
			
			String defaultThemesDir = CMConfig.getInstance().get("themes-dir", CMConfig.DEFAULT_THEMES_DIR);
			File themeFile = new File(defaultThemesDir + "\\" + CMConfig.DEFAULT_THEME);
			CMHelper.getInstance().copyFromResources("default_theme.cmtheme", themeFile);
			return new CMTheme(themeFile.getAbsolutePath());
			
		}catch(Exception e){
			String message = "failed to get default theme";
			(new CmdInvokeLater(new CmdFatalErrorExit(message, e))).execute();
			return null;
		}
	}

	public static CMTheme getFromDefaultDir(String themeName) throws Exception{
		String defaultThemesDir = CMConfig.getInstance().get("themes-dir", CMConfig.DEFAULT_THEMES_DIR);
		String themePath = defaultThemesDir + "\\" + themeName;
		return new CMTheme(themePath);
	}
	
}
