package be.nabu.libs.channels.util;

import java.io.IOException;
import java.net.URI;

import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public class RetryTransactionBatch implements DataTransactionBatch<ChannelProvider<?>> {

	private DataTransactionHandle existingHandle;
	private boolean isHandled = false;
	private String batchId;

	public RetryTransactionBatch(String batchId, DataTransactionHandle existingHandle) {
		this.batchId = batchId;
		this.existingHandle = existingHandle;
	}
	
	@Override
	public <P> DataTransactionHandle start(ChannelProvider<?> provider, P properties, URI request) throws IOException {
		if (isHandled) {
			throw new IOException("Can only retry once");
		}
		isHandled = true;
		return existingHandle;
	}

	@Override
	public String getId() {
		return batchId;
	}

}
