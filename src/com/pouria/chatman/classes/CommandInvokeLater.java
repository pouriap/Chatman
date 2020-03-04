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

import com.pouria.chatman.Helper;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.SwingUtilities;

/**
 *
 * @author pouriap
 * 
 * get one or more Command objects and runs their Execute() method in a thread-safe manner
 */
public class CommandInvokeLater implements Command{
	//TODO: should all calls be invokeAndWait?
    private final Command[] innerCommands;
	private final boolean sync;
    
    public CommandInvokeLater(Command c){
        innerCommands = new Command[]{c};
		sync = false;
    }
    
    public CommandInvokeLater(Command[] c){
        innerCommands = c;
		sync = false;
    }
	
	public CommandInvokeLater(Command c, boolean synchronous){
		innerCommands = new Command[]{c};
		sync = synchronous;
	}
    
    @Override
    public void execute(){
		
		//don't use invokelater if we're on event dispatch thread as suggested by JAVA docs
		if(SwingUtilities.isEventDispatchThread()){
			for(Command command: innerCommands){
				command.execute();
			}
		}
		
		if(sync){
			try{
				SwingUtilities.invokeAndWait(() -> {
					for(Command command: innerCommands){
						command.execute();
					}
				});
			}catch(Exception e){
				//this is for debug
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String stackTrace = sw.toString();
				Helper.getInstance().log("invokeAndWait Failed" + "\nStack Trace:\n" + stackTrace);
				//if wait fails then do normal invokeAndWait
				SwingUtilities.invokeLater(() -> {
					for(Command command: innerCommands){
						command.execute();
					}
				});
			}
		}
		else{
			SwingUtilities.invokeLater(() -> {
				for(Command command: innerCommands){
					command.execute();
				}
			});
		}
    }
}
