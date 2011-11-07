package ddg.model;

import ddg.emulator.EmulatorControl;
import ddg.emulator.events.WakeUp;
import ddg.emulator.events.filesystem.CloseEvent;
import ddg.emulator.events.filesystem.ReadEvent;
import ddg.emulator.events.filesystem.UnlinkEvent;
import ddg.emulator.events.filesystem.WriteEvent;
import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.EventScheduler;
import ddg.kernel.Time;
import ddg.model.data.ReplicationGroup;

/**
 * A single client.
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class DDGClient extends EventHandler {

	private final String id;
	private final MetadataServer metadataServer;
	private final Machine machine;

	/**
	 * 
	 * @param scheduler
	 * @param machine
	 * @param herald
	 */
	public DDGClient(EventScheduler scheduler, Machine machine, MetadataServer herald) {

		super(scheduler);

		this.metadataServer = herald;
		this.machine = machine;
		this.id = "client" + machine.bindClient(this) + machine;
	}

	@Override
	public void handleEvent(Event anEvent) {

		String anEventName = anEvent.getName();

		if (anEventName.equals(ReadEvent.EVENT_NAME)) {
			ReadEvent readEvent = (ReadEvent) anEvent;
			
			String filePath = readEvent.getFilePath();
			ReplicationGroup group = metadataServer.openPath(this, filePath);
			
			Machine primaryDataServerMachine =
				group.getPrimary().getMachine();
			
			if(primaryDataServerMachine.isSleeping()) {
				Time now = getScheduler().now();
				WakeUp wakeUp = new WakeUp(primaryDataServerMachine, now, true);
				primaryDataServerMachine.handleEvent(wakeUp);
			}

		} else if (anEventName.equals(WriteEvent.EVENT_NAME)) {
			WriteEvent writeEvent = (WriteEvent) anEvent;
			
			String filePath = writeEvent.getFilePath();
			ReplicationGroup group = metadataServer.openPath(this, filePath);
			group.setChanged(true);
			
			Machine primaryDataServerMachine =
				group.getPrimary().getMachine();
			
			if(primaryDataServerMachine.isSleeping()) {
				Time now = getScheduler().now();
				WakeUp wakeUp = new WakeUp(primaryDataServerMachine, now, true);
				primaryDataServerMachine.handleEvent(wakeUp);
			}
			
		} else if(anEventName.equals(CloseEvent.EVENT_NAME)) {
			CloseEvent closeEvent = (CloseEvent) anEvent;
			
			String filePath = closeEvent.getFilePath();
			
			metadataServer.closePath(this, filePath);
			
		} else if(anEventName.equals(UnlinkEvent.EVENT_NAME)) {
			throw new UnsupportedOperationException("falta implementar");
			//TODO implement
		} else {
			throw new RuntimeException();
		} 

		EmulatorControl.getInstance().scheduleNext();
	}

	/**
	 * @return the machine
	 */
	public Machine getMachine() {
		return machine;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DDGClient other = (DDGClient) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}