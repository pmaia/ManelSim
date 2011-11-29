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

package ddg.emulator.event.filesystem;

import ddg.kernel.Event;
import ddg.kernel.Time;
import ddg.model.DDGClient;

/**
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class WriteEvent extends Event {

	public static final String EVENT_NAME = "write";
	
	private final long length;
	private final String filePath;
	private final Time duration;

	public WriteEvent(DDGClient handler, Time scheduledTime, long length, Time duration, String filePath) {
		super(EVENT_NAME, handler, scheduledTime);
		
		this.length = length;
		this.duration = duration;
		this.filePath = filePath;
	}

	public long getLength() {
		return length;
	}
	
	/**
	 * 
	 * @return the duration of this operation
	 */
	public Time getDuration() {
		return duration;
	}

	public String getFilePath() {
		return filePath;
	}

	@Override
	public String toString() {
		return EVENT_NAME + "\t" + getScheduledTime() + "\t" + filePath + "\t" + length;
	}
}