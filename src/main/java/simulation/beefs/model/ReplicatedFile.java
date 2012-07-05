package simulation.beefs.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Patrick Maia
 *
 */
public class ReplicatedFile {
	
	private final String fullpath;
	private final DataServer primary;
	private final Set<DataServer> secondaries;
	
	private long size = 0;
	private boolean replicasAreConsistent = true;
	
	public ReplicatedFile(String fullpath, DataServer primary, Set<DataServer> secondaries) {
		this.fullpath = fullpath;
		this.primary = primary;
		this.secondaries = secondaries;
	}

	public DataServer getPrimary() {
		return primary;
	}
	
	public Set<DataServer> getSecondaries() {
		return new HashSet<DataServer>(secondaries);
	}
	
	public String getFullPath() {
		return fullpath;
	}

	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public boolean areReplicasConsistent() {
		return replicasAreConsistent;
	}
	
	public void setReplicasAreConsistent(boolean replicasAreConsistent) {
		this.replicasAreConsistent = replicasAreConsistent;
	}

}
