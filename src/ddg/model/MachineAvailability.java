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
package ddg.model;

import ddg.kernel.Time;
import ddg.kernel.Time.Unit;

/**
 * Stores the total times in which a machine remained active, idle, sleeping, turned off or in some intermediary state. 
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MachineAvailability {
	
	private Time totalActiveDuration = new Time(0, Unit.SECONDS);
	private Time totalIdleDuration = new Time(0, Unit.SECONDS);
	private Time totalSleepingDuration = new Time(0, Unit.SECONDS);
	private Time totalShutdownDuration = new Time(0, Unit.SECONDS);
	
	private int shutdownCount = 0;
	private int sleepCount = 0;

	public void addActiveDuration(Time increment) {
		totalActiveDuration = totalActiveDuration.plus(increment);
	}
	
	public void addIdleDuration(Time increment) {
		totalIdleDuration = totalIdleDuration.plus(increment);
	}
	
	public void addSleepingDuration(Time increment) {
		totalSleepingDuration = totalSleepingDuration.plus(increment);
		sleepCount++;
	}
	
	public void addShutdownDuration(Time increment) {
		totalShutdownDuration = totalShutdownDuration.plus(increment);
		shutdownCount++;
	}
	
	public Time getTotalActiveDuration() {
		return totalActiveDuration;
	}
	
	public Time getTotalIdleDuration() {
		return totalIdleDuration;
	}
	
	public Time getTotalSleepingDuration() {
		return totalSleepingDuration;
	}
	
	public Time getTotalShutdownDuration() {
		return totalShutdownDuration;
	}
	
	public int getSleepCount() {
		return sleepCount;
	}
	
	public int getShutdownCount() {
		return shutdownCount;
	}
	
}