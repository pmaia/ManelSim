package ddg.model;

import ddg.emulator.EmulatorControl;
import ddg.emulator.events.filesystem.OpenEvent;
import ddg.emulator.events.filesystem.ReadEvent;
import ddg.emulator.events.filesystem.WriteEvent;
import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.EventScheduler;
import ddg.model.data.ReplicationGroup;

/**
 * A single client.
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class DDGClient extends EventHandler {

	private final String id;
	private final MetadataServer herald;
	private final Machine machine;

	/**
	 * Default constructor using fields.
	 * 
	 * @param schedulerwasLocal
	 * @param clientIndex
	 * @param machine
	 * @param herald
	 */
	public DDGClient(EventScheduler scheduler, int clientIndex,
			Machine machine, MetadataServer herald) {

		super(scheduler);

		this.herald = herald;
		this.machine = machine;
		machine.bindClient(this);
		this.id = "client" + clientIndex + machine;
	}

	@Override
	public void handleEvent(Event anEvent) {

		String anEventName = anEvent.getName();

		if (anEventName.equals(OpenEvent.EVENT_NAME)) {

			OpenEvent ev = (OpenEvent) anEvent;

			ReplicationGroup replicationGroup = herald.openPath(
					ev.getFileName(), ev.getFileDescriptor(), this);

			if (replicationGroup == null) {
				throw new RuntimeException(
						"The metadata server returned a null group for file: "
								+ ev.getFileName());
			}

		} else if (anEventName.equals(ReadEvent.EVENT_NAME)) {

			ReadEvent event = (ReadEvent) anEvent;
			int fileDescriptor = event.getFileDescriptor();
			ReplicationGroup group = herald
					.lookupReplicationGroup(fileDescriptor);
			herald.read(this, group.getFileName(), event.getOffset(),
					event.getSize());

		} else if (anEventName.equals(WriteEvent.EVENT_NAME)) {

			WriteEvent event = (WriteEvent) anEvent;
			int fileDescriptor = event.getFileDescriptor();
			ReplicationGroup group = herald
					.lookupReplicationGroup(fileDescriptor);
			String fileName = group.getFileName();
			herald.write(this, fileName, event.getOffset(), event.getSize());

		} else {

			// throw new RuntimeException();
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