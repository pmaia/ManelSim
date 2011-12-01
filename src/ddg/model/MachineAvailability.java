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

/**
 * Stores the total times in which a machine remained active, idle, sleeping, turned off or in some intermediary state. 
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MachineAvailability {
	
	private double totalActiveDuration = 0;
	private double totalIdleDuration = 0;
	private double totalSleepingDuration = 0;
	private double totalShutdownDuration = 0;
	
	private int shutdownCount = 0;
	private int sleepCount = 0;

	public void addActiveDuration(double increment) {
		totalActiveDuration += increment;
	}
	
	public void addIdleDuration(double increment) {
		totalIdleDuration += increment;
	}
	
	public void addSleepingDuration(double increment) {
		totalSleepingDuration += increment;
		sleepCount++;
	}
	
	public void addShutdownDuration(double increment) {
		totalShutdownDuration += increment;
		shutdownCount++;
	}
	
	public double getTotalActiveDuration() {
		return totalActiveDuration;
	}
	
	public double getTotalIdleDuration() {
		return totalIdleDuration;
	}
	
	public double getTotalSleepingDuration() {
		return totalSleepingDuration;
	}
	
	public double getTotalShutdownDuration() {
		return totalShutdownDuration;
	}
	
	public int getSleepCount() {
		return sleepCount;
	}
	
	public int getShutdownCount() {
		return shutdownCount;
	}
	
}