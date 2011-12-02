package ddg.model.data;

import ddg.model.Machine;

public class DataServer {

	private final String id;
	private final Machine machine;

	public DataServer(Machine machine) {
		this.machine = machine;
		this.id = "ds" + machine.deploy(this) + machine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ddg.DataServer#getId()
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the machine
	 */
	public Machine getMachine() {
		return machine;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id;
	}

}