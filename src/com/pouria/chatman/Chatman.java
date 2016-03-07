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

import com.pouria.chatman.gui.Background;
import com.pouria.chatman.gui.ChatFrame;
import java.io.PrintWriter;

/**
 *
 * @author pouriap
 * 
 * is responsible for sending data to streams
 * inputReaderTh is responsible for input streams
 * if ChatmanClient extends it, output stream(writer) is a socket
 * if ChatmanServer extends it, output stream(writer) is STDOUT
 */

public abstract class Chatman {
    protected final int mode;
    protected final ChatFrame gui;
    protected PrintWriter writer;
    protected InputReaderTh th;
    protected Thread inputReaderThread;
    protected String userName, peerName;
    public final static int MOD_SERVER = 1, MOD_CLIENT = 2;
    public final static String SPECIAL_BYE = "byebyebye", SPECIAL_FILE = "filefilefile", SPECIAL_ID = "ididid";
    
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
    
    //public final void sendUserId(){
    //    String userId = ChatmanConfig.getInstance().get("user-id");
    //    send(SPECIAL_ID);
    //    send(userId);
    //}
    
    public final int getMode(){
        return this.mode;
    }
    
    public final void setUserName(String name){
        userName = name;
    }
    
    public final String getUserName(){
        return userName;
    }
    
    //public final void setPeerName(String name){
    //    peerName = name;
    //}
    
    //public final String getPeerName(){
    //    return peerName;
    //}
    
    //username is determined based on the file name of the current background
    //i deliberately did this
    public final void updateUserName(){
        //background names are like batman_1.jpg or batman.png
        String name = Background.getInstance().getCurrent().split("\\.")[0].split("_")[0];        
        name = name.substring(0, 1).toUpperCase() + name.substring(1,name.length());
        setUserName(name);
    }

    //abstract methods
    public abstract void start();
            
}
