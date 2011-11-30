package ddg.model;

import java.util.HashSet;
import java.util.Set;

import ddg.emulator.event.machine.IdlenessEvent;
import ddg.emulator.event.machine.ShutdownEvent;
import ddg.emulator.event.machine.SleepEvent;
import ddg.emulator.event.machine.WakeUpEvent;
import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.EventScheduler;
import ddg.kernel.Time;
import ddg.kernel.Time.Unit;
import ddg.model.data.DataServer;

/**
 * Models a machine. This machine can hold a number of clients and data servers.
 * 
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 * @author Thiago Emmanuel Pereira da Cunha Silva - thiagoepdc@lsd.ufcg.edu.br
 */
public class Machine extends EventHandler {
	
	private enum State { ACTIVE, IDLE, SLEEPING, SHUTDOWN };

	/*
	 * The source of the values below is Lesandro's work: 
	 * "On the Impact of Energy-saving Strategies in Opportunistic Grids"
	 */
	public static final double TRANSITION_POWER_IN_WATTS = 140;
	public static final double ACTIVE_POWER_IN_WATTS = 140;
	public static final double IDLE_POWER_IN_WATTS = 0; //FIXME need to discover the right value
	public static final double STAND_BY_POWER_IN_WATTS = 3.33;
	public static final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	
	private final Set<DataServer> deployedDataServers;
	private final Set<DDGClient> clients;
	
	/**
	 * The amount of time this machine must wait idle before sleep
	 */
	private final Time timeBeforeSleep;
	
	private final String id;
	
	private State currentState;
	private Time currentStateStartTime;
	private Time currentStateEndTime;

	/**
	 * 
	 * @param scheduler
	 * @param id
	 * @param timeBeforeSleep
	 */
	public Machine(EventScheduler scheduler, String id, long timeBeforeSleep) {
		super(scheduler);
		
		this.id = id;
		this.deployedDataServers = new HashSet<DataServer>();
		this.clients = new HashSet<DDGClient>();
		this.timeBeforeSleep = new Time(timeBeforeSleep, Unit.SECONDS);

		this.currentState = State.SHUTDOWN;
		this.currentStateStartTime = scheduler.now();
		this.currentStateEndTime = null;
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
	public Set<DataServer> getDeployedDataServers() {
		return deployedDataServers;
	}

	/**
	 * @return
	 */
	public Set<DDGClient> getDeployedClients() {
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
	public boolean bindClient(DDGClient newClient) {
		return clients.add(newClient);
	}

	/**
	 * @param newDataServer
	 * @return
	 */
	public boolean deploy(DataServer newDataServer) {
		return deployedDataServers.add(newDataServer);
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
	public void handleEvent(Event event) {
		if(event instanceof IdlenessEvent) {
			handleIdleness((IdlenessEvent) event);
		} else if(event instanceof WakeUpEvent) {
			handleWakeUp((WakeUpEvent) event);
		} else if(event instanceof SleepEvent) {
			handleSleep((SleepEvent) event);
		} else if(event instanceof ShutdownEvent) {
			handleShutdown((ShutdownEvent) event);
		} else {
			throw new IllegalArgumentException(event.toString());
		}
		
	}
	
	private void handleShutdown(ShutdownEvent event) {
		
		Aggregator aggregator = Aggregator.getInstance();
		Time now = getScheduler().now();
		double currentStateDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		switch(currentState) {
		case ACTIVE: 
			throw new IllegalStateException("There is no ACTIVE -> SHUTDOWN transitions.");
		case IDLE: 
			aggregator.aggregateIdleDuration(getId(), currentStateDuration); break;
		case SLEEPING: 
			aggregator.aggregateSleepingDuration(getId(), currentStateDuration); break;
		case SHUTDOWN: 
			throw new IllegalStateException("This machine is already turned off");
		}
		
		currentState = State.SHUTDOWN;
		currentStateStartTime = now;
		currentStateEndTime = now.plus(event.getDuration());
	}

	private void handleWakeUp(WakeUpEvent wakeUp) {
		Aggregator aggregator = Aggregator.getInstance();
		Time now = getScheduler().now();
		
		double currentStateDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		switch(currentState) {
		case ACTIVE: 
			throw new IllegalStateException("This machine is already active");
		case IDLE: 
			aggregator.aggregateIdleDuration(getId(), currentStateDuration); break;
		case SLEEPING: 
			aggregator.aggregateSleepingDuration(getId(), currentStateDuration); break;
		case SHUTDOWN: 
			aggregator.aggregateShutdownDuration(getId(), currentStateDuration); break;
		}
		
		Time activityEnd = now.plus(wakeUp.getDuration());
		
		if(wakeUp.wasCausedByTheOpportunisticFS() && activityEnd.isEarlierThan(currentStateEndTime)) {
			send(new IdlenessEvent(this, activityEnd, currentStateEndTime.minus(activityEnd)));
		}
		
		currentState = State.ACTIVE;
		currentStateStartTime = now;
		currentStateEndTime = activityEnd;
	}
	
	private void handleIdleness(IdlenessEvent event) {
		Aggregator aggregator = Aggregator.getInstance();
		Time idlenessDuration = event.getIdlenessDuration();
		Time now = getScheduler().now();
		
		double currentStateDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		switch(currentState) {
		case ACTIVE: 
			aggregator.aggregateActiveDuration(getId(), currentStateDuration); break;
		case IDLE: 
			throw new IllegalStateException("This machine is already idle");
		case SLEEPING: 
			aggregator.aggregateSleepingDuration(getId(), currentStateDuration); break;
		case SHUTDOWN: 
			aggregator.aggregateShutdownDuration(getId(), currentStateDuration); break;
		}
		
		if(!idlenessDuration.isEarlierThan(timeBeforeSleep)) {
			Time bedTime = now.plus(timeBeforeSleep);
			Time duration = currentStateEndTime.minus(bedTime);
			
			send(new SleepEvent(this, bedTime, duration));
		}
		
		currentState = State.IDLE;
		currentStateStartTime = now;
		currentStateEndTime = now.plus(idlenessDuration);
	}
	
	private void handleSleep(SleepEvent event) {
		Aggregator aggregator = Aggregator.getInstance();
		Time now = getScheduler().now();
		
		double currentStateDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		switch(currentState) {
		case ACTIVE: 
			throw new IllegalStateException("There is no ACTIVE -> SLEEP transitions.");
		case IDLE: 
			aggregator.aggregateIdleDuration(getId(), currentStateDuration); break;
		case SLEEPING: 
			throw new IllegalStateException("This machine is already sleeping");
		case SHUTDOWN: 
			throw new IllegalStateException("There is no SHUTDOWN -> SLEEP transitions.");
		}
		
		currentState = State.SLEEPING;
		currentStateStartTime = now;
		currentStateEndTime = now.plus(event.getDuration());
	}
}
