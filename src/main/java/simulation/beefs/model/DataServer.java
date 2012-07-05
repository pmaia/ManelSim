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
	private List<TimeInterval> readIntervals = new ArrayList<TimeInterval>();

	public List<TimeInterval> getWriteIntervals() {
		return new ArrayList<TimeInterval>(writeIntervals);
	}
	
	public List<TimeInterval> getReadIntervals() {
		return new ArrayList<TimeInterval>(readIntervals);
	}

	public String getHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public void reportWrite(Time start, Time duration) {
		writeIntervals = reportOperation(start, duration, writeIntervals);
	}

	public void reportRead(Time start, Time duration) {
		readIntervals = reportOperation(start, duration, readIntervals);
	}
	
	private List<TimeInterval> reportOperation(Time start, Time duration, List<TimeInterval> originalIntervals) {
		TimeInterval newInterval = new TimeInterval(start, start.plus(duration));

		List<TimeInterval> updatedIntervalsList = new ArrayList<TimeInterval>();
		updatedIntervalsList.add(newInterval);

		for(TimeInterval interval : originalIntervals) {
			if(interval.overlaps(newInterval)) {
				updatedIntervalsList.remove(newInterval);
				newInterval = interval.merge(newInterval);
				updatedIntervalsList.add(newInterval);
			} else {
				updatedIntervalsList.add(interval);
			}
		}

		return updatedIntervalsList;
	}

}
