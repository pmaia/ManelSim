package model.data;

import model.Machine;

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

	public Machine getMachine() {
		return machine;
	}

	@Override
	public String toString() {
		return id;
	}

}