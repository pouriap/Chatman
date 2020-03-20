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
import com.pouria.chatman.classes.CmdInvokeLater;
import com.pouria.chatman.classes.CmdShowError;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author Pouria
 */
public class NotificationPopup{
	
	private final int os;
	private final JDialog dialog;
	
	public NotificationPopup(ImageIcon image) {
			
		dialog = new JDialog();
		dialog.setSize(image.getIconWidth(), image.getIconHeight());
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.getRootPane().setOpaque(false);
		dialog.setBackground(new Color(0,0,0,0));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        dialog.setLocation(
                screenWidth - dialog.getWidth() - 10, 
                screenHeight - dialog.getHeight() - 50
        );
		
		JLabel label = new JLabel();
        label.setIcon(image);
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
	
	//@Override
	public void show(){
		dialog.setVisible(true);
	}
	
	//@Override
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
			if(os == CMHelper.OS_WIN){
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
			String error = CMHelper.getInstance().getStr("audio_play_fail") + e.getMessage();
            (new CmdInvokeLater(new CmdShowError(error))).execute();
        }
    }
	
}
