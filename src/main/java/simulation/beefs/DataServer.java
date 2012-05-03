package simulation.beefs;


public class DataServer {

	private final String id;
	private final Machine machine;

	public DataServer(Machine machine) {
		this.machine = machine;
		this.id = "ds" + machine.deploy(this) + machine;
	}

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