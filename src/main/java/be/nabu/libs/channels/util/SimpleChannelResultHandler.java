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

package be.nabu.libs.channels.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.SingleChannelResultHandler;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public class SimpleChannelResultHandler implements ChannelResultHandler {

	private SingleChannelResultHandler handler;

	public SimpleChannelResultHandler(SingleChannelResultHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void handle(DataTransactionHandle...transactionHandles) {
		for (DataTransactionHandle handle : transactionHandles) {
			try {
				try {
					handler.handle(handle.getTransaction());
					handle.done();
				}
				catch (Exception e) {
					StringWriter writer = new StringWriter();
					PrintWriter printer = new PrintWriter(writer);
					e.printStackTrace(printer);
					printer.flush();
					handle.fail(writer.toString());
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public SingleChannelResultHandler getHandler() {
		return handler;
	}
	
}
