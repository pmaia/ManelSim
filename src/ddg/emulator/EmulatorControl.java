/**
 * 
 */
package ddg.emulator;

import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JEEventScheduler;
import ddg.model.MetadataServer;

/**
 * Create system entities, schedule first events and start emulation.
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class EmulatorControl {

	private final JEEventScheduler theUniqueEventScheduler;
	private final ClientEventInjector eventInjector;

	// ugly but works
	private static EmulatorControl singleInstace;

	private final MetadataServer metadataServer;
	private final BootStrapperEventHandler bootStrapperEventHandler;

	public final boolean datamigration;
	public final long replicationDelayMillis;

	/**
	 * @param eventScheduler
	 * @param eventInjector
	 * @param metadataServer
	 * @param data_migration
	 * @param replicationDelayMillis
	 * @return
	 */
	public static EmulatorControl build(JEEventScheduler eventScheduler,
			ClientEventInjector eventInjector, MetadataServer metadataServer,
			boolean data_migration, long replicationDelayMillis) {

		if (singleInstace != null)
			throw new IllegalStateException();

		singleInstace = new EmulatorControl(eventScheduler, eventInjector,
				metadataServer, data_migration, replicationDelayMillis);
		return singleInstace;
	}

	/**
	 * @return
	 */
	public static EmulatorControl getInstance() {

		if (singleInstace == null)
			throw new IllegalStateException();

		return singleInstace;
	}

	/**
	 * @return
	 */
	public MetadataServer getMetadataServer() {
		return metadataServer;
	}

	/**
	 * @param eventScheduler
	 * @param injector
	 * @param metadataServer
	 * @param datamigration
	 * @param replicationDelayMillis
	 */
	private EmulatorControl(JEEventScheduler eventScheduler,
			ClientEventInjector injector, MetadataServer metadataServer,
			boolean datamigration, long replicationDelayMillis) {

		this.theUniqueEventScheduler = eventScheduler;
		this.eventInjector = injector;
		this.metadataServer = metadataServer;
		this.datamigration = datamigration;
		this.replicationDelayMillis = replicationDelayMillis;
		bootStrapperEventHandler = new BootStrapperEventHandler(
				theUniqueEventScheduler);
	}

	public JEEventScheduler getTheUniqueEventScheduler() {
		return theUniqueEventScheduler;
	}

	/**
	 * 
	 */
	public void scheduleNext() {
		scheduleNext(this.eventInjector.getNextEvent());
	}

	/**
	 * @param nextEvent
	 */
	public void scheduleNext(JEEvent nextEvent) {

		if (nextEvent != null) {
			bootStrapperEventHandler.handleEvent(nextEvent);
		}
	}

	private class BootStrapperEventHandler extends JEEventHandler {

		/**
		 * @param scheduler
		 */
		public BootStrapperEventHandler(JEEventScheduler scheduler) {
			super(scheduler);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleEvent(JEEvent arg) {
			send(arg);
		}

	}
}