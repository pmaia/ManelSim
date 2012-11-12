package simulation.beefs.userMigration;

import simulation.beefs.model.FileSystemClient;
import core.Time;

/**
 * @author manel
 */
public interface UserMigrationAlgorithm {
	
	FileSystemClient baseClient(Time now);
}