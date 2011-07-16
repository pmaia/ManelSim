package ddg.emulator;

import java.util.List;

import ddg.emulator.events.CancelPendingMachineStateTransitionsEvent;
import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JEEventScheduler;
import ddg.model.Machine;

/**
 * 
 * Arghhhhhh!
 * 
 * @author Don't ask
 *
 */
public class CancelPendingMachineStateTransitionsEventHandler extends
		JEEventHandler {
	
	private final List<Machine> machines;
	
	private static CancelPendingMachineStateTransitionsEventHandler instance;

	private CancelPendingMachineStateTransitionsEventHandler(JEEventScheduler scheduler, List<Machine> machines) {
		super(scheduler);
		this.machines = machines;
	}
	
	public static CancelPendingMachineStateTransitionsEventHandler build(JEEventScheduler scheduler, List<Machine> machines) {
		
		if(instance != null)
			throw new IllegalStateException();
		
		instance = new CancelPendingMachineStateTransitionsEventHandler(scheduler, machines);
		
		return instance;
	}
	
	public static CancelPendingMachineStateTransitionsEventHandler getInstance() {
		if(instance == null)
			throw new IllegalStateException();
		
		return instance;
	}

	@Override
	public void handleEvent(JEEvent jeevent) {
		if(jeevent instanceof CancelPendingMachineStateTransitionsEvent) {
			for(Machine machine : machines) {
				machine.cancelPendingMachineStateTransition();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

}
