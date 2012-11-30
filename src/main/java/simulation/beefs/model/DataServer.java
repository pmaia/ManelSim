package simulation.beefs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Time;
import core.TimeInterval;

/**
 * 
 * @author Patrick Maia
 * @author manel
 */
public class DataServer {
	
	private static final Logger logger = LoggerFactory.getLogger(DataServer.class);
	
	private List<TimeInterval> writeIntervals = new ArrayList<TimeInterval>();
	private List<TimeInterval> readIntervals = new ArrayList<TimeInterval>();

	private long totalSpaceBytes;
	private long usedSpace = 0;
	
	private Map<String, Long> primaries = new HashMap<String, Long>();
	private Map<String, Long> secs = new HashMap<String, Long>();
	
	private final Machine host;
	private final String id = UUID.randomUUID().toString();

	private final boolean reportOps;
	
	public DataServer(Machine host) {
		this(host, Long.MAX_VALUE);
	}

	public DataServer(Machine host, long totalSpaceBytes) {
		this(host, totalSpaceBytes, true);
	}
	
	public DataServer(Machine host, long totalSpaceBytes, boolean reportOps) {
		this.host = host;
		this.totalSpaceBytes = totalSpaceBytes;
		//read and write reports are burns cpu. I do not have time to improve the code
		//so I added this flags to bypass them.
		this.reportOps = reportOps;
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
		if (reportOps) {
			writeIntervals = reportOperation(start, duration, writeIntervals);
		}
	}

	public void reportRead(Time start, Time duration) {
		if (reportOps) {
			readIntervals = reportOperation(start, duration, readIntervals);
		}
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
	
	public boolean contains(String fullpath) {
		return (primaries.containsKey(fullpath) || secs.containsKey(fullpath));
	}
	
	public void createReplica(String fullpath, boolean primary) {
		
		if (contains(fullpath)) {
			throw new IllegalArgumentException("File: " + fullpath +
					" already exists.");
		}
		
		logger.debug("creating file={} primary={} ds={}", new Object[] {fullpath, primary, this});
		
		((primary) ? primaries : secs).put(fullpath, 0L);
	}

	public long secondaryUsedSpace() {
		//for the debug purpose, takes time, take care
		long space = 0;
		for(Long size: this.secs.values()) {
			space += size;
		}
		return space;
	}
	
	/**
	 * Remove a number of secondaries replicas until toReclaim bytes are
	 * reclaimed. If it not possible to reclaim the requested amount of data
	 * no replica is purged.
	 * 
	 * @param toReclaim
	 * @return A {@link Set} of fullpath {@link String} from purged replicas. 
	 * 	If we cannot reclaim the requested amount of data, we return an
	 *  empty {@link Set}.
	 */
	public Collection<String> purge(long toReclaim) {
		
		//if (usedSpace > toReclaim) {
		//	return Collections.EMPTY_SET;
		//}
		
		Set<String> toPurge = new HashSet<String>();
		
		//It takes circa 0.6 ms to shuffle a 30k (my guess it will take 1ms)
		//... and, even though there are a number of ways to make it efficient, I'm
		//not going to make it.
		List<String> samplePaths = new LinkedList<String>(this.secs.keySet()); 
		Collections.shuffle(samplePaths);
		
		long reclaimed = 0;
		for (String fullpath : samplePaths) {
			
			toPurge.add(fullpath);
			reclaimed += secs.get(fullpath);
			
			if (reclaimed >= toReclaim) {
				for (String pathToPurge : toPurge) {
					delete(pathToPurge);
				}
				break;
			}
		}
		
		return toPurge;
	}
	
	public void delete(String fullpath) {
		
		Map<String, Long> pool = selectPool(fullpath);
		if (pool == null) {
			throw new IllegalArgumentException("File not found: " + fullpath);
		}
		
		logger.info("deleting file={} from ds={}", fullpath, toString());
		
		long fileSize = pool.remove(fullpath);
		this.usedSpace -= fileSize;
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
