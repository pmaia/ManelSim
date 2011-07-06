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
public class OpenEvent extends TransactionalDataEvent {

	public static final String EVENT_NAME = "open";
	private final DDGClient client;
	private final int fileDescriptor;
	private final String fileName;

	/**
	 * @param fileName
	 * @param fileDescriptor
	 * @param client
	 * @param handler
	 * @param scheduledTime
	 */
	public OpenEvent(String fileName, int fileDescriptor, DDGClient client,
			JEEventHandler handler, JETime scheduledTime) {
		super(EVENT_NAME, handler, scheduledTime);
		this.fileName = fileName;
		this.fileDescriptor = fileDescriptor;
		this.client = client;
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return fileName;
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
	public DDGClient getClient() {
		return client;
	}

}