/**
 * Copyright (C) 2009 Universidade Federal de Campina Grande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package simulation.beefs.event.filesystem;

import java.util.List;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.ReplicatedFile;
import core.Event;
import core.EventScheduler;
import core.Time;
import core.Time.Unit;

/**
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class RepairReplicatedFile extends Event {

	//FIXME: Do we want to turn this as a simulation parameter ?
	public static final Time REPAIR_DELAY =
				new Time(15 * 60, Unit.SECONDS);
		
	private final List<DataServer> candidates;
	private final ReplicatedFile replicatedFile;

	public RepairReplicatedFile(List<DataServer> candidates, 
			ReplicatedFile replicatedFile,
			Time scheduledTime) {
		
		super(scheduledTime);
		this.candidates = candidates;
		this.replicatedFile = replicatedFile;
	}
	
	@Override
	public String toString() {
		return "repair\t" + getScheduledTime() + "\t" + "filePath\t" + replicatedFile.getFullPath();
	}
	
	@Override
	public void process() {
		
		boolean repairCompleted = this.replicatedFile.repair(candidates);
		
		if (!repairCompleted) {
			EventScheduler.schedule(
					new RepairReplicatedFile(candidates, replicatedFile,
							EventScheduler.now().plus(REPAIR_DELAY)));
		}
	}

}