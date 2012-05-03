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
package simulation.beefs.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO make doc
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public abstract class FakeTraceStream extends InputStream {
	private int remainingEvents;
	private InputStream currentEventStream;
	
	public FakeTraceStream(int numberOfEvents) {
		this.remainingEvents = numberOfEvents;
	}

	@Override
	public int read() throws IOException {

		if(currentEventStream == null) {
			currentEventStream = generateNextEventStream();
			remainingEvents--;
		}
		
		int nextByte = currentEventStream.read();

		if(nextByte == -1) {
			if(remainingEvents > 0) {
				currentEventStream = generateNextEventStream();
				remainingEvents--;	
			} else {
				return -1;
			}
		}

		return nextByte;
	}

	protected abstract InputStream generateNextEventStream();

}
