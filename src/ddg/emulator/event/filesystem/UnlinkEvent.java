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
package ddg.emulator.event.filesystem;

import ddg.kernel.Event;
import ddg.kernel.Time;
import ddg.kernel.Time.Unit;
import ddg.model.FileSystemClient;

/**
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class UnlinkEvent extends Event {
	
	public static final String EVENT_NAME = "unlink";
	
	private final String filePath;
	
	private static final Time UNLINK_DURATION = new Time(0, Unit.SECONDS);

	public UnlinkEvent(FileSystemClient aHandler, Time aScheduledTime, String filePath) {
		super(EVENT_NAME, aHandler, aScheduledTime, UNLINK_DURATION);
		
		this.filePath = filePath;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	
	@Override
	public String toString() {
		return EVENT_NAME + "\t" + getScheduledTime() + "\t" + filePath;
	}

}
