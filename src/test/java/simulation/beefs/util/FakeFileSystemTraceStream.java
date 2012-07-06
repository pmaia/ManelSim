package simulation.beefs.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import simulation.beefs.event.filesystem.source.FileSystemTraceEventSource;


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
	
	private final String SEPARATOR = "\t";
	private final String LINE_SEPARATOR = "\n";
	
	private final String filePath;
	private final long operationDuration; 
	private final int bytesTransfered;
	private final long fileSize;

	private static final String [] operations = {"read", "write", "close", "unlink"};

	public FakeFileSystemTraceStream(int numberOfEvents, String filePath, long opDuration, int bytesTransfered, 
			long fileSize) {
		super(numberOfEvents);
		
		this.filePath = filePath;
		this.operationDuration = opDuration;
		this.bytesTransfered = bytesTransfered;
		this.fileSize = fileSize;
	}

	protected InputStream generateNextEventStream() {
		StringBuilder strBuilder = new StringBuilder();

		String op = operations[random.nextInt(operations.length)];

		strBuilder.append(op);
		strBuilder.append(SEPARATOR);

		strBuilder.append(nextTimeStamp);
		strBuilder.append("-");
		strBuilder.append(operationDuration);
		strBuilder.append(SEPARATOR);
		strBuilder.append(filePath);
		strBuilder.append(SEPARATOR);

		if(op.equals("read") || op.equals("write")) {
			strBuilder.append(bytesTransfered);
			
			if(op.equals("write")) {
				strBuilder.append(SEPARATOR);
				strBuilder.append(fileSize);
			}
		}

		strBuilder.append(LINE_SEPARATOR);
		
		nextTimeStamp += random.nextInt(5000);

		return new ByteArrayInputStream(strBuilder.toString().getBytes());
	}

}