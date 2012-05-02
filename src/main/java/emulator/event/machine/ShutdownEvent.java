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
package emulator.event.machine;

import kernel.Event;
import kernel.EventHandler;
import kernel.Time;

/**
 * An {@link Event} representing a shutdown period.
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class ShutdownEvent extends Event {
	
	public static final String EVENT_NAME = "machine-shutdown";
	
	public ShutdownEvent(EventHandler handler, Time scheduledTime, Time duration) {
		super(EVENT_NAME, handler, scheduledTime, duration);
	}
	
	@Override
	public String toString() {
		return getHandler() + "\t" + EVENT_NAME + "\t" + getScheduledTime();
	}
}
