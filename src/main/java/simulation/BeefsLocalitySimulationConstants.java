package simulation;

/**
 * @author manel
 */
public class BeefsLocalitySimulationConstants {
	
	// Configuration keys
	public static final String FS_TRACE_FILE = "fs_trace_file";
	
	public static final String WAKE_ON_LAN = "wake_on_lan";
	public static final String TO_SLEEP_TIMEOUT = "to_sleep_timeout";
	public static final String TRANSITION_DURATION = "transition_duration";
	
	public static final String PLACEMENT_POLICE = "placement_police";
	public static final String REPLICATION_LEVEL = "replication_level";
	public static final String TIME_TO_COHERENCE = "time_to_coherence";
	public static final String TIME_TO_DELETE_REPLICAS  = "time_to_delete_replicas";
	
	public static final String NUM_MACHINES = "num_machines";
	
	public static final String USER_MIGRATION_ALGORITHM = "user_migration_algorithm";
	public static final String USER_MIGRATION_PROB = "user_migration_probability";
	public static final String USER_INACTIVITY_DELAY = "user_inactivity_delay";
	
	// Context keys
	public static final String MACHINES = "machines";
	public static final String DATA_SERVERS = "data_serves";
	public static final String METADATA_SERVER = "metadata_server";
	public static final String CLIENTS = "clients";
}