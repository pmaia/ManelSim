package ddg.model;

import java.util.ArrayList;
import java.util.List;

import ddg.emulator.EmulatorControl;
import ddg.kernel.JEEventScheduler;
import ddg.kernel.JETime;
import ddg.model.data.DataServer;

/**
 * Models a machine. This machine can hold a number of clients and data servers.
 * 
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 * @author Thiago Emmanuel Pereira da Cunha Silva - thiagoepdc@lsd.ufcg.edu.br
 */
public class Machine {

	private final List<DataServer> deployedDataServers;
	private final List<DDGClient> clients;
	
	private final Availability availability;

	private final String id;

	/**
	 * Default constructor using fields.
	 * 
	 * @param scheduler
	 * @param id
	 */
	public Machine(JEEventScheduler scheduler, Availability availability, String id) {
		this.id = id;
		this.deployedDataServers = new ArrayList<DataServer>();
		this.clients = new ArrayList<DDGClient>();
		this.availability = availability;
	}
	
	public boolean isBeingUsed() {
		JETime now = 
			EmulatorControl.getInstance().getTheUniqueEventScheduler().now();
		
		availability.updateSimulationTime(now);
		
		return !availability.isAvailable();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the deployedDataServers
	 */
	public List<DataServer> getDeployedDataServers() {
		return deployedDataServers;
	}

	/**
	 * @return
	 */
	public List<DDGClient> getDeployedClients() {
		return clients;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Machine other = (Machine) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @param newClient
	 * @return
	 */
	public int bindClient(DDGClient newClient) {
		clients.add(newClient);
		return clients.indexOf(newClient);
	}

	/**
	 * @param newDataServer
	 * @return
	 */
	public int deploy(DataServer newDataServer) {
		deployedDataServers.add(newDataServer);
		return deployedDataServers.indexOf(newDataServer);
	}

	/**
	 * @param newDataServer
	 */
	public boolean isDeployed(DataServer dataServer) {
		return deployedDataServers.contains(dataServer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "machine" + id;
	}
}
