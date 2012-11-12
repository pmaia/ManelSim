package simulation.beefs.event.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import simulation.beefs.model.FileSystemClient;
import core.Event;
import core.EventSource;
import core.Time;
import core.Time.Unit;

/**
 * A parser for the trace of calls to the file system.
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FileSystemTraceEventSource implements EventSource {

	private final BufferedReader bufferedReader;

	private FileSystemClient client;

	public FileSystemTraceEventSource(FileSystemClient client, InputStream traceStream) {
		
		this.bufferedReader = new BufferedReader(new InputStreamReader(traceStream));
		this.client = client;
	}
	
	public void setClient(FileSystemClient newClient) {
		this.client = newClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Event getNextEvent() {

		String traceLine;

		try {
			traceLine = readNextLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (traceLine == null)
			return null;

		try {
			StringTokenizer tokenizer = new StringTokenizer(traceLine);
			String operation = tokenizer.nextToken();

			if (operation.equals("read")) {
				return parseReadEvent(tokenizer);
			} else if (operation.equals("write")) {
				return parseWriteEvent(tokenizer);
			} else if (operation.equals("close")) {
				return parseCloseEvent(tokenizer);
			} else if (operation.equals("unlink")) {
				return parseUnlinkEvent(tokenizer);
			} else {
				return getNextEvent();
			}
		} catch(Throwable t ) {
			System.err.println("Warning: Bad format line: " + traceLine);
			return getNextEvent();
		}
	}

	private String readNextLine() throws IOException {

		String readLine = null;

		while ((readLine = bufferedReader.readLine()) != null) {

			if (!readLine.trim().equals("") && !readLine.startsWith("#")) {
				return readLine;
			}

		}

		return readLine;
	}
	
	private Unlink parseUnlinkEvent(StringTokenizer tokenizer) {
		// unlink  begin-elapsed   fullpath
		
		Time time = parseTime(tokenizer.nextToken())[0];
		String targetPath = tokenizer.nextToken();
		
		return new Unlink(client, time, targetPath);
	}

	private Close parseCloseEvent(StringTokenizer tokenizer) {
		// close   begin-elapsed   fullpath

		Time time = parseTime(tokenizer.nextToken())[0];
		String targetPath = tokenizer.nextToken();

		return new Close(client, time, targetPath);
	}

	private Read parseReadEvent(StringTokenizer tokenizer) {
		// read    begin-elapsed   fullpath        length
		
		Time [] timestampAndDuration = parseTime(tokenizer.nextToken());
		String filePath = tokenizer.nextToken();
		long length = Long.parseLong(tokenizer.nextToken());

		return new Read(client, timestampAndDuration[0], timestampAndDuration[1], filePath, length);
	}

	private Write parseWriteEvent(StringTokenizer tokenizer) {
		// write   begin-elapsed   fullpath        bytes_transfered	file_size
		
		Time [] timestampAndDuration = parseTime(tokenizer.nextToken());
		String filePath = tokenizer.nextToken();
		long bytesTransfered = Long.parseLong(tokenizer.nextToken());
		long fileSize = Long.parseLong(tokenizer.nextToken());

		return new Write(client, timestampAndDuration[0], timestampAndDuration[1], filePath,
				bytesTransfered, fileSize); 
	}
	
	private Time [] parseTime(String traceTimestamp) {
		Time [] parsedTimes = new Time[2];
		
		String [] timestampAndDuration = traceTimestamp.split("-");
		
		parsedTimes[0] = 
				new Time(Long.parseLong(timestampAndDuration[0]), Unit.MICROSECONDS);
		parsedTimes[1] =
				new Time(Long.parseLong(timestampAndDuration[1]), Unit.MICROSECONDS);
		
		return parsedTimes; 
	}

}