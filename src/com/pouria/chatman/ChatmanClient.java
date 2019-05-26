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

import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandMessage;
import com.pouria.chatman.classes.CommandSetLabelStatus;
import com.pouria.chatman.gui.ChatmanConfig;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author pouriap
 * 
 * Output: Socket.OutputStream
 * Input:  Socket.inputStream
 */

public class ChatmanClient extends Chatman{
    
    private Socket serverSocket = null;
    private Thread scanner;
    
    public static final boolean RETRY = true, NORETRY = false;
    
    public ChatmanClient(){
        super(MOD_CLIENT);
    }
    
    //stablishes the input and output streams as a client
    //is called from IpConnector when it finds an alive server hence needs to be thread safe
    //if we are not connected calls connect() and returns. threads in connect() call start() again once they find a live server
    @Override
    public void start(){
        
        if(!isConnected()){
            connect();
            return;
        }
        
        try{
            writer = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream()),true);
            (new CommandInvokeLater(new CommandSetLabelStatus(Helper.getInstance().getStr("connection_with") + serverSocket.getInetAddress().getHostAddress() + Helper.getInstance().getStr("stablished")))).execute();

            th = new InputReaderTh(serverSocket);
            inputReaderThread = new Thread(th);
            inputReaderThread.start();
            
        }catch(UnknownHostException e){
            (new CommandInvokeLater(new CommandMessage(Helper.getInstance().getStr("find_host_fail") + e.getMessage()))).execute();
            gui.exit();
        }catch(IOException e){
            (new CommandInvokeLater(new CommandMessage(Helper.getInstance().getStr("stream_open_fail") + e.getMessage()))).execute();
            gui.exit();
        }catch(Exception e){
            (new CommandInvokeLater(new CommandMessage(Helper.getInstance().getStr("client_start_fail") + e.getMessage()))).execute();
            gui.exit();
        }
    }
    
    //connects to server. if server-ip is specified in config then connects directly
    //else it scans the subnet-mask for live servers    
    private void connect(){
        gui.setLabelStatus(Helper.getInstance().getStr("searching_network"));

        int serverPort = Integer.valueOf(ChatmanConfig.getInstance().get("server-port"));
        
        //if we have server's ip we don't scan the network
        if(ChatmanConfig.getInstance().isSet("server-ip")){
            String serverIp = ChatmanConfig.getInstance().get("server-ip");
            scanner = new Thread(new IpConnector(serverIp, serverPort, true, this));
            scanner.start();
        }
        else{
            String subnet = ChatmanConfig.getInstance().get("subnet-mask");
            scanner = new Thread(new IpScanner(subnet, serverPort, this));
            scanner.start();
        }

    }
    
    
    //sets the socket we wnat to connect to
    public void setServerSocket(Socket s){
        serverSocket = s;
    }
	
	//have we found a server
	public boolean isServerFound(){
		return serverSocket != null;
	}
    
    public boolean isConnected(){
        if(serverSocket == null)
            return false;
        
        return this.serverSocket.isConnected() && !this.serverSocket.isClosed();
    }
}
