package ddg.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ddg.emulator.event.machine.FileSystemActivityEvent;
import ddg.emulator.event.machine.ShutdownEvent;
import ddg.emulator.event.machine.SleepEvent;
import ddg.emulator.event.machine.UserActivityEvent;
import ddg.emulator.event.machine.UserIdlenessEvent;
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
	
	/*
	 * The source of the values below is Lesandro's work: 
	 * "On the Impact of Energy-saving Strategies in Opportunistic Grids"
	 */
	public static final double TRANSITION_POWER_IN_WATTS = 140;
	public static final double ACTIVE_POWER_IN_WATTS = 140;
	public static final double IDLE_POWER_IN_WATTS = 0; //FIXME need to discover the right value
	public static final double SLEEP_POWER_IN_WATTS = 3.33;
	public static final double TO_SLEEP_POWER = 0; //FIXME need to discover the right value
	public static final double FROM_SLEEP_POWER = 0; //FIXME need to discover the right value
	public static final double TO_SHUTDOWN_POWER = 0; //FIXME need to discover the right value
	public static final double FROM_SHUTDOWN_POWER = 0; //FIXME need to discover the right value 
	
	public static final Time SLEEP_TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	public static final Time SHUTDOWN_TRANSITION_DURATION = new Time(60, Unit.SECONDS); //FIXME need to discover the right value
	
	private final Set<DataServer> deployedDataServers;
	private final Set<DDGClient> clients;
	
	/**
	 * The amount of time this machine must wait idle before sleep
	 */
	private final Time timeBeforeSleep;
	
	private final String id;

	private List<FileSystemActivityEvent> pendingFSActivityEvents;
	private String currentStateName;
	private Time currentStateStartTime;
	private Time currentStateSupposedEndTime;
	private double cumulatedFSActivityTime;
	
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

		currentStateName = ShutdownEvent.EVENT_NAME;
		currentStateStartTime = scheduler.now();
		currentStateSupposedEndTime = Time.END_OF_THE_WORLD;
		pendingFSActivityEvents = new ArrayList<FileSystemActivityEvent>();
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
		if(event instanceof UserIdlenessEvent) {
			handleUserIdleness((UserIdlenessEvent) event);
		} else if(event instanceof UserActivityEvent) {
			handleUserActivity((UserActivityEvent) event);
		} else if(event instanceof SleepEvent) {
			handleSleep((SleepEvent) event);
		} else if(event instanceof ShutdownEvent) {
			handleShutdown((ShutdownEvent) event);
		} else if(event instanceof FileSystemActivityEvent) {
			handleFileSystemActivityEvent((FileSystemActivityEvent) event);
		} else {
			throw new IllegalArgumentException(event.toString());
		}
		
	}
	
	private void handleFileSystemActivityEvent(FileSystemActivityEvent fsActivity) {
		/* The new FileSystemActivityEvent must be scheduled to one second after the transition to the next state.
		 * This is done to make sure the event that changes the state was already handled.
		 */
		Time oneSecond = new Time(1, Unit.SECONDS);
		Time now = getScheduler().now();		
		
		if(currentStateName.equals(ShutdownEvent.EVENT_NAME) || currentStateName.equals(SleepEvent.EVENT_NAME)) {
			if(fsActivity.isFromLocalFSClient()) {
				pendingFSActivityEvents.add(fsActivity);
			} else {
				UserIdlenessEvent idlenessEvent = null;
				
				if(currentStateName.equals(SHUTDOWN_TRANSITION_DURATION)) {
					idlenessEvent = buildUserIdlenessEventToWakeUp(SHUTDOWN_TRANSITION_DURATION);
				} else {
					idlenessEvent = buildUserIdlenessEventToWakeUp(SLEEP_TRANSITION_DURATION);
				}
				
				FileSystemActivityEvent newFSActivityEvent = new FileSystemActivityEvent(this, 
						idlenessEvent.getScheduledTime().plus(oneSecond), fsActivity.getDuration(), 
						fsActivity.isFromLocalFSClient());
				
				send(newFSActivityEvent);		
				send(idlenessEvent);
			}
		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
			//if this fs event duration is greater than the duration of the current state
			if(currentStateSupposedEndTime.isEarlierThan(now.plus(fsActivity.getDuration()))) {
				Time duration = now.plus(fsActivity.getDuration()).minus(currentStateSupposedEndTime); 
				FileSystemActivityEvent newFSActivityEvent = new FileSystemActivityEvent(this, 
						currentStateSupposedEndTime.plus(oneSecond), duration, 
						fsActivity.isFromLocalFSClient());
				
				send(newFSActivityEvent);
			}
		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
			//if this fs event duration is greater than the duration of the current state
			if(currentStateSupposedEndTime.isEarlierThan(now.plus(fsActivity.getDuration()))) {
				Time whatHappensInThisState = currentStateSupposedEndTime.minus(now);
				cumulatedFSActivityTime += whatHappensInThisState.asMilliseconds();
				FileSystemActivityEvent newFSActivityEvent = new FileSystemActivityEvent(this, 
						currentStateSupposedEndTime.plus(oneSecond), 
						fsActivity.getDuration().minus(whatHappensInThisState), 
						fsActivity.isFromLocalFSClient());
				
				//TODO tratar os casos em que já aconteceram outro eventos de fs durante esse periodo de ociosidade
				
				send(newFSActivityEvent);
			} else {
				//TODO implementar
			}
		}
	}
	
	private UserIdlenessEvent buildUserIdlenessEventToWakeUp(Time transitionDuration) {
		Time now = getScheduler().now();
		Time idlenessStart = null;

		//checking if the machine has already finished the transition to the current state
		if(now.minus(transitionDuration).isEarlierThan(currentStateStartTime)) {
			idlenessStart = currentStateStartTime.plus(transitionDuration.times(2));	
		} else {
			idlenessStart = now.plus(transitionDuration);
		}
		
		Time userIdlenessDuration = currentStateSupposedEndTime.minus(idlenessStart);
		
		UserIdlenessEvent idlenessEvent = new UserIdlenessEvent(this, idlenessStart, userIdlenessDuration);
		
		return idlenessEvent;
	}

	private void handleShutdown(ShutdownEvent event) {
		Aggregator aggregator = Aggregator.getInstance();
		Time now = getScheduler().now();		
		
		double currentActualDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
			throw new IllegalStateException(String.format("The machine %s was already turned off.", getId()));
			
		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
			System.err.println(String.format("WARNING: A shutdown during a sleep time has occurred in machine %s at " +
					"timestamp %d. We will keep the machine sleeping.", getId(), now.asMilliseconds()));
			
		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
			aggregator.aggregateActiveDuration(getId(), currentActualDuration);
			
		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
			aggregator.aggregateIdleDuration(getId(), currentActualDuration - cumulatedFSActivityTime);
			aggregator.aggregateActiveDuration(getId(), cumulatedFSActivityTime);
			
			cumulatedFSActivityTime = 0;
		}
		
		currentStateName = ShutdownEvent.EVENT_NAME;
		currentStateStartTime = now;
		currentStateSupposedEndTime = currentStateStartTime.plus(event.getDuration());
	}

	private void handleUserActivity(UserActivityEvent event) {
		
		Aggregator aggregator = Aggregator.getInstance();
		Time now = getScheduler().now();		
		
		double currentStateActualDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
			aggregator.aggregateShutdownDuration(getId(), currentStateActualDuration);

		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
			aggregator.aggregateSleepingDuration(getId(), currentStateActualDuration);
			
		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
			throw new IllegalStateException(String.format("The machine %s was already in activity.", getId()));
			
		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
			aggregator.aggregateIdleDuration(getId(), currentStateActualDuration - cumulatedFSActivityTime);
			aggregator.aggregateActiveDuration(getId(), cumulatedFSActivityTime);
			
			cumulatedFSActivityTime = 0;
		}
		
		currentStateName = UserActivityEvent.EVENT_NAME;
		currentStateStartTime = now;
		currentStateSupposedEndTime = currentStateStartTime.plus(event.getDuration());
		
		handlePendingFileSystemActivityEvents();
	}
	
	private void handleUserIdleness(UserIdlenessEvent event) {
		Aggregator aggregator = Aggregator.getInstance();
		Time idlenessDuration = event.getDuration();
		Time now = getScheduler().now();
		
		double currentStateActualDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
			aggregator.aggregateShutdownDuration(getId(), currentStateActualDuration);

		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
			aggregator.aggregateSleepingDuration(getId(), currentStateActualDuration);
			
		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
			aggregator.aggregateActiveDuration(getId(), currentStateActualDuration);
			
		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
			// if the fs wake the machine up it is possible that we have idle to idle transition
			aggregator.aggregateIdleDuration(getId(), currentStateActualDuration - cumulatedFSActivityTime);
			aggregator.aggregateActiveDuration(getId(), cumulatedFSActivityTime);
			
			cumulatedFSActivityTime = 0;
		}
		
		currentStateName = UserIdlenessEvent.EVENT_NAME;
		currentStateStartTime = now;
		
		if(idlenessDuration.isEarlierThan(timeBeforeSleep)) {
			currentStateSupposedEndTime = currentStateStartTime.plus(event.getDuration());
		} else {
			Time bedTime = now.plus(timeBeforeSleep);
			Time duration = now.plus(idlenessDuration).minus(bedTime);
			
			//we must sleep only if we have time to wake up
			if(duration.compareTo(SLEEP_TRANSITION_DURATION.plus(SLEEP_TRANSITION_DURATION)) >= 0) { 
				aggregator.aggregateIdleDuration(getId(), timeBeforeSleep.asMilliseconds());
				
				send(new SleepEvent(this, bedTime));
				
				currentStateSupposedEndTime = currentStateStartTime.plus(duration);
			} else {
				currentStateSupposedEndTime = currentStateStartTime.plus(event.getDuration());
			}
		}
		
		handlePendingFileSystemActivityEvents();
	}
	
	private void handleSleep(SleepEvent event) {
		Aggregator aggregator = Aggregator.getInstance();
		Time now = getScheduler().now();		
		
		double currentStateActualDuration = now.minus(currentStateStartTime).asMilliseconds();
		
		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
			aggregator.aggregateShutdownDuration(getId(), currentStateActualDuration);

		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
			throw new IllegalStateException(String.format("The machine %s was already sleeping.", getId()));
			
		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
			aggregator.aggregateActiveDuration(getId(), currentStateActualDuration);
			
		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
			aggregator.aggregateIdleDuration(getId(), currentStateActualDuration - cumulatedFSActivityTime);
			aggregator.aggregateActiveDuration(getId(), cumulatedFSActivityTime);
			
			cumulatedFSActivityTime = 0;
		}
		
		currentStateName = SleepEvent.EVENT_NAME;
		currentStateStartTime = now;
		currentStateSupposedEndTime = currentStateStartTime.plus(event.getDuration());
	}
	
	private void handlePendingFileSystemActivityEvents() {
		for(FileSystemActivityEvent fsActivityEvent : pendingFSActivityEvents) {
			handleFileSystemActivityEvent(fsActivityEvent);
		}
		
		pendingFSActivityEvents.clear();
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
}
