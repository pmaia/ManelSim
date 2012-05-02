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

package emulator.event.filesystem;

import model.FileSystemClient;
import kernel.Event;
import kernel.Time;

/**
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class ReadEvent extends Event {

	public static final String EVENT_NAME = "read";

	private final long length;
	private final String filePath;

	/**
	 * 
	 * @param filePath
	 * @param length
	 * @param handler
	 * @param scheduledTime
	 */
	public ReadEvent(FileSystemClient handler, Time scheduledTime, Time duration, String filePath, long length) {
		super(EVENT_NAME, handler, scheduledTime, duration);
		
		this.filePath = filePath;
		this.length = length;
	}

	/**
	 * @return
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @return
	 */
	public long getLength() {
		return length;
	}
	
	@Override
	public String toString() {
		return getHandler() + "\t" + EVENT_NAME + "\t" + getScheduledTime() + "\t" + filePath + "\t" + length;
	}

}