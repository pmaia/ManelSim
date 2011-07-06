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
package ddg.model;

import ddg.model.data.DataServer;

/**
 * TODO make doc
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class HasNotSpaceOnDeviceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final long sizeAddition;

	/**
	 * @param sizeAddition
	 * @param dataserver
	 */
	public HasNotSpaceOnDeviceException(long sizeAddition, DataServer dataserver) {
		super("DataServer: " + dataserver + " available on disk: "
				+ dataserver.getAvailableDiskSize() + " size addition: "
				+ sizeAddition);
		this.sizeAddition = sizeAddition;
	}

	/**
	 * @return
	 */
	public long getSizeAddition() {
		return sizeAddition;
	}

}