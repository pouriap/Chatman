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
package com.pouria.chatman.classes;

import com.pouria.chatman.gui.ChatFrame;

/**
 *
 * @author pouriap
 * 
 * it may seem that this Command doesn't do any GUI-related thing. but some GUI-related things happen in Chatman.connect()
 */
public class CommandClientConnect implements Command{
    
    public CommandClientConnect(){
    }
    
    @Override
    public void execute(){
        ChatFrame gui = ChatFrame.getInstance();
        gui.getChatmanInstance().getClient().connect();
    };
}
