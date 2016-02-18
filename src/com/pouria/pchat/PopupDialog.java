/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author SH
 */
public class PopupDialog extends JDialog{
    private AudioInputStream audioStream;
    private Clip clip;

    //plays the sound
    public void playSound(){
        try{ 
            audioStream = AudioSystem.getAudioInputStream(getClass().getResource("/resources/notification.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            return;
            
        }catch(NullPointerException e){
            JOptionPane.showMessageDialog(null, "could not open audio stream: "+e.getMessage());
            return;
        }catch(IOException e){
            JOptionPane.showMessageDialog(null, "cold not open audio file: "+e.getMessage());
            return;
        }catch(IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, "format not supported: "+e.getMessage());
            return;
        }
        catch(LineUnavailableException e){
            JOptionPane.showMessageDialog(null, "line unavailable: "+e.getMessage());
            return;
        }catch(UnsupportedAudioFileException e){
            JOptionPane.showMessageDialog(null, "unsupported audio file"+e.getMessage());
            return;
        }
    }

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
    
    public void showPopup(int x, int y, boolean undecorated, boolean hideMainFrame){
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
    
    public void hidePopup(){
        this.setVisible(false);
    }
    
    public void closePopup(){
        try{
            if(this.audioStream != null)
                this.audioStream.close();
            if(this.clip != null)
                this.clip.close();
        }catch(IOException e){
            System.out.println("could not close audio stream: "+e.getMessage());
        }catch(NullPointerException e){
            System.out.println("could not close handles in popup: "+e.getMessage());
        }
        finally{
            this.dispose();
        }
        
    }
    
}
    



    

