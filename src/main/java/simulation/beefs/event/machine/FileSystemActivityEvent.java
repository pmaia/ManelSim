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
package simulation.beefs.event.machine;

import simulation.beefs.Machine;
import core.Event;
import core.Time;

/**
 * TODO make doc
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FileSystemActivityEvent extends Event {
	
	public static final String EVENT_NAME = "fs-activity";
	
	private final boolean wakeOnLan;
	
	public FileSystemActivityEvent(Machine handler, Time scheduledTime, Time duration, boolean wakeOnLan) {
		super(EVENT_NAME, handler, scheduledTime, duration);
		
		this.wakeOnLan = wakeOnLan;
	}

	public boolean wakeOnLan() {
		return wakeOnLan;
	}
	
	@Override
	public String toString() {
		return getHandler() + "\t" + EVENT_NAME + "\t" + getScheduledTime() + "\t" +
				getDuration() + "\t" + wakeOnLan;
	}

}
