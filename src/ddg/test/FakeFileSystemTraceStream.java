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

import ddg.emulator.event.filesystem.FileSystemEventParser;

/**
 * 
 * An InputStream that simulates an InputStream over a trace file whose events are in the format expected by 
 * {@link FileSystemEventParser}. <code>numberOfEvents</code> events will be generated.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FakeFileSystemTraceStream extends FakeTraceStream {
	
	private final Random random = new Random();
	private long nextTimeStamp = System.currentTimeMillis();

	private static final String [] operations = {"read", "write", "open", "close"};

	public FakeFileSystemTraceStream(int numberOfEvents) {
		super(numberOfEvents);
	}

	//FIXME update this method to generate lines accordingly with the new fs trace format 
	protected InputStream generateNextEventStream() {
		final String SEPARATOR = "\t";
		final String LINE_SEPARATOR = "\n";
		final int uniqueFileHandle = 33;
		final int lengthReadOrWrite = 1024;

		StringBuilder strBuilder = new StringBuilder();

		String op = operations[random.nextInt(4)];

		strBuilder.append(op);
		strBuilder.append(SEPARATOR);

		if(op.equals("open")) {
			strBuilder.append("/home/unique/file");
			strBuilder.append(SEPARATOR);
		}

		strBuilder.append(nextTimeStamp);
		strBuilder.append(SEPARATOR);
		strBuilder.append(uniqueFileHandle);
		strBuilder.append(SEPARATOR);

		if(op.equals("read") || op.equals("write")) {
			strBuilder.append(lengthReadOrWrite);
		}

		strBuilder.append(LINE_SEPARATOR);

		return new ByteArrayInputStream(strBuilder.toString().getBytes());
	}

}