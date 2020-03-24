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

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;

/**
 *
 * @author Pouria
 */
public class CMNotifPopup{
	
	private final CMOS os;
	private final JDialog dialog;
	private final CMTheme theme;
	private final JLabel label;
	
	public CMNotifPopup(CMTheme theme) {

		this.theme = theme;
		ImageIcon popupImage = theme.getPopupImage();
		int widht = popupImage.getIconWidth();
		int height = popupImage.getIconHeight();
		int rightOffset = theme.getPopupRightOffset();
		int bottomOffset = theme.getPopupBottomOffset();
		
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
                screenHeight - dialog.getHeight() - bottomOffset
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
			AudioInputStream audioStream;
			Clip clip;
			
			//linux sucks so...
			if(os == CMOS.WINDOWS){
				audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/resources/notification-bat.wav"));
				clip = AudioSystem.getClip();
			}
			else{
				BufferedInputStream srcStream = new BufferedInputStream(getClass().getResourceAsStream("/resources/notification-bat.wav")); 
				audioStream = AudioSystem.getAudioInputStream(srcStream);
				AudioFormat format = audioStream.getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				clip = (Clip)AudioSystem.getLine(info);
			}

			clip.open(audioStream);
			clip.start();

        }catch(Exception e){
			CMHelper.getInstance().log("playing notification sound failed: " + e.getMessage());
			String error = CMHelper.getInstance().getStr("audio_play_fail");
            (new CmdInvokeLater(new CmdShowError(error))).execute();
        }
    }
	
}
