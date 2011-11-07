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
package ddg.model.placement;

import ddg.model.DDGClient;
import ddg.model.data.ReplicationGroup;

/**
 * Encapsulates the logic of {@link ReplicationGroup} creation, deciding where primary
 * and secondaries replicas of a file are placed.
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public interface DataPlacementAlgorithm {

	ReplicationGroup createFile(DDGClient client, String fileName, int replicationLevel);
	
}