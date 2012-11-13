package simulation.beefs.model;

import java.util.Collection;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullpath == null) ? 0 : fullpath.hashCode());
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
		ReplicatedFile other = (ReplicatedFile) obj;
		if (fullpath == null) {
			if (other.fullpath != null)
				return false;
		} else if (!fullpath.equals(other.fullpath))
			return false;
		return true;
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
		
		try {
			primary.update(getFullPath(), size);
		} catch (InsufficientSpaceException e) {
			
			Collection<String> purgedReplicas = primary.purge(e.remainder);
			if (purgedReplicas.isEmpty()) {
				throw new InsufficientSpaceException(
						"DataServer purge did not reclaim enough space",
						e.request, e.remainder);
			} else {
				//FIXME: schedule repair process
			}
		}
		
		Set<DataServer> secsToRemove = new HashSet<DataServer>();
		for (DataServer sec : secondaries) {
			try {
				sec.update(getFullPath(), size);
			} catch (InsufficientSpaceException e) {
				sec.delete(getFullPath());
				secsToRemove.add(sec);
			}
		}
		
		secondaries.removeAll(secsToRemove);
		//FIXME: schedule repair event
	}
	
	public boolean areReplicasConsistent() {
		return replicasAreConsistent;
	}
	
	public void setReplicasAreConsistent(boolean replicasAreConsistent) {
		this.replicasAreConsistent = replicasAreConsistent;
	}
	
}