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

import java.io.IOException;

/**
 *
 * @author pouriap
 */
public interface ChatmanServer {
	/**
	 * should start the server and listen for all incoming messages
	 * incoming messages should be sent to a ChatmanMessageHandler for processing
	 * @throws java.io.IOException when port bind fails
	 */
	public void start() throws IOException;
}
