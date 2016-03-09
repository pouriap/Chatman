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

import com.pouria.chatman.classes.CommandClientStart;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.gui.ChatFrame;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author pouriap
 * 
 * is only used when we are client
 * takes an ip and port and connects to it
 * is only called when we have the ip of the live server
 * which we acquire either by scanning or from the config server-ip
 */

public class IpConnector implements Runnable{

    private final ChatFrame gui;
    private final String ip;
    private final int port;
    private final boolean isSignle;
    
    IpConnector(String h, int p, boolean r){
        this.gui = ChatFrame.getInstance();
        this.port = p;
        this.ip = h;
        this.isSignle = r;
    }

    @Override
    public void run() {
        //connect to a specific ip
        try{
            Socket socket = new Socket();           
            socket.connect(new InetSocketAddress(ip, port), 3000);
            
            //isSingle = we have the server ip and we are connecting to it
            //there is no scanning and adding to list
            if(isSignle){
                ((ChatmanClient)gui.getChatmanInstance()).setServerSocket(socket);
                ((ChatmanClient)gui.getChatmanInstance()).start();
            }
            //we are scanning
            //so we just add the live server to list
            //when scanning is finished we decide what to do in IpScanner
            else{
                ((ChatmanClient)gui.getChatmanInstance()).addLiveServer(socket);
            }
            
        }catch(IOException ex){ 
            //is single is false when we are scanning network for servers and don't want a "not found" message for each failed connect
            //we show a confirm dialog asking retry?
            if(isSignle){
                (new CommandInvokeLater(new CommandConfirmDialog(
                        new CommandClientStart(),
                        gui.l.getString("server_retry_confirm"),
                        gui.l.getString("server_not_found")
                ))).execute();
            }
        }
        
    }
    
}
