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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 *
 * @author pouriap
 * 
 * Output: STDOUT
 * Input:  STDIN
 */

public class ChatmanServer extends Chatman {
    
    public ChatmanServer(){
        super(MOD_SERVER);
    }

    //establishes the input and output streams as a server    
    @Override
    public void start(){

        writer = new PrintWriter(new OutputStreamWriter(System.out), true);
        gui.setLabelStatus(gui.l.getString("server_running"));

        th = new InputReaderTh();
        inputReaderThread = new Thread(th);
        inputReaderThread.start();
        
    }


}
