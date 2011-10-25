package ddg.model;

import java.util.ArrayList;
import java.util.List;

import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JEEventScheduler;
import ddg.model.data.DataServer;

/**
 * Models a machine. This machine can hold a number of clients and data servers.
 * 
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 * @author Thiago Emmanuel Pereira da Cunha Silva - thiagoepdc@lsd.ufcg.edu.br
 */
public class Machine extends JEEventHandler {

	/*
	 * The source of the values below is Lesandro's work: 
	 * "On the Impact of Energy-saving Strategies in Opportunistic Grids"
	 */
	public static final double TRANSITION_POWER_IN_WATTS = 140;
	public static final double ACTIVE_POWER_IN_WATTS = 140;
	public static final double STAND_BY_POWER_IN_WATTS = 3.33;
	public static final long TRANSITION_DURATION_IN_MILLISECONDS = 2500;
	
	private final List<DataServer> deployedDataServers;
	private final List<DDGClient> clients;
	
	private final String id;

	public Machine(JEEventScheduler scheduler, String id) {
		super(scheduler);
		
		this.id = id;
		this.deployedDataServers = new ArrayList<DataServer>();
		this.clients = new ArrayList<DDGClient>();
	}
	
	public boolean isBeingUsed() {
		return true; //FIXME this code must not exist anymore
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
	
	@Override
	public void handleEvent(JEEvent jeevent) { //TODO implementar
//		if(jeevent instanceof MachineStateTransitionEvent) {
//			State justEndedState = availability.currentState(); 
//			if(justEndedState.isActive()) {
//				Aggregator.getInstance().aggregateActiveDuration(getId(), justEndedState.getDuration());
//			} else {
//				Aggregator.getInstance().aggregateInactiveDuration(getId(), justEndedState.getDuration());
//			}
//			
//			availability.advanceState();
//			lastTransitionTime = super.getScheduler().now();
//			JETime nextTransition = 
//				lastTransitionTime.plus(new JETime(availability.currentState().getDuration()));
//			
//			pendingTransition = new MachineStateTransitionEvent(this, nextTransition); 
//			
//			this.send(pendingTransition);
//		} else {
//			throw new IllegalArgumentException();
//		}
	}
}
