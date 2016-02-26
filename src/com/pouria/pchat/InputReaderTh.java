/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author PouriaP
 */
//is responsible for reading input streams
//we need a thread for input and not for output
//is created from chatmanclient or chatmanserver
public class InputReaderTh implements Runnable{
    
    private Socket socket;
    private final ChatFrame gui;
    private String line; //baraye inke tooye anonymous class faghat be variable haye kelasy ke 
                         //anonymous tooye oone dastresi darim va be variable haye final
    
    //when we are client
    InputReaderTh(ChatFrame g, Socket s){
        gui = g;
        socket = s;
    }
    //when we are server
    InputReaderTh(ChatFrame g){
        gui = g;
    }

    public void run(){
        BufferedReader reader = null;
        boolean c = true;
        
        //client uses socket input stream
        if (gui.getChatmanInstance().getMode() == Chatman.MOD_CLIENT){
            try{
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            }catch(IOException e){
                gui.message("could not open socket input stream: " + e.getMessage());
                c = false;
            }
            
        }
        //server uses stdin
        else{
            reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            if(reader == null){
                gui.message("could not open STDIN");
                c = false;
            }
        }
        
        try{
            while(c){  
                line = reader.readLine();
                //unexpected close
                if(line == null){
                    //agar hanuz hidden ast faghat kharej sahvim
                    if(gui.isHidden())
                        gui.exit();
                    
                    int o = JOptionPane.showConfirmDialog(null, "اتصال قطع شد. خروج؟", "اتصال قطع شد", JOptionPane.YES_NO_OPTION);
                    if(o == JOptionPane.YES_OPTION){ 
                        c = false; 
                    }
                    else{
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                gui.setLabelStatus("اتصال قطع شد!");
                            }
                        });
                        return;
                    }
                }
                //exit message
                else if(line.equals(Chatman.SPECIAL_BYE)){
                    //agar hanuz hidden ast faghat kharej sahvim
                    if(gui.isHidden())
                        gui.exit();
                    
                    int o = JOptionPane.showConfirmDialog(null, "طرف مقابل از برنامه خارج شد. خروج؟", "خروج", JOptionPane.YES_NO_OPTION);
                    if(o == JOptionPane.YES_OPTION){
                        c = false; 
                    }
                    else{
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                gui.setLabelStatus("اتصال قطع شد!");
                            }
                        });
                        return;
                    }
                }
                //file message
                else if(line.equals(Chatman.SPECIAL_FILE)){
                        String f = reader.readLine();
                        final String fileName = new String(BaseEncoding.base64().decode(f), Charsets.UTF_8);
                        String fileData = reader.readLine();
                        final String location = System.getProperty("user.home") + "\\My Documents\\Chatman Downloads\\";
                        try{
                            File saveDir = new File(location);
                            if(!saveDir.isDirectory())
                                saveDir.mkdir();
                            Files.write(BaseEncoding.base64().decode(fileData), new File(location + fileName));
                            
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    gui.updateChatText("فایل دریافت شده: " + fileName + " - ذخیره شد در file://" + location + fileName);
                                }
                            });
                            
                        }catch(IOException e){
                            gui.message("could not save received file: " + e.getMessage());
                        }
                }
                //normal message
                else{
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            gui.updateChatText(line + "\n");
                        }
                    });
                }
            }//end of while
        }catch(IOException e){
            gui.message("closing applicaation. could not read input stream: " + e.getMessage());
        }
        //che exception rokh bede ya nade(while tamum she) ya exceptioni bashe ke catch nashode in code ejra mishe
        finally{
            try{
                if(socket != null)
                    socket.close();
                if(reader != null)
                    reader.close();
            }catch(IOException e){
                gui.message("could not close the streams: "+e.getMessage());
            }
        }
        gui.exit();
    }//end of run()
    
}
