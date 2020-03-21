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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.json.JSONObject;

/**
 *
 * @author Pouria
 */
public class CMTheme {
	
	private final File file;
	private final ImageIcon bgImage;
	private final CMImageIcon popupImage;
	private final int popupRightOffset;
	private final int popupBottomOffset;
	private final String userName;
	private final String buttonsTheme;
	private final String textAreasTheme;

	public CMTheme(String themeFilePath) throws Exception{
		
		file = new File(themeFilePath);
		
		//try faghat baraye ine ke zipFile ro bebande age exception shod ya nashod
		try(ZipFile zipFile = new ZipFile(file)){
			
			ZipEntry dataFile = zipFile.getEntry("data.json");
			InputStreamReader r = new InputStreamReader(zipFile.getInputStream(dataFile));
			StringBuilder b = new StringBuilder();
			while(r.ready()){
				char[] buff = new char[1024];
				r.read(buff);
				b.append(buff);
			}
			String jsonString = b.toString();

			JSONObject json = new JSONObject(jsonString);
			String bgFilename = json.getString("bg-image");
			String popupFilename = json.getString("popup-image");
			popupRightOffset = json.getInt("popup-right-offset");
			popupBottomOffset = json.getInt("popup-bottom-offset");
			userName = json.getString("username");
			buttonsTheme = json.getString("buttons-theme");
			textAreasTheme = json.getString("textareas-theme");

			ZipEntry bgFile = zipFile.getEntry(bgFilename);
			BufferedImage bgBuff = ImageIO.read(zipFile.getInputStream(bgFile));
			bgImage = new ImageIcon(bgBuff);

			ZipEntry popupFile = zipFile.getEntry(popupFilename);
			InputStream in = zipFile.getInputStream(popupFile);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int len = 0;
			byte[] buff = new byte[2048];
			while((len = in.read(buff)) > 0){
				out.write(buff, 0, len);
			}
			popupImage = new CMImageIcon(out.toByteArray());
		}
		
	}
	
	public ImageIcon getBgImage() {
		return bgImage;
	}

	public CMImageIcon getPopupImage() {
		return popupImage;
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

	
}
