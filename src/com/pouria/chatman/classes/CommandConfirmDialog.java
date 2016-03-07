/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

import javax.swing.JOptionPane;

/**
 *
 * @author pouriap
 * 
 * shows a confirm dialog and executes the commandToRunOnYes and commandToRunOnNO based on user choice
 * yesCommand is a Command object that will be Execute()ed when the user chooses yes
 * noCommand is a Command object that will be Execute()ed when the user chooses no
 */
public class CommandConfirmDialog implements Command{
    
    private Command commandToRunOnYes = null;
    private Command commandToRunOnNO = null;
    private final String message;
    private final String title;
    
    public CommandConfirmDialog(Command yesCommand, String message, String title){
        commandToRunOnYes = yesCommand;
        this.message = message;
        this.title = title;
    }
    
    public CommandConfirmDialog(Command yesCommand, Command noCommand, String message, String title){
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
