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
package simulation.beefs.model;

/**
 * TODO make doc
 *
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class InsufficientSpaceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public final long request;
	public final long remainder;

	public InsufficientSpaceException(String message, long request, long remainder) {
		super(message);
		this.request = request;
		this.remainder = remainder;
	}

	public InsufficientSpaceException(long request, long remainder) {
		this("", request, remainder);
	}

}