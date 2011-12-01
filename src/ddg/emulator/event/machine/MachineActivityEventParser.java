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
import ddg.kernel.Time.Unit;
import ddg.model.Machine;

/**
 * 
 * A parser for the trace of machine activity.
 * 
 * This parser expects that activity is logged in the format below:
 * <br><br>
 * &lt;idleness|shutdown&gt;\t&lt;start_timestamp&gt;\t&lt;duration&gt;
 * <br><br>
 * where &lt;start_timestamp&gt; are the seconds since epoch in which the event 
 * started and &lt;duration&gt; is the time in seconds during which the event lasted.
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MachineActivityEventParser implements EventSource {
	
	private final Machine machine;
	private final BufferedReader eventReader;
	
	public MachineActivityEventParser(Machine machine, InputStream eventStream) {
		this.machine = machine;
		this.eventReader = new BufferedReader(new InputStreamReader(eventStream));
	}

	@Override
	public Event getNextEvent() {
		Event event = null;
		
		try {
			String traceLine = eventReader.readLine();
			
			if(traceLine != null) {
				String [] tokens = traceLine.split("\\s");
				
				if(tokens.length != 3) {
					throw new RuntimeException("Bad formatted line: " + traceLine);
				}
				
				String eventType = tokens[0];
				Time aScheduledTime = new Time(Long.parseLong(tokens[1]), Unit.SECONDS);
				Time duration = new Time(Long.parseLong(tokens[2]), Unit.SECONDS);
				
				if(eventType.equals("idleness")) {
					event = new UserIdlenessEvent(machine, aScheduledTime, duration);
				} else if(eventType.equals("shutdown")) {
					event = new ShutdownEvent(machine, aScheduledTime, duration);
				} else if(eventType.equals("activity")) {
					event = new UserActivityEvent(machine, aScheduledTime, duration, false);
				} else {
					throw new RuntimeException(eventType + " is not recognized by this parser as a valid event type.");
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return event;
	}

}
