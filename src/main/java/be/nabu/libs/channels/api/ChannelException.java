/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.channels.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelException extends Exception {

	private static final long serialVersionUID = 1072452438832380010L;
	private List<Exception> suppressedExceptions = new ArrayList<Exception>();

	public ChannelException() {
		super();
	}

	public ChannelException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelException(String message) {
		super(message);
	}

	public ChannelException(Throwable cause) {
		super(cause);
	}
	
	public ChannelException(Exception...suppressed) {
		super("Combined channel exception");
		this.suppressedExceptions.addAll(Arrays.asList(suppressed));
	}

	public List<Exception> getSuppressedExceptions() {
		return suppressedExceptions;
	}

	public void addSuppressedException(Exception e) {
		this.suppressedExceptions.add(e);
	}
}
