/*
 * Copyright (c) 2020. Pouria Pirhadi
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.commands;

import javax.swing.*;

/**
 *
 * @author pouriap
 * 
 * shows a confirm dialog and executes the commandToRunOnYes and commandToRunOnNO based on user choice
 * yesCommand is a Command object that will be Execute()ed when the user chooses yes
 * noCommand is a Command object that will be Execute()ed when the user chooses no
 */
public class CmdConfirmDialog implements Command{
    
    private Command commandToRunOnYes = null;
    private Command commandToRunOnNO = null;
    private final String message;
    private final String title;
    
    public CmdConfirmDialog(Command yesCommand, String message, String title){
        commandToRunOnYes = yesCommand;
        this.message = message;
        this.title = title;
    }
    
    public CmdConfirmDialog(Command yesCommand, Command noCommand, String message, String title){
        commandToRunOnYes = yesCommand;
        commandToRunOnNO = noCommand;
        this.message = message;
        this.title = title;
    }
    
    @Override
    public void execute(){
        int o = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
        if(o == JOptionPane.YES_OPTION){
            commandToRunOnYes.execute();
        }
        else if( commandToRunOnNO != null ){
            commandToRunOnNO.execute();
        }
    }
            
    
}
