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
package com.pouria.chatman.commands;

/**
 *
 * @author pouriap
 * 
 * i could as well use the Runnable() interface instead of creating this
 * but i was learning design patterns and was excited :P
 * all classes implementing this interface do a GUI-related command
 * all classes implementing this interface will get wrapped in a CommandInvokeLater object (they don't have to but that's the whole purpose of this interface)
 * the purpose was to reduce code redundancy
 */
public interface Command {
    public void execute();
}
