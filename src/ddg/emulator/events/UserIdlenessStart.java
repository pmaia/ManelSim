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
package ddg.emulator.events;

import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.Time;

/**
 * An {@link Event} representing the start of an idleness period.
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class UserIdlenessStart extends Event {
	
	public static final String EVENT_NAME = "user-inactivity-start";
	
	private final long inactivityTime; 

	public UserIdlenessStart(EventHandler aHandler, Time aScheduledTime, long inactivityTime) {
		super(EVENT_NAME, aHandler, aScheduledTime);
		
		this.inactivityTime = inactivityTime;
	}

	public long getInactivityTime() {
		return inactivityTime;
	}

}
