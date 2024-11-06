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
import java.net.URI;

import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.TwoPhaseChannelProvider;
import be.nabu.libs.datatransactions.api.DataTransaction;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.DataTransactionState;

public class ChannelDataTransactionHandle<T> implements DataTransactionHandle {

	private DataTransactionHandle handle;
	private ChannelProvider<T> provider;
	private T properties;
	private ChannelResultHandler resultHandler;
	private boolean pushFailedTransactions;

	public ChannelDataTransactionHandle(ChannelResultHandler resultHandler, ChannelProvider<T> provider, T properties, DataTransactionHandle handle, boolean pushFailedTransactions) {
		this.resultHandler = resultHandler;
		this.provider = provider;
		this.properties = properties;
		this.handle = handle;
		this.pushFailedTransactions = pushFailedTransactions;
	}
	
	@Override
	public DataTransaction<?> getTransaction() {
		return handle.getTransaction();
	}

	@Override
	public void commit(URI response) throws IOException {
		// first commit the actual response
		handle.commit(response);
		// then call the provider finish method if it's a two-phase
		if (provider instanceof TwoPhaseChannelProvider) {
			try {
				((TwoPhaseChannelProvider<T>) provider).finish(properties);
			}
			catch (ChannelException e) {
				handle.fail("Could not execute second phase of provider: " + e.getMessage());
				if (pushFailedTransactions && resultHandler != null) {
					resultHandler.handle(handle);
				}
			}
		}
		// then push it to the result handler
		resultHandler.handle(handle);
	}

	@Override
	public void done() throws IOException {
		handle.done();
	}

	@Override
	public void fail(String message) throws IOException {
		// You can configure in general whether or not you want to push failed transactions to your receiving code
		// You may want to use a custom error alerting framework, create an error process,...
		// However we only do this if the transaction is in the STARTED state because after the commit, it is assumed that you already have access to the transaction and it is your own custom code that is failing
		boolean pushThisTransaction = this.pushFailedTransactions && DataTransactionState.STARTED.equals(handle.getTransaction().getState());
		handle.fail(message);
		if (pushThisTransaction) {
			resultHandler.handle(handle);
		}
	}
}
