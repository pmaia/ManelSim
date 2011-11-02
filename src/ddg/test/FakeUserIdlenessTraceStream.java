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
package ddg.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import ddg.emulator.UserIdlenessEventParser;

/**
 * 
 * An InputStream that simulates an InputStream over a trace file whose events are in the format expected by 
 * {@link UserIdlenessEventParser}. <code>numberOfEvents</code> events will be generated.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FakeUserIdlenessTraceStream extends FakeTraceStream {
	
	private final Random random = new Random();
	private long nextTimeStamp = System.currentTimeMillis();

	public FakeUserIdlenessTraceStream(int numberOfEvents) {
		super(numberOfEvents);
	}

	@Override
	protected InputStream generateNextEventStream() {
		StringBuilder traceLine = new StringBuilder();
		
		long idlenessDuration = Math.abs(random.nextInt());
		
		traceLine.append(nextTimeStamp);
		traceLine.append("\t");
		traceLine.append(idlenessDuration);
		traceLine.append("\n");
		
		nextTimeStamp += idlenessDuration;
		
		return new ByteArrayInputStream(traceLine.toString().getBytes());
	}

}