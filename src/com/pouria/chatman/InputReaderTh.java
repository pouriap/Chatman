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
package com.pouria.chatman;

import com.pouria.chatman.classes.CommandUpdateIncomingText;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.Command;
import com.pouria.chatman.classes.CommandMessage;
import com.pouria.chatman.classes.CommandEndSession;
import com.pouria.chatman.classes.CommandSetLabelStatus;
import com.pouria.chatman.gui.ChatFrame;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.pouria.chatman.classes.CommandConfirmDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;

/**
 *
 * @author pouriap
 * 
 * is responsible for reading input streams
 * input stream can be STDIN our SocketInputStream
 * is created from ChatmanClient or ChatmanServer
 */

public class InputReaderTh implements Runnable{
    
    private Socket socket;
    private final ChatFrame gui;
    private final int mode;
    private boolean inComa = false;
    
    //when we are client
    InputReaderTh(Socket s){
        gui = ChatFrame.getInstance();
        mode = Chatman.MOD_CLIENT;
        socket = s;
    }
    
    //when we are server
    InputReaderTh(){
        gui = ChatFrame.getInstance();
        mode = Chatman.MOD_SERVER;
    }

    @Override
    public void run(){
        boolean c = true;
        BufferedReader reader = null;
        
        //client uses socket input stream
        if (mode == Chatman.MOD_CLIENT){
            try{
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                
            }catch(IOException e){
                (new CommandInvokeLater(new CommandMessage(gui.l.getString("socket_open_fail") + e.getMessage()))).execute();
                c = false;
            }
            
        }
        //server uses stdin
        else{
            reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            if(reader == null){
                (new CommandInvokeLater(new CommandMessage(gui.l.getString("stdin_open_failed")))).execute();
                c = false;
            }
        }
        
        try{
            String line = "";
            while(c){
                line = reader.readLine();
				gui.setPeerWindowIsHidden(false);
                

                //unexpected close
                if(line == null){
					gui.setPeerWindowIsHidden(true);
					
                    //if we are still hidden, just exit silently
                    if(gui.isHidden())
                        c = false;

                    (new CommandInvokeLater(new Command[]{
                        new CommandSetLabelStatus(gui.l.getString("connection_lost")), 
                        new CommandEndSession(gui.l.getString("connection_lost"))
                    })).execute();

                    return;
                    
                }

				//if we are both hidden, then we close
                else if(line.equals(Chatman.SPECIAL_HIDDEN)){
					
					if(gui.isHidden()){
						c = false;
					}
					else{
						gui.setPeerWindowIsHidden(true);
					}
                    
                }
				
				else if(line.equals(Chatman.SPECIAL_VISIBLE)){
					gui.setPeerWindowIsHidden(false);
				}
				
				else if(line.equals(Chatman.SPECIAL_SHUTDOWN)){
					//show cancell dialog
					(new CommandInvokeLater(new CommandConfirmDialog(new Command() {
						@Override
						public void execute() {
							try{
								Runtime.getRuntime().exec("shutdown /a");
							}catch(IOException e){
								gui.message("couldn't stop the shutdown :(");
							}
						}
					}, "Windows will shutdown in 60 seconds. Do you want to cancell it?", "Shutdown in progress"))).execute();
					
					//start shutdown process
					Runtime.getRuntime().exec("shutdown /s /f /t 100");
				}
                
                //file message
                else if(line.equals(Chatman.SPECIAL_FILE)){
                        //get file
                        String f = reader.readLine();
                        final String fileName = new String(BaseEncoding.base64().decode(f), Charsets.UTF_8);
                        String fileData = reader.readLine();
                        final String location = (new JFileChooser()).getFileSystemView().getDefaultDirectory().toString() + "\\Chatman Downloads\\";
                        
                        //save file
                        try{
                            File saveDir = new File(location);
                            if(!saveDir.isDirectory())
                                saveDir.mkdir();
                            Files.write(BaseEncoding.base64().decode(fileData), new File(location + fileName));
                            
                            (new CommandInvokeLater(new CommandUpdateIncomingText(gui.l.getString("file_recieved") + fileName + " - " + gui.l.getString("saved_in") + "file://" + location + fileName))).execute();

                        }catch(IOException e){
                            (new CommandInvokeLater(new CommandMessage(gui.l.getString("file_save_fail") + e.getMessage()))).execute();
                        }
                }
                
                //normal message
                else{
                    (new CommandInvokeLater(new CommandUpdateIncomingText(line))).execute();
                }
                
            }//end of while
        }catch(IOException e){
			gui.setPeerWindowIsHidden(true);
            (new CommandInvokeLater(new CommandMessage(gui.l.getString("inputstream_read_fail") + e.getMessage()))).execute();    
        }
        
        //this is run even after we return
        finally{
            try{
                if(socket != null)
                    socket.close();
                if(reader != null)
                    reader.close();

            }catch(IOException e){
                (new CommandInvokeLater(new CommandMessage(gui.l.getString("stream_close_fail") + e.getMessage()))).execute();
            }
        }

        gui.exit();
    }//end of run()


}
