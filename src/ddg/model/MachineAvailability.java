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
	private double totalToSleepDuration = 0;
	private double totalFromSleepDuration = 0;
	private double totalToShutdownDuration = 0;
	private double totalFromShutdownDuration = 0;
	
	public void addActiveDuration(double increment) {
		totalActiveDuration += increment;
	}
	
	public void addIdleDuration(double increment) {
		totalIdleDuration += increment;
	}
	
	public void addSleepingDuration(double increment) {
		totalSleepingDuration += increment;
	}
	
	public void addShutdownDuration(double increment) {
		totalShutdownDuration += increment;
	}
	
	public void addToSleepDuration(double increment) {
		totalToSleepDuration += increment;
	}

	public void addFromSleepDuration(double increment) {
		totalFromSleepDuration += increment;
	}

	public void addToShutdownDuration(double increment) {
		totalToShutdownDuration += increment;
	}

	public void addFromShutdownDuration(double increment) {
		totalFromShutdownDuration += increment;
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
	
	public double getToSleepDuration() {
		return totalToSleepDuration;
	}

	public double getFromSleepDuration() {
		return totalFromSleepDuration;
	}

	public double getToShutdownDuration() {
		return totalToShutdownDuration;
	}

	public double addFromShutdownDuration() {
		return totalFromShutdownDuration;
	}
	
}