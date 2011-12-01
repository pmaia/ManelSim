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
public class SleepEvent extends Event {
	
	public static final String EVENT_NAME = "sleep";
	
	public SleepEvent(Machine aHandler, Time aScheduledTime) {
		super(EVENT_NAME, aHandler, aScheduledTime, null);
	}
	
	@Override
	public String toString() {
		return EVENT_NAME + "\t" + getScheduledTime();
	}
	
	@Override
	public Time getDuration() {
		throw new UnsupportedOperationException("It's not possible to know the duration of an sleep period");
	}

}
