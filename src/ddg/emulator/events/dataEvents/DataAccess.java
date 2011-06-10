/**
 * Copyright (C) 2009 Universidade Federal de Campina Grande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ddg.emulator.events.dataEvents;

import ddg.emulator.events.TransactionalDataEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JETime;

/**
 * TODO make doc
 *
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public class DataAccess extends TransactionalDataEvent {

	public static final String EVENT_NAME = "data_access";
	
	public enum Type {READ_ACCESS, WRITE_ACCESS};
	
	private final long size;
	private final long offset;
	private final Type type;
	
	/**
	 * @param size
	 * @param offset
	 * @param accessType TODO
	 * @param name
	 * @param handler
	 * @param scheduledTime
	 */
	public DataAccess(long size, long offset, Type accessType, String name, JEEventHandler handler, JETime scheduledTime) {
		super(name, handler, scheduledTime);
		this.type = accessType;
		this.size = size;
		this.offset = offset;
	}

	public long getSize() {
		return size;
	}

	public long getOffset() {
		return offset;
	}

	public Type getType() {
		return type;
	}
	
}
