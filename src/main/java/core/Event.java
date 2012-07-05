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
package core;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class Event implements Comparable<Event> {

	private final Time scheduledTime;

	public Event(Time scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public abstract void process();

	public Time getScheduledTime() {
		return scheduledTime;
	}

	@Override
	public int compareTo(Event o) {
		return this.scheduledTime.compareTo(o.scheduledTime);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((scheduledTime == null) ? 0 : scheduledTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (scheduledTime == null) {
			if (other.scheduledTime != null)
				return false;
		} else if (!scheduledTime.equals(other.scheduledTime))
			return false;
		return true;
	}
	
}