package ddg.test;

import java.io.InputStream;

import ddg.emulator.UserIdlenessEventParser;

/**
 * 
 * An InputStream that simulates an InputStream over a trace file whose events are in the format expected by 
 * {@link UserIdlenessEventParser}. <code>numberOfEvents</code> events will be generated.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class FakeUserIdlenessTraceStream extends FakeTraceStream {

	public FakeUserIdlenessTraceStream(int numberOfEvents) {
		super(numberOfEvents);
	}

	@Override
	protected InputStream generateNextEventStream() {
		return null;
	}

}