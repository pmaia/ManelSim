package simulation.beefs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import core.Time;
import core.TimeInterval;

/**
 * 
 * @author Patrick Maia
 * @author manel
 */
public class DataServer {
	
	private List<TimeInterval> writeIntervals = new ArrayList<TimeInterval>();
	private List<TimeInterval> readIntervals = new ArrayList<TimeInterval>();

	private long totalSpaceBytes;
	private long usedSpace = 0;
	
	private Map<String, Long> primaries = new HashMap<String, Long>();
	private Map<String, Long> secs = new HashMap<String, Long>();
	
	private final Machine host;
	private final String id = UUID.randomUUID().toString();
	
	public DataServer(Machine host) {
		this(host, Long.MAX_VALUE);
	}

	public DataServer(Machine host, long totalSpaceBytes) {
		this.host = host;
		this.totalSpaceBytes = totalSpaceBytes;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataServer other = (DataServer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getHost().toString() + "-" + id;
	}
	
	public Machine getHost() {//can we remove it ?FIXME:
		return host;
	}
	
	public List<TimeInterval> getWriteIntervals() {
		return new ArrayList<TimeInterval>(writeIntervals);
	}
	
	public List<TimeInterval> getReadIntervals() {
		return new ArrayList<TimeInterval>(readIntervals);
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
	
	public void createReplica(String fullpath, boolean primary) {
		
		if (primaries.containsKey(fullpath) || secs.containsKey(fullpath)) {
			throw new IllegalArgumentException("File: " + fullpath +
					" already exists.");
		}
		
		((primary) ? primaries : secs).put(fullpath, 0L); 
	}
	
	public void update(String fullpath, long newSize) {
		
		if (newSize < 0) {
			throw new IllegalArgumentException("File sizes cannot be negative: " +
				+ newSize);
		}
		
		Map<String, Long> pool = selectPool(fullpath);
		if (pool == null) {
			throw new IllegalArgumentException("File not found: " + fullpath);
		}
		
		long oldSize = pool.get(fullpath);
		long increment = newSize - oldSize;
		
		if (increment > availableSpace()) {
			throw new InsufficientSpaceException(increment, (increment - availableSpace())); 
		}
		
		pool.put(fullpath, newSize);
		this.usedSpace += increment;
	}
	 
	public long size(String fullpath) {
		
		Map<String, Long> pool = selectPool(fullpath);
		if (pool == null) {
			throw new IllegalArgumentException("File not found: " + fullpath);
		}
		
		return pool.get(fullpath);
	}
	
	public class InsufficientSpaceException extends RuntimeException {
		
		public final long request;
		public final long remainder;

		public InsufficientSpaceException(long request, long remainder) {
			this.request = request;
			this.remainder = remainder;
		}
	}
	
	private Map<String, Long> selectPool(String fullpath) {
		
		if (primaries.containsKey(fullpath)) {
			return primaries;
		}
		
		if (secs.containsKey(fullpath)) {
			return secs;
		}
		
		return null;
	}
	
	public long totalSpace() {
		return totalSpaceBytes;
	}
	
	public long availableSpace() {
		return totalSpaceBytes - usedSpace;
	}

}