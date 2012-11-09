package simulation.beefs.model;

/**
 * 
 * @author Patrick Maia
 */
public class DataServer {
	
	//DataServer does not track its own files. We keep this logic in ReplicatedFile. 
	//It saves a lot of memory

	private long totalSpaceBytes;
	private long usedSpace = 0;
	
	private final Machine host;

	public DataServer(Machine host, long totalSpaceBytes) {
		this.host = host;
		this.totalSpaceBytes = totalSpaceBytes;
	}

	public Machine getHost() {
		return host;
	}
	
	public void consume(long bytesToConsume) {
		
		if (bytesToConsume < 0) {
			throw new IllegalArgumentException("Cannot consume negative bytes: " +
												+ bytesToConsume);
		}
		
		if (availableSpace() < bytesToConsume) {
			throw new RuntimeException("no space available: " + availableSpace()
										+ " requestedSpace: " + bytesToConsume);
		}
	}
	
	public void release(long bytesToRelease) {
		
		if (bytesToRelease < 0) {
			throw new IllegalArgumentException("Cannot release negative bytes: " +
												+ bytesToRelease);
		}
		
		if ((availableSpace() + bytesToRelease) > totalSpaceBytes) {
			throw new RuntimeException("resource misbalance ? " +
					"available: " + availableSpace() + 
					"bytesToRelease: " + bytesToRelease +
					"totalSpace: " + totalSpaceBytes);
		}
		
		this.usedSpace -= bytesToRelease;
	}
	
	public long availableSpace() {
		return totalSpaceBytes - usedSpace;
	}

}