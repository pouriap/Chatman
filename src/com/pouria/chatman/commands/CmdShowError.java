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

import com.pouria.chatman.CMHelper;

import javax.swing.*;

/**
 *
 * @author pouriap
 */
public class CmdShowError implements Command{
    final String message;
    
    public CmdShowError(String message){
        this.message = message;
    }
    
    @Override
    public void execute(){
        JOptionPane.showMessageDialog(null, message, CMHelper.getInstance().getStr("error"), JOptionPane.ERROR_MESSAGE);
    }
}
