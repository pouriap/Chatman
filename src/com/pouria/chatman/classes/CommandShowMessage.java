/*
 * Copyright (C) 2020 pouriap
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

import javax.swing.JOptionPane;

/**
 *
 * @author pouriap
 */
public class CommandShowMessage implements Command{
    String message;
    
    public CommandShowMessage(String message){
        this.message = message;
    }
    
    @Override
    public void execute(){
        JOptionPane.showMessageDialog(null, message, "Fatal Error!", JOptionPane.ERROR_MESSAGE);
    }
}
