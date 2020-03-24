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
	private final int popupRightOffset;
	private final int popupBottomOffset;
	private final String userName;
	private final String buttonsTheme;
	private final String textAreasTheme;
	
	private final byte[] popupData;
	
	public CMTheme(String themeFilePath) throws Exception{
		
		file = new File(themeFilePath);
		
		//try faghat baraye ine ke zipFile ro bebande age exception shod ya nashod
		try(ZipFile zipFile = new ZipFile(file)){
			
			ZipEntry jsonFile = zipFile.getEntry("data.json");
			InputStream jsonIn = zipFile.getInputStream(jsonFile);
			byte[] jsonData = CMHelper.readStreamAsByteArray(jsonIn);
			String jsonString = new String(jsonData);

			JSONObject json = new JSONObject(jsonString);
			String bgFilename = json.getString("bg-image");
			String popupFilename = json.getString("popup-image");
			popupRightOffset = json.getInt("popup-right-offset");
			popupBottomOffset = json.getInt("popup-bottom-offset");
			userName = json.getString("username");
			buttonsTheme = json.getString("buttons-theme");
			textAreasTheme = json.getString("textareas-theme");

			ZipEntry bgFile = zipFile.getEntry(bgFilename);
			InputStream bgIn = zipFile.getInputStream(bgFile);
			byte[] bgData = CMHelper.readStreamAsByteArray(bgIn);
			bgImage = new ImageIcon(bgData);

			ZipEntry popupFile = zipFile.getEntry(popupFilename);
			InputStream popupIn = zipFile.getInputStream(popupFile);
			popupData = CMHelper.readStreamAsByteArray(popupIn);
		}
		
	}
	
	public ImageIcon getBgImage() {
		return bgImage;
	}

	public ImageIcon getPopupImage() {
		//baraye inke gif haii ke yek repeat darad reset shavand
		return new ImageIcon(popupData);
	}

	public int getPopupRightOffset() {
		return popupRightOffset;
	}

	public int getPopupBottomOffset() {
		return popupBottomOffset;
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
