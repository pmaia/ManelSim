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

package ddg.emulator.events.fuseEvents;

import ddg.emulator.events.TransactionalDataEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JETime;
import ddg.model.DDGClient;

/**
 * TODO make doc
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class WriteEvent extends TransactionalDataEvent {

	public static final String EVENT_NAME = "write";

	private final long size;
	private final long offset;
	private final DDGClient client;

	private final int fileDescriptor;

	/**
	 * @param size
	 *            TODO
	 * @param offset
	 *            TODO
	 * @param fileDescriptor
	 *            TODO
	 * @param handler
	 * @param scheduledTime
	 * @param client
	 *            TODO
	 */
	public WriteEvent(long size, long offset, int fileDescriptor,
			JEEventHandler handler, JETime scheduledTime, DDGClient client) {
		super(EVENT_NAME, handler, scheduledTime);
		this.fileDescriptor = fileDescriptor;
		this.size = size;
		this.offset = offset;
		this.client = client;
	}

	/**
	 * @return
	 */
	public int getFileDescriptor() {
		return fileDescriptor;
	}

	/**
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @return
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @return
	 */
	public DDGClient getClient() {
		return client;
	}

}