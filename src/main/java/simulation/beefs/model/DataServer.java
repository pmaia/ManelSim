package simulation.beefs.model;

import java.util.ArrayList;
import java.util.List;

import core.Time;
import core.TimeInterval;

/**
 * 
 * @author Patrick Maia
 *
 */
public class DataServer {

	private List<TimeInterval> writeIntervals = new ArrayList<TimeInterval>();

	public List<TimeInterval> getWriteIntervals() {
		return new ArrayList<TimeInterval>(writeIntervals);
	}

	public String getHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public void reportWrite(Time start, Time duration) {
		TimeInterval newInterval = new TimeInterval(start, start.plus(duration));

		List<TimeInterval> updatedWriteIntervalsList = new ArrayList<TimeInterval>();
		updatedWriteIntervalsList.add(newInterval);

		for(TimeInterval writeInterval : writeIntervals) {
			if(writeInterval.overlaps(newInterval)) {
				updatedWriteIntervalsList.remove(newInterval);
				newInterval = writeInterval.merge(newInterval);
				updatedWriteIntervalsList.add(newInterval);
			} else {
				updatedWriteIntervalsList.add(writeInterval);
			}
		}

		writeIntervals = updatedWriteIntervalsList;
	}

}
