package ddg.emulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import ddg.model.Availability;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public class FTATraceAvailability implements Availability {
	
	public final FileReader fileReader;
	
	public FTATraceAvailability(File traceFile) throws FileNotFoundException {
		this.fileReader = new FileReader(traceFile);
	}

	@Override
	public long nextAvailabilityDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long nextUnavailabilityDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

}
