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
package ddg.emulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import ddg.emulator.events.filesystem.CloseEvent;
import ddg.emulator.events.filesystem.OpenEvent;
import ddg.emulator.events.filesystem.ReadEvent;
import ddg.emulator.events.filesystem.WriteEvent;
import ddg.kernel.Event;
import ddg.kernel.Time;
import ddg.model.DDGClient;

/**
 * A parser for the trace of calls to the file system.
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FileSystemEventParser implements EventParser {

	private final BufferedReader bufferedReader;
	
	private final DDGClient client;

	/**
	 * @param traceStream
	 * @throws IOException
	 */
	public FileSystemEventParser(InputStream traceStream, DDGClient client) {
		this.bufferedReader = new BufferedReader(new InputStreamReader(traceStream));
		this.client = client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Event getNextEvent() {

		String nextLine;

		try {
			nextLine = readNextLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (nextLine == null)
			return null;

		Operation nextOp = readNextOperation(nextLine);

		switch (nextOp) {

		case OPEN:
			return readOpenEvent(nextLine);
		case WRITE:
			return readWriteEvent(nextLine);
		case READ:
			return readReadEvent(nextLine);
		case CLOSE:
			return readCloseEvent(nextLine);
		default:
			return null;
		}
	}

	private String readNextLine() throws IOException {

		String readLine = null;

		while ((readLine = bufferedReader.readLine()) != null) {

			if (!readLine.trim().equals("")) {
				return readLine;
			}

		}

		return readLine;
	}

	private Event readCloseEvent(String nextLine) {

		// close 960254662.240045 3
		StringTokenizer tokenizer = new StringTokenizer(nextLine);

		tokenizer.nextToken();// close
		Time time = new Time(Long.valueOf(tokenizer.nextToken()));// time stamp
		tokenizer.nextToken();// fd

		return new CloseEvent(null, client, time);
	}

	private Event readReadEvent(String traceLine) {

		// read 960254162.929275 3 4096

		StringTokenizer tokenizer = new StringTokenizer(traceLine);

		tokenizer.nextToken();// read
		Time time = new Time(Long.valueOf(tokenizer.nextToken()));// time stamp
		int fileDescriptor = new Integer(tokenizer.nextToken());// fd
		long dataLength = Long.parseLong(tokenizer.nextToken());// length

		return new ReadEvent(dataLength, 0, fileDescriptor, client, time, client);
	}

	private Event readWriteEvent(String traceLine) {

		// write 960254662.240004 3 1137

		StringTokenizer tokenizer = new StringTokenizer(traceLine);

		tokenizer.nextToken();// write
		Time time = new Time(Long.valueOf(tokenizer.nextToken()));// time
		int fileDescriptor = new Integer(tokenizer.nextToken());// fd
		long dataLength = Long.parseLong(tokenizer.nextToken());// length

		return new WriteEvent(dataLength, 0, fileDescriptor, client, time, client);
	}

	private Event readOpenEvent(String line) {

		// open /usr/X11R6/lib/X11/fonts/misc/fonts.dir 960254165 5
		StringTokenizer tokenizer = new StringTokenizer(line);

		tokenizer.nextToken();// open
		String fileName = tokenizer.nextToken(); // filename

		Time time = new Time(Long.valueOf(tokenizer.nextToken()));// time stamp
		int fileDescriptor = new Integer(tokenizer.nextToken());// fd

		return new OpenEvent(fileName, fileDescriptor, client, client, time);
	}

	private Operation readNextOperation(String nextLine) {

		StringTokenizer tokenizer = new StringTokenizer(nextLine);
		String operation = tokenizer.nextToken();

		if (operation.equals("open")) {
			return Operation.OPEN;
		} else if (operation.equals("read")) {
			return Operation.READ;
		} else if (operation.equals("write")) {
			return Operation.WRITE;
		} else if (operation.equals("close")) {
			return Operation.CLOSE;
		}

		return null;
	}

	private enum Operation {
		OPEN, WRITE, READ, CLOSE
	};

}