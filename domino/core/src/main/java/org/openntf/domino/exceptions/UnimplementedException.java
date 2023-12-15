/**
 * Copyright © 2013-2023 The OpenNTF Domino API Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.domino.exceptions;

public class UnimplementedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnimplementedException() {
		super();
	}

	/**
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
	 */
	public UnimplementedException(final String message) {
		super(message);
	}

	public UnimplementedException(final String message, final Throwable paramThrowable) {
		super(message, paramThrowable);
	}

}
