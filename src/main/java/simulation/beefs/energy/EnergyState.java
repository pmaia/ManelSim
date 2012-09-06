package simulation.beefs.energy;

public enum EnergyState {
	READ_ACTIVE,
	READ_IDLE,
	WRITE_ACTIVE,
	WRITE_IDLE,
	READ_WRITE_ACTIVE,
	READ_WRITE_IDLE,
	ACTIVE,
	IDLE,
	SLEEPING,
	TRANSITIONING;

	public EnergyState addWrite() {
		EnergyState statePlusRead;
		switch(this) {
		case READ_ACTIVE: statePlusRead = EnergyState.READ_WRITE_ACTIVE; break;
		case READ_IDLE: statePlusRead = EnergyState.READ_WRITE_IDLE; break;
		case ACTIVE: statePlusRead = EnergyState.WRITE_ACTIVE; break;
		case IDLE: statePlusRead = EnergyState.WRITE_IDLE; break;
		default: 
			throw new IllegalArgumentException("Could not add " + this + " and WRITE");
		}
		return statePlusRead;
	}

	public EnergyState addRead() {
		EnergyState statePlusRead;
		switch(this) {
		case WRITE_ACTIVE: statePlusRead = EnergyState.READ_WRITE_ACTIVE; break;
		case WRITE_IDLE: statePlusRead = EnergyState.READ_WRITE_IDLE; break;
		case ACTIVE: statePlusRead = EnergyState.READ_ACTIVE; break;
		case IDLE: statePlusRead = EnergyState.READ_IDLE; break;
		default: 
			throw new IllegalArgumentException("Could not add " + this + " and READ");
		}
		return statePlusRead;
	}

}