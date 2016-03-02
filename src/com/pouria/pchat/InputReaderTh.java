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
                (new CommandInvokeLater(new CommandMessage("could not open socket input stream: " + e.getMessage()))).execute();
                c = false;
            }
            
        }
        //server uses stdin
        else{
            reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            if(reader == null){
                (new CommandInvokeLater(new CommandMessage("could not open STDIN"))).execute();
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
                        c = false;
                    
                    (new CommandInvokeLater(new Command[]{
                        new CommandSetLabelStatus(gui, "اتصال قطع شد"), 
                        new CommandEndSession(gui, "اتصال قطع شد")
                    })).execute();

                    return;
                    
                }
                //exit message
                else if(line.equals(Chatman.SPECIAL_BYE)){
                    //agar hanuz hidden ast faghat kharej sahvim
                    if(gui.isHidden())
                        c = false;
                    
                    (new CommandInvokeLater(new Command[]{
                        new CommandSetLabelStatus(gui, "اتصال قطع شد"), 
                        new CommandEndSession(gui, "طرف مقابل از برنامه خارج شد")
                    })).execute();
                    
                    return;
                    
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
                            
                            (new CommandInvokeLater(new CommandUpdateIncomingText(gui, "فایل دریافت شده: " + fileName + " - ذخیره شد در file://" + location + fileName))).execute();

                            
                        }catch(IOException e){
                            (new CommandInvokeLater(new CommandMessage("could not save received file: " + e.getMessage()))).execute();
                        }
                }
                //normal message
                else{
                    (new CommandInvokeLater(new CommandUpdateIncomingText(gui,line))).execute();
                }
            }//end of while
        }catch(IOException e){
            (new CommandInvokeLater(new CommandMessage("closing applicaation. could not read input stream: " + e.getMessage()))).execute();
        }
        //che exception rokh bede ya nade(while tamum she) ya exceptioni bashe ke catch nashode in code ejra mishe
        finally{
            try{
                if(socket != null)
                    socket.close();
                if(reader != null)
                    reader.close();
            }catch(IOException e){
                (new CommandInvokeLater(new CommandMessage("could not close the streams: "+e.getMessage()))).execute();
            }
        }
        gui.exit();
    }//end of run()
    
}
