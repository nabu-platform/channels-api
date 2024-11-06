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
