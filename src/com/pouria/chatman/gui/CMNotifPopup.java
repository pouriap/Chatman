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

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.commands.CmdShowError;
import com.pouria.chatman.enums.CMOS;
import com.sun.istack.internal.NotNull;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 *
 * @author Pouria
 */
public class CMNotifPopup{

	private final JDialog dialog;
	private final JLabel label;
	private final CMTheme theme;
	private final CMOS os;
	private AudioFormat audioFormat;
	private Clip clip;

	public CMNotifPopup(@NotNull CMTheme theme) {

		Objects.requireNonNull(theme);
		
		Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int taskBarHeight = scrnSize.height - winSize.height;

		ImageIcon popupImage = theme.getPopupImage();
		int widht = popupImage.getIconWidth();
		int height = popupImage.getIconHeight();
		int rightOffset = theme.getPopupRightMargin();
		int bottomOffset = theme.getPopupBottomMargin();

		dialog = new JDialog();
		dialog.setSize(widht, height);
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.getRootPane().setOpaque(false);
		dialog.setBackground(new Color(0,0,0,0));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int screenHeight = (int) screenSize.getHeight();
		dialog.setLocation(
				screenWidth - dialog.getWidth() - rightOffset,
				screenHeight - dialog.getHeight() - taskBarHeight - bottomOffset
		);
		
		label = new JLabel();
		label.setIcon(popupImage);
		label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		label.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				ChatFrame.getInstance().popopClicked(evt.getButton());
			}
		});
		
		dialog.add(label);
		
		this.os = CMHelper.getInstance().getOS();
		this.theme = theme;

	}

	public void show(){
		//to reload one-loop gif animations
		label.setIcon(theme.getPopupImage());
		dialog.setVisible(true);
	}
	
	public void hide(){
		dialog.dispose();
	}
	
	public boolean isVisible(){
		return dialog.isVisible();
	}

	//plays the bleep
	public void playSound(){

		try{

			//don't play sound if sound is already playing
			if(clip!=null &&(clip.isActive() || clip.isOpen() || clip.isRunning())){
				return;
			}

			if(audioFormat == null){
				setAudioFormat();
			}

			clip = getClip();
			clip.addLineListener(myLineEvent -> {
				if (myLineEvent.getType() == LineEvent.Type.STOP) {
					clip.close();
				}
			});

			clip.open(audioFormat, theme.getSoundFileBytes(), 0, theme.getSoundFileBytes().length);
			clip.start();

		}catch(Exception e){
			CMHelper.getInstance().log("playing notification sound failed: " + e.getMessage());
			String error = CMHelper.getInstance().getStr("audio_play_fail");
			(new CmdInvokeLater(new CmdShowError(error))).execute();
		}

	}

	public Clip getClip() throws Exception{
		if(os == CMOS.WINDOWS){
			return AudioSystem.getClip();
		}
		else{
			DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
			return (Clip)AudioSystem.getLine(info);
		}
	}

	private void setAudioFormat() throws Exception{
		ByteArrayInputStream is = new ByteArrayInputStream(theme.getSoundFileBytes());
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
		audioFormat = audioStream.getFormat();
		audioStream.close();
		is.close();
	}
	
}
