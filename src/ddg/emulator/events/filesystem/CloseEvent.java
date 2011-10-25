/*
 * Copyright (c) 2002-2008 Universidade Federal de Campina Grande
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ddg.emulator.events.filesystem;

import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.Time;

/**
 * TODO make doc
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class CloseEvent extends Event {

	public static final String EVENT_NAME = "close";

	/**
	 * @param fileName
	 * @param handler
	 * @param scheduledTime
	 */
	public CloseEvent(String fileName, EventHandler handler,
			Time scheduledTime) {
		super(EVENT_NAME, handler, scheduledTime);
	}

}