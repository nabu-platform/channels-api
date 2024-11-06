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
import be.nabu.libs.channels.util.SimpleChannelResultHandler;
import be.nabu.libs.datastore.api.ContextualWritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionProvider;
import be.nabu.libs.datatransactions.api.ProviderResolver;

public class ChannelUtils {
	
	public static DataTransactionBatch<ChannelProvider<?>> manage(DataTransactionBatch<ChannelProvider<?>> batch, ChannelResultHandler resultHandler) {
		return new ChannelDataTransactionBatch(batch, resultHandler, true);
	}
	
	public static ChannelOrchestrator newOrchestrator(ContextualWritableDatastore<String> datastore, DataTransactionProvider transactionProvider) {
		return new ChannelOrchestratorImpl(datastore, transactionProvider);
	}

	public static ChannelManager newChannelManager(ChannelManagerConfiguration configuration) {
		return new ChannelManagerImpl(configuration);
	}
	
	public static ChannelResultHandler newChannelResultHandler(SingleChannelResultHandler handler) {
		return new SimpleChannelResultHandler(handler);
	}
	
	public static ProviderResolver<ChannelResultHandler> newChannelResultHandlerResolver() {
		return new ProviderResolver<ChannelResultHandler>() {
			@Override
			public String getId(ChannelResultHandler provider) {
				if (provider instanceof SimpleChannelResultHandler) {
					return ((SimpleChannelResultHandler) provider).getHandler().getClass().getName();
				}
				else {
					return provider.getClass().getName();
				}
			}
			@Override
			public ChannelResultHandler getProvider(String id) {
				try {
					Object newInstance = Thread.currentThread().getContextClassLoader().loadClass(id).newInstance();
					return newInstance instanceof SingleChannelResultHandler ? newChannelResultHandler((SingleChannelResultHandler) newInstance) : (ChannelResultHandler) newInstance;
				}
				catch (Exception e) {
					throw new RuntimeException();
				}
			}
		};
	}
}
