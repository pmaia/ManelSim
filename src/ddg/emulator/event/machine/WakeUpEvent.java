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
package ddg.emulator.event.machine;

import ddg.kernel.Event;
import ddg.kernel.Time;
import ddg.model.Machine;

/**
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class WakeUpEvent extends Event {
	
	public static final String EVENT_NAME = "wake-up";
	
	private final boolean fsWakeUp;

	/**
	 * 
	 * @param aHandler
	 * @param aScheduledTime
	 * @param fsWakeUp true if the wake up was caused by the opportunistic file system 
	 */
	public WakeUpEvent(Machine aHandler, Time aScheduledTime, boolean fsWakeUp) {
		super(EVENT_NAME, aHandler, aScheduledTime);
		
		this.fsWakeUp = fsWakeUp;
	}
	
	/**
	 * 
	 * @return true if the wake up was caused by file system activity
	 */
	public boolean wasCausedByTheOpportunisticFS() {
		return this.fsWakeUp;
	}
	
	@Override
	public String toString() {
		return EVENT_NAME + "\t" + getScheduledTime();
	}

}
