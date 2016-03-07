/*
 * Copyright (C) 2016 Pouria Pirhadi
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

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author pouriap
 * 
 * a dialog that pops up and makes a bleep
 * we don't use some of it's functions in this application
 */
public class PopupDialog extends JDialog{
    private AudioInputStream audioStream;
    private Clip clip;

    //plays the bleep
    //this does not work on linux for some reason! eventhough it's .wav
    public void playSound(){
        try{ 
            audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/resources/notification.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            return;
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, ChatFrame.getInstance().l.getString("audio_play_fail") + e.getMessage());
            return;
        }
    }

    //shows the popup dialog in bottom-right corner of the screen and hides window decorations of it
    public void showPopup(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        this.setLocation(
                (screenWidth - this.getWidth()) - 20, 
                (screenHeight - this.getHeight()) - 100
        );
        this.setUndecorated(true);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }
    
    //fancy version of the previous function
    public void showPopup(int x, int y, boolean undecorated){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        this.setLocation(
                (screenWidth - this.getWidth()) - 20, 
                (screenHeight - this.getHeight()) - 100
        );
        this.setUndecorated(undecorated);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }
    
    //gues what?
    public void hidePopup(){
        this.setVisible(false);
    }
    
    /*
    //useless
    public void closePopup(){
        try{
            if(this.audioStream != null)
                this.audioStream.close();
            if(this.clip != null)
                this.clip.close();
        }catch(Exception e){
            ChatFrame.getInstance().message(ChatFrame.getInstance().l.getString("audio_close_fail") + e.getMessage());
        }
        finally{
            this.dispose();
        }
        
    }
    */
    
}
    



    

