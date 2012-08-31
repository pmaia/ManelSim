package core;

import java.util.Properties;

/**
 * 
 * @author Patrick Maia
 *
 */
public interface Initializer {
	Context initialize(Properties config);
}
