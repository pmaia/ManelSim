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
package simulation.beefs.event.filesystem;

import core.Event;
import core.EventHandler;
import core.Time;
import core.Time.Unit;

/**
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class DeleteReplicationGroup extends Event {
	
	public static final String EVENT_NAME = "delete-replication-group";
	
	private final String filePath;
	
	private static final Time DELETE_REPLICATION_GROUP_DURATION = new Time(0, Unit.SECONDS);

	public DeleteReplicationGroup(EventHandler aHandler, Time aScheduledTime, String filePath) {
		super(EVENT_NAME, aHandler, aScheduledTime, DELETE_REPLICATION_GROUP_DURATION);
		
		this.filePath = filePath;
	}
	
	public String getFilePath() { 
		return this.filePath;
	}
	
	@Override
	public String toString() {
		return getHandler() + "\t" + EVENT_NAME + "\t" + getScheduledTime() + "\t" + filePath;
	}

}
