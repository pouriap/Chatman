/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman;

import com.pouria.chatman.gui.ChatmanConfig;
import com.pouria.chatman.gui.ChatFrame;
import java.io.PrintWriter;

/**
 *
 * @author pouriap
 */
//is responsible for sending data to streams
//if chatmanclient extends it, output stream is a socket
//if chatmanserver extends it, output stream is STDOUT
//inputreaderth is responsible for input streams
public abstract class Chatman {
    protected final int mode;
    protected final ChatFrame gui;
    protected PrintWriter writer;
    protected InputReaderTh th;
    protected Thread inputReaderThread;
    protected String userName, peerName;
    public final static int MOD_SERVER = 1, MOD_CLIENT = 2;
    public final static String SPECIAL_BYE = "byebyebye", SPECIAL_FILE = "filefilefile";
    
    public Chatman(int mode){
        this.gui = ChatFrame.getInstance();
        this.mode = mode;
        updateUserName();
    }
    
    public final void send(String s){
        if(writer == null)
            throw new NullPointerException();
        else
            writer.println(s);
    }

    public final void sendFile(String fileName, String fileContent){
        send(SPECIAL_FILE);
        send(fileName);
        send(fileContent);
    }
    
    public final void sendBye(){
        //reason of goToComa is explained in InputReaderTh
        th.goToComa();
        send(SPECIAL_BYE);
    }
    
    public final int getMode(){
        return this.mode;
    }
    
    public final void setUserName(String name){
        userName = name;
    }
    
    public final void setPeerName(String name){
        peerName = name;
    }
    
    public final String getUserName(){
        return userName;
    }
    
    public final String getPeerName(){
        return peerName;
    }
    
    public final void updateUserName(){
        //esme background ha be in shekl as
        String name = ChatmanConfig.getInstance().get("background-image").split("_")[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1,name.length());
        setUserName(name);
    }

    //abstract methods
    public abstract void start();
            
}
