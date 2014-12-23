package be.nabu.libs.channels.util;

import java.io.IOException;
import java.net.URI;

import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.TwoPhaseChannelProvider;
import be.nabu.libs.datatransactions.api.DataTransaction;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public class ChannelDataTransactionHandle<T> implements DataTransactionHandle {

	private DataTransactionHandle handle;
	private ChannelProvider<T> provider;
	private T properties;
	private ChannelResultHandler resultHandler;

	public ChannelDataTransactionHandle(ChannelResultHandler resultHandler, ChannelProvider<T> provider, T properties, DataTransactionHandle handle) {
		this.resultHandler = resultHandler;
		this.provider = provider;
		this.properties = properties;
		this.handle = handle;
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
				throw new IOException(e);
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
		handle.fail(message);
	}
}
