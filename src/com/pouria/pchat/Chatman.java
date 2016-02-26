/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author SH
 */
//is responsible for sending data to streams
//if chatmanclient extends it, output stream is a socket
//if chatmanserver extends it, output stream is STDOUT
//inputreaderth is responsible for input streams
public abstract class Chatman {
    protected final int mode;
    protected final ChatFrame gui;
    protected PrintWriter writer;
    protected Thread inputReaderThread;
    final static int MOD_SERVER = 1, MOD_CLIENT = 2;
    final static String SPECIAL_BYE = "byebyebye", SPECIAL_FILE = "filefilefile";
    
    Chatman(ChatFrame gui, int mode){
        this.gui = gui;
        this.mode = mode;
    }
    
    public void send(String s){
        if(writer == null)
            throw new NullPointerException();
        else
            writer.println(s);
    }

    public  void sendFile(String fileName, String fileContent){
        send(SPECIAL_FILE);
        send(fileName);
        send(fileContent);
    }
    
    public void sendBye(){
        send(SPECIAL_BYE);
    }
    
    public int getMode(){
        return this.mode;
    }
    
    //abstract methods
    //to be Ovverriden by both client and server
    public abstract void start();
    
    //to be Overriden by ChatmanClient
    public void connect(boolean retry){
    }
    
    public void setServerSocket(Socket s){
    }
    
    public boolean isServerSocketSet(){
        return true;
    }
}
