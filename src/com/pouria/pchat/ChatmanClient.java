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
    
    ChatmanClient(ChatFrame gui){
        super(gui, MOD_CLIENT);
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
            gui.setLabelStatus("اتصال با " + serverSocket.getInetAddress().getHostAddress() + " برقرار شد");

            inputReaderThread = new Thread(new InputReaderTh(gui, serverSocket));
            inputReaderThread.start();
        }catch(UnknownHostException e){
            gui.message("could not find host"+e.getMessage());
            gui.exit();
        }catch(IOException e){
            gui.message("could not open stream"+e.getMessage());
            gui.exit();
        }
    }
    
    @Override
    public void stop(){
        try{
            if(isServerSocketSet()){
                serverSocket.close();
                serverSocket = null;
            }
            writer = null;
            
            if(inputReaderThread != null)
                if(inputReaderThread.isAlive())
                    inputReaderThread.interrupt();
            
            if(scanner != null)
                if(scanner.isAlive())
                    scanner.interrupt();
            
        }catch(Exception e){
            gui.message("could not stop chatman client: " + e.getMessage());
        }
    }
    
    public void connect(boolean retry){
        //connects to server. if server-ip is specified in config then connects directly
        //else it scans the subnet-mask for live servers
        //if retry is true, shows a dialog asking the user whether to retry connection
        
        if(retry)
            if(JOptionPane.showConfirmDialog(null, "سروری در شبکه پیدا نشد. تلاش دوباره؟", "سرور پیدا نشد", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                gui.exit();

        gui.setLabelStatus("در حال جستجوی شبکه");

        int serverPort = Integer.valueOf(gui.getConfig("server-port"));
        //if we have server's ip we don't scan the network
        if(gui.isConfigAvailable("server-ip")){
            String serverIp = gui.getConfig("server-ip");
            scanner = new Thread(new IpConnector(gui, serverIp, serverPort, true));
            scanner.start();
        }
        else{
            String subnet = gui.getConfig("subnet-mask");
            scanner = new Thread(new IpScanner(gui, subnet, serverPort));
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
