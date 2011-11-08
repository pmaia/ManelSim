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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ddg.emulator.EventSource;
import ddg.kernel.Event;
import ddg.kernel.Time;
import ddg.model.Machine;

/**
 * 
 * A parser for the trace of user activity.
 * 
 * This parser expects that inactivity is logged in the format below:
 * <br><br>
 * &lt;timestamp&gt;\t&lt;inactivity_time&gt;
 * <br><br>
 * where &lt;timestamp&gt; are the milliseconds since epoch in which the user inactivity 
 * started and &lt;inactivity_time&gt; is the time in seconds during which the user remained 
 * inactive. 
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class UserIdlenessEventParser implements EventSource {
	
	private final Machine machine;
	private final BufferedReader eventReader;
	
	public UserIdlenessEventParser(Machine machine, InputStream eventStream) {
		this.machine = machine;
		this.eventReader = new BufferedReader(new InputStreamReader(eventStream));
	}

	@Override
	public Event getNextEvent() {
		UserIdlenessStart event = null;
		
		try {
			String traceLine = eventReader.readLine();
			
			if(traceLine != null) {
				String [] tokens = traceLine.split("\\s");

				Time aScheduledTime = new Time(Long.parseLong(tokens[0]));
				long inactivityTime = Long.parseLong(tokens[1]);
				
				event = new UserIdlenessStart(machine, aScheduledTime, inactivityTime);
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return event;
	}

}
