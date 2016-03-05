/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

/**
 *
 * @author SH
 */
//Output: Socket.OutputStream
//Input:  Socket.inputStream
public class ChatmanClient extends Chatman{
    
    private Socket serverSocket;
    private Thread scanner;
    
    ChatmanClient(){
        super(MOD_CLIENT);
    }
    
    @Override
    public void start(){
        //we could put connets()'s content here but connect needs and arguments and we want to 
        //use the abstract start() so we do this
        connect(false);
    }
        
    public void start2(){
        //this method does what start() does in ChatmanServer, however because in cilent we need
        //to stablish a socket connection first, the start() method in client does that.
        //stablishes the input and output streams as a client
        //is only called from IpConnector when it finds an alive server
        try{
            writer = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream()),true);
            (new CommandInvokeLater(new CommandSetLabelStatus(gui.l.getString("connection_with") + serverSocket.getInetAddress().getHostAddress() + gui.l.getString("stablished")))).execute();

            th = new InputReaderTh(serverSocket);
            inputReaderThread = new Thread(th);
            inputReaderThread.start();
        }catch(UnknownHostException e){
            (new CommandInvokeLater(new CommandMessage(gui.l.getString("find_host_fail") + e.getMessage()))).execute();
            gui.exit();
        }catch(IOException e){
            (new CommandInvokeLater(new CommandMessage(gui.l.getString("stream_open_fail") + e.getMessage()))).execute();
            gui.exit();
        }
    }
    
    
    public void connect(boolean retry){
        //connects to server. if server-ip is specified in config then connects directly
        //else it scans the subnet-mask for live servers
        //if retry is true, shows a dialog asking the user whether to retry connection
        
        if(retry)
            if(JOptionPane.showConfirmDialog(null, gui.l.getString("server_retry_confirm"), gui.l.getString("server_not_found"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                gui.exit();

        gui.setLabelStatus(gui.l.getString("searching_network"));

        int serverPort = Integer.valueOf(ChatmanConfig.getInstance().get("server-port"));
        //if we have server's ip we don't scan the network
        if(ChatmanConfig.getInstance().isSet("server-ip")){
            String serverIp = ChatmanConfig.getInstance().get("server-ip");
            scanner = new Thread(new IpConnector(serverIp, serverPort, true));
            scanner.start();
        }
        else{
            String subnet = ChatmanConfig.getInstance().get("subnet-mask");
            scanner = new Thread(new IpScanner(subnet, serverPort));
            scanner.start();
        }

    }
    
    //this is called from the scanner thread. acts as a flag for us to know if the 
    //scanner thread has found a live server
    public void setServerSocket(Socket s){
        if(this.serverSocket != null)
            throw new IllegalArgumentException();
        else
            this.serverSocket = s;
    }
    
    public boolean isServerSocketSet(){
        return this.serverSocket != null;
    }
}
