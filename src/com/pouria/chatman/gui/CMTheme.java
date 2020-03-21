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
	
	private final ImageIcon bgImage;
	private final CMImageIcon popupImage;
	private final int popupRightOffset;
	private final int popupBottomOffset;

	public CMTheme(String filePath) throws Exception{

		ZipFile themeFile = new ZipFile(filePath);

		ZipEntry dataFile = themeFile.getEntry("data.json");
		InputStreamReader r = new InputStreamReader(themeFile.getInputStream(dataFile));
		StringBuilder b = new StringBuilder();
		while(r.ready()){
			char[] buff = new char[1024];
			r.read(buff);
			b.append(buff);
		}
		String jsonString = b.toString();

		JSONObject json = new JSONObject(jsonString);
		String bgFilename = json.getString("background-image");
		String popupFilename = json.getString("popup-image");
		popupRightOffset = json.getInt("popup-right-offset");
		popupBottomOffset = json.getInt("popup-bottom-offset");

		ZipEntry bgFile = themeFile.getEntry(bgFilename);
		BufferedImage bgBuff = ImageIO.read(themeFile.getInputStream(bgFile));
		bgImage = new ImageIcon(bgBuff);

		ZipEntry popupFile = themeFile.getEntry(popupFilename);
		InputStream in = themeFile.getInputStream(popupFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len = 0;
		byte[] buff = new byte[2048];
		while((len = in.read(buff)) > 0){
			out.write(buff, 0, len);
		}
		popupImage = new CMImageIcon(out.toByteArray());
		
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
	
	
}
