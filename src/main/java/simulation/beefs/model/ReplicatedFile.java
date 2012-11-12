package simulation.beefs.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Patrick Maia
 */
public class ReplicatedFile {
	
	private final String fullpath;
	private final DataServer primary;
	private final Set<DataServer> secondaries;
	
	private boolean replicasAreConsistent = true;
	
	/**
	 * Creates a {@link ReplicatedFile} abstraction to model
	 * a distributed store.
	 *  
	 * @param fullpath
	 * @param primary
	 * @param secondaries
	 */
	public ReplicatedFile(String fullpath, DataServer primary, Set<DataServer> secondaries) {
		//FIXME: rlevel
		this.fullpath = fullpath;
		this.primary = primary;
		this.secondaries = secondaries;
		
		this.primary.createReplica(fullpath, true);
		for (DataServer sec : secondaries) {
			sec.createReplica(fullpath, false);
		}
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
		return primary.size(getFullPath());
	}
	
	public void setSize(long size) {
		primary.update(getFullPath(), size);
		for (DataServer sec : secondaries) {
			sec.update(getFullPath(), size);
		}
	}
	
	public boolean areReplicasConsistent() {
		return replicasAreConsistent;
	}
	
	public void setReplicasAreConsistent(boolean replicasAreConsistent) {
		this.replicasAreConsistent = replicasAreConsistent;
	}
	
}