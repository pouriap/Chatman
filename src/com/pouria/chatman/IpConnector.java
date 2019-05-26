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
    private final ChatmanClient client;
    private final String ip;
    private final int port;
	private final boolean askRetry;
    
    IpConnector(String host, int port, boolean retry, ChatmanClient client){
        this.gui = ChatFrame.getInstance();
        this.client = client;
        this.port = port;
        this.ip = host;
		this.askRetry = retry;
    }

    @Override
    public void run() {
        //connect to a specific ip
        try{
            Socket socket = new Socket();           
            socket.connect(new InetSocketAddress(ip, port), 3000);
            
			client.setServerSocket(socket);
			client.start();

            
        }catch(IOException ex){ 
			if(askRetry){
				//we show a confirm dialog asking retry?
				(new CommandInvokeLater(new CommandConfirmDialog(
						new CommandClientStart(),
						Helper.getInstance().getStr("server_retry_confirm"),
						Helper.getInstance().getStr("server_not_found")
				))).execute();
			}
        }
        
    }
    
}
