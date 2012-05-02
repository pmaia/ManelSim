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
package emulator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import emulator.event.filesystem.FileSystemTraceEventSource;

/**
 * 
 * An InputStream that simulates an InputStream over a trace file whose events are in the format expected by 
 * {@link FileSystemTraceEventSource}. <code>numberOfEvents</code> events will be generated.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FakeFileSystemTraceStream extends FakeTraceStream {
	
	private final Random random = new Random();
	private long nextTimeStamp = System.currentTimeMillis();

	private static final String [] operations = {"read", "write", "close", "unlink"};

	public FakeFileSystemTraceStream(int numberOfEvents) {
		super(numberOfEvents);
	}

	protected InputStream generateNextEventStream() {
		final String SEPARATOR = "\t";
		final String LINE_SEPARATOR = "\n";
		final String uniqueFilePath = "/home/unique/file";
		final String operationDuration = "-12345"; 
		final int lengthReadOrWrite = 1024;

		StringBuilder strBuilder = new StringBuilder();

		String op = operations[random.nextInt(operations.length)];

		strBuilder.append(op);
		strBuilder.append(SEPARATOR);

		strBuilder.append(nextTimeStamp);
		strBuilder.append(operationDuration);
		strBuilder.append(SEPARATOR);
		strBuilder.append(uniqueFilePath);
		strBuilder.append(SEPARATOR);

		if(op.equals("read") || op.equals("write")) {
			strBuilder.append(lengthReadOrWrite);
		}

		strBuilder.append(LINE_SEPARATOR);
		
		nextTimeStamp += random.nextInt(5000);

		return new ByteArrayInputStream(strBuilder.toString().getBytes());
	}

}