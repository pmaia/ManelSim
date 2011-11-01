package ddg.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ddg.emulator.FileSystemEventParser;

/**
 * 
 * An InputStream that simulates an InputStream over a trace file whose events are in the format expected by 
 * {@link FileSystemEventParser}. <code>numberOfEvents</code> events will be generated.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FakeFileSystemTraceStream extends FakeTraceStream {

	private static final String [] operations = {"read", "write", "open", "close"};

	public FakeFileSystemTraceStream(int numberOfEvents) {
		super(numberOfEvents);
	}

	//TODO I implemented this to generate lines similar to the ones found in the cleared SEER traces.
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

		nextTimeStamp += random.nextInt(1000);

		return new ByteArrayInputStream(strBuilder.toString().getBytes());
	}

}