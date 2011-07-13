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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import ddg.emulator.events.fuseEvents.OpenEvent;
import ddg.emulator.events.fuseEvents.ReadEvent;
import ddg.emulator.events.fuseEvents.ReleaseEvent;
import ddg.emulator.events.fuseEvents.WriteEvent;
import ddg.kernel.JEEvent;
import ddg.kernel.JETime;
import ddg.model.DDGClient;
import ddg.model.loginAlgorithm.LoginAlgorithm;

/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public class SeerParserAndEventInjector implements ClientEventInjector {

	private long lastTimeStamp;

	private long firstTimeStamp;

	private final BufferedReader bufferedReader;
	private final LoginAlgorithm loginAlgorithm;

	/**
	 * @param traceFile
	 * @throws IOException
	 */
	public SeerParserAndEventInjector(File traceFile,
			LoginAlgorithm loginAlgorithm) throws IOException {
		this.lastTimeStamp = -1;
		this.bufferedReader = new BufferedReader(new FileReader(traceFile));
		this.loginAlgorithm = loginAlgorithm;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JEEvent getNextEvent() {

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

	private JEEvent readCloseEvent(String nextLine) {

		// close 960254662.240045 3
		StringTokenizer tokenizer = new StringTokenizer(nextLine);

		tokenizer.nextToken();// close
		long time = Long.valueOf(tokenizer.nextToken());// time stamp
		tokenizer.nextToken();// fd

		advanceTime(time);
		DDGClient client = chooseClient(lastTimeStamp);

		return new ReleaseEvent(null, client, now());
	}

	private JETime now() {
		return new JETime(lastTimeStamp - firstTimeStamp);
	}

	private void advanceTime(long time) {

		if (lastTimeStamp == -1) {
			firstTimeStamp = time;
		}

		lastTimeStamp = time;
	}

	private DDGClient chooseClient(long now) {
		return loginAlgorithm.sampleClient(now);
	}

	private JEEvent readReadEvent(String traceLine) {

		// read 960254162.929275 3 4096

		StringTokenizer tokenizer = new StringTokenizer(traceLine);

		tokenizer.nextToken();// read
		long time = Long.valueOf(tokenizer.nextToken());// time stamp
		int fileDescriptor = new Integer(tokenizer.nextToken());// fd
		long dataLength = Long.parseLong(tokenizer.nextToken());// length

		advanceTime(time);
		DDGClient client = chooseClient(lastTimeStamp);

		return new ReadEvent(dataLength, 0, fileDescriptor, client, now(),
				client);
	}

	private JEEvent readWriteEvent(String traceLine) {

		// write 960254662.240004 3 1137

		StringTokenizer tokenizer = new StringTokenizer(traceLine);

		tokenizer.nextToken();// write
		long time = Long.valueOf(tokenizer.nextToken());// time
		int fileDescriptor = new Integer(tokenizer.nextToken());// fd
		long dataLength = Long.parseLong(tokenizer.nextToken());// length

		advanceTime(time);
		DDGClient client = chooseClient(lastTimeStamp);

		return new WriteEvent(dataLength, 0, fileDescriptor, client, now(),
				client);
	}

	private JEEvent readOpenEvent(String line) {

		// open /usr/X11R6/lib/X11/fonts/misc/fonts.dir 960254165.543422 5
		StringTokenizer tokenizer = new StringTokenizer(line);

		tokenizer.nextToken();// open
		String fileName = tokenizer.nextToken(); // filename

		long time = Long.valueOf(tokenizer.nextToken());// time stamp
		int fileDescriptor = new Integer(tokenizer.nextToken());// fd

		advanceTime(time);
		DDGClient client = chooseClient(lastTimeStamp);

		return new OpenEvent(fileName, fileDescriptor, client, client, now());
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