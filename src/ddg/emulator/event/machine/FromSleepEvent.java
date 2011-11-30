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
import ddg.kernel.EventHandler;
import ddg.kernel.Time;
import ddg.kernel.Time.Unit;

/**
 * TODO make doc
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FromSleepEvent extends Event {
	
	private static final String EVENT_NAME = "wake-up-from-sleep";
	private static final Time FROM_SLEEP_DURATION = new Time(0L, Unit.SECONDS); //FIXME what is the right value?
	
	public FromSleepEvent(EventHandler aHandler, Time scheduledTime) {
		super(EVENT_NAME, aHandler, scheduledTime, FROM_SLEEP_DURATION);
	}

}
