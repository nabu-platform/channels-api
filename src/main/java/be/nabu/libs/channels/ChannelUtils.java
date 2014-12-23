package be.nabu.libs.channels;

import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelOrchestrator;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.SingleChannelResultHandler;
import be.nabu.libs.channels.util.ChannelDataTransactionBatch;
import be.nabu.libs.channels.util.ChannelManagerConfiguration;
import be.nabu.libs.channels.util.ChannelManagerImpl;
import be.nabu.libs.channels.util.ChannelOrchestratorImpl;
import be.nabu.libs.channels.util.ChannelProviderResolver;
import be.nabu.libs.channels.util.SimpleChannelResultHandler;
import be.nabu.libs.datastore.api.ContextualWritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionProvider;
import be.nabu.libs.datatransactions.api.ProviderResolver;

public class ChannelUtils {
	
	public static DataTransactionBatch<ChannelProvider<?>> manage(DataTransactionBatch<ChannelProvider<?>> batch, ChannelResultHandler resultHandler) {
		return new ChannelDataTransactionBatch(batch, resultHandler);
	}
	
	public static ChannelOrchestrator newOrchestrator(ContextualWritableDatastore<String> datastore, DataTransactionProvider transactionProvider) {
		return new ChannelOrchestratorImpl(datastore, transactionProvider);
	}
	
	public static ProviderResolver<ChannelProvider<?>> newProviderResolver(ChannelManager manager) {
		return new ChannelProviderResolver(manager);
	}

	public static ChannelManager newChannelManager(ChannelManagerConfiguration configuration) {
		return new ChannelManagerImpl(configuration);
	}
	
	public static ChannelResultHandler newChannelResultHandler(SingleChannelResultHandler handler) {
		return new SimpleChannelResultHandler(handler);
	}
}
