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
public class WriteEvent extends Event {

	public static final String EVENT_NAME = "write";
	
	private final long fileSize;
	private final String filePath;

	public WriteEvent(FileSystemClient handler, Time scheduledTime, long fileSize, Time duration, String filePath) {
		super(EVENT_NAME, handler, scheduledTime, duration);
		
		this.fileSize = fileSize;
		this.filePath = filePath;
	}

	public long getFileSize() {
		return fileSize;
	}
	
	public String getFilePath() {
		return filePath;
	}

	@Override
	public String toString() {
		return getHandler() + "\t" + EVENT_NAME + "\t" + getScheduledTime() + "\t" + filePath + "\t" + fileSize;
	}
}