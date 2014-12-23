package be.nabu.libs.channels.util;

import java.io.IOException;
import java.net.URI;

import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public class ChannelDataTransactionBatch implements DataTransactionBatch<ChannelProvider<?>> {

	private DataTransactionBatch<ChannelProvider<?>> batch;
	private ChannelResultHandler resultHandler;
	private boolean pushFailedTransactions;

	public ChannelDataTransactionBatch(DataTransactionBatch<ChannelProvider<?>> batch, ChannelResultHandler resultHandler, boolean pushFailedTransactions) {
		this.batch = batch;
		this.resultHandler = resultHandler;
		this.pushFailedTransactions = pushFailedTransactions;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <P> DataTransactionHandle start(ChannelProvider<?> provider, P properties, URI request) throws IOException {
		return new ChannelDataTransactionHandle(resultHandler, provider, properties, batch.start(provider, properties, request), pushFailedTransactions);
	}

	@Override
	public String getId() {
		return batch.getId();
	}

}
