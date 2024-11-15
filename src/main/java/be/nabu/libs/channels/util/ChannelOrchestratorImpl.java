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
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelOrchestrator;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelRecoverySelector;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.ChannelRewriter;
import be.nabu.libs.channels.api.SingleChannelResultHandler;
import be.nabu.libs.channels.api.TwoPhaseChannelProvider;
import be.nabu.libs.datastore.DatastoreUtils;
import be.nabu.libs.datastore.api.ContextualWritableDatastore;
import be.nabu.libs.datatransactions.DataTransactionUtils;
import be.nabu.libs.datatransactions.api.DataTransaction;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.DataTransactionProvider;
import be.nabu.libs.datatransactions.api.DataTransactionState;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.ProviderResolver;
import be.nabu.libs.datatransactions.api.Transactionality;

public class ChannelOrchestratorImpl implements ChannelOrchestrator {
	
	private boolean useSingleBatch = false, pushFailedTransactions = true;
	private ChannelRewriter rewriter;
	private ContextualWritableDatastore<String> datastore;
	private DataTransactionProvider transactionProvider;
	private String creatorId;
	
	public ChannelOrchestratorImpl(ContextualWritableDatastore<String> datastore, DataTransactionProvider transactionProvider) {
		this(datastore, transactionProvider, DataTransactionUtils.generateCreatorId());
	}
	public ChannelOrchestratorImpl(ContextualWritableDatastore<String> datastore, DataTransactionProvider transactionProvider, String creatorId) {
		this.datastore = datastore;
		this.transactionProvider = transactionProvider;
		this.creatorId = creatorId;
	}
	
	@Override
	public void transact(ChannelManager manager, String context, Direction direction, ChannelResultHandler resultHandler, URI...requests) throws ChannelException {
		context = context.toLowerCase().replace('/', '.');
		List<Channel<?>> channelsToRun = new ArrayList<Channel<?>>();
		for (Channel<?> channel : manager.getChannels()) {
			if (direction.equals(channel.getDirection()) || Direction.BOTH.equals(channel.getDirection())) {
				String channelContext = channel.getContext().toLowerCase().replace('/', '.');
				if (channelContext.equals(context) || channelContext.startsWith(context + ".")) {
					channelsToRun.add(channel);
				}
			}
		}
		if (!channelsToRun.isEmpty()) {
			channelsToRun = rewrite(channelsToRun);
			if (useSingleBatch) {
				transactAsOneBatch(manager, context, direction, resultHandler, channelsToRun, requests);
			}
			else {
				transactAsManyBatches(manager, context, direction, resultHandler, channelsToRun, requests);
			}
		}
	}
	
	private void transactAsOneBatch(ChannelManager manager, String context, Direction direction, ChannelResultHandler resultHandler, List<Channel<?>> channels, URI...requests) throws ChannelException {
		LimitedSizeChannelResultHandler limitedSizeChannelResultHandler = new LimitedSizeChannelResultHandler(resultHandler, getCommonFinishAmount(channels));
		DataTransactionBatch<ChannelProvider<?>> batch = new ChannelDataTransactionBatch(transactionProvider.newBatch(manager.getProviderResolver(), context, creatorId, null, manager.getResultHandlerResolver().getId(resultHandler), direction, getCommonTransactionality(channels)), limitedSizeChannelResultHandler, pushFailedTransactions);
		ChannelException exception = null;
		try {
			for (Channel<?> channel : channels) {
				ChannelException channelException = transactSingleChannel(manager, context, limitedSizeChannelResultHandler, channel, batch, requests);
				if (channelException != null) {
					if (exception == null) {
						exception = new ChannelException("Could not process channel");
					}
					exception.addSuppressedException(channelException);
					if (!channel.isContinueOnFailure()) {
						throw exception;
					}
				}
			}
		}
		// always finish the remaining transactions that may be dangling
		finally {
			limitedSizeChannelResultHandler.flush();
		}
		if (exception != null) {
			throw exception;
		}
	}
	
	private void transactAsManyBatches(ChannelManager manager, String context, Direction direction, ChannelResultHandler resultHandler, List<Channel<?>> channels, URI...requests) throws ChannelException {
		ChannelException exception = null;
		for (Channel<?> channel : channels) {
			LimitedSizeChannelResultHandler limitedSizeChannelResultHandler = new LimitedSizeChannelResultHandler(resultHandler, channel.getFinishAmount());
			DataTransactionBatch<ChannelProvider<?>> batch = new ChannelDataTransactionBatch(transactionProvider.newBatch(manager.getProviderResolver(), channel.getContext(), creatorId, null, manager.getResultHandlerResolver().getId(resultHandler), direction, channel.getTransactionality()), limitedSizeChannelResultHandler, pushFailedTransactions);
			try {
				ChannelException channelException = transactSingleChannel(manager, context, limitedSizeChannelResultHandler, channel, batch, requests);
				if (channelException != null) {
					if (exception == null) {
						exception = channelException;
					}
					else {
						exception.addSuppressedException(channelException);
					}
					if (!channel.isContinueOnFailure()) {
						throw exception;
					}
				}
			}
			finally {
				limitedSizeChannelResultHandler.flush();
			}
		}
		if (exception != null) {
			throw exception;
		}
	}
	
	public ChannelException transact(ChannelManager manager, String providerId, Object properties, String context, ChannelResultHandler resultHandler, Transactionality transactionality, Integer finishAmount, 
			int retryAmount, long retryInterval, URI...requests) {
		if (finishAmount != null && resultHandler != null) {
			resultHandler = new LimitedSizeChannelResultHandler(resultHandler, finishAmount);
		}
		DataTransactionBatch<ChannelProvider<?>> batch = new ChannelDataTransactionBatch(transactionProvider.newBatch(
				manager.getProviderResolver(), 
				context, 
				creatorId, 
				null, 
				resultHandler == null ? null : manager.getResultHandlerResolver().getId(resultHandler), 
				requests == null || requests.length == 0 ? Direction.IN : Direction.OUT, 
				transactionality == null ? Transactionality.THREE_PHASE : transactionality
			), 
			resultHandler == null ? new SimpleChannelResultHandler(new SingleChannelResultHandler() {
				@Override
				public void handle(DataTransaction<?> transaction) throws ChannelException {
					// do nothing
				}
			}) : resultHandler, 
			pushFailedTransactions
		);
		ChannelException exception;
		try {
			exception = transactSingleChannel(manager, context, resultHandler, providerId, retryAmount, retryInterval, properties, batch, requests);
		}
		finally {
			if (resultHandler instanceof LimitedSizeChannelResultHandler) {
				((LimitedSizeChannelResultHandler) resultHandler).flush();
			}
		}
		return exception;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private ChannelException transactSingleChannel(ChannelManager manager, String context, ChannelResultHandler resultHandler, Channel channel, DataTransactionBatch batch, URI...requests) {
		return this.transactSingleChannel(manager, channel.getContext(), resultHandler, channel.getProviderId(), channel.getRetryAmount(), channel.getRetryInterval(), channel.getProperties(), batch, requests);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ChannelException transactSingleChannel(ChannelManager manager, String context, ChannelResultHandler resultHandler, String providerId, int retryAmount, long retryInterval, Object properties, DataTransactionBatch batch, URI...requests) {
		if (requests.length == 0) {
			requests = new URI[] { null };
		}
		ChannelException exception = null;
		// run 1 more time than the retry amount, the first is a try, not a retry
		for (int retried = 0; retried < retryAmount + 1; retried++) {
			// if this is a retry, do a sleep first
			if (retried > 0) {
				try {
					Thread.sleep(retryInterval);
				}
				catch (InterruptedException e) {
					// stop
					break;
				}
			}
			try {
				ChannelProvider provider = manager.getProviderResolver().getProvider(providerId);
				provider.transact(properties, DatastoreUtils.scope(datastore, context), batch, requests);
				// stop the retry loop
				break;
			}
			catch (ChannelException e) {
				if (exception == null) {
					exception = e;
				}
				else {
					exception.addSuppressedException(e);
				}
			}
		}
		return exception;
	}
	
	/**
	 * The most stringent transactionality wins
	 */
	private Transactionality getCommonTransactionality(List<Channel<?>> channels) {
		Transactionality transactionality = Transactionality.ONE_PHASE;
		for (Channel<?> channel : channels) {
			if (channel.getTransactionality().ordinal() < transactionality.ordinal()) {
				transactionality = channel.getTransactionality();
			}
		}
		return transactionality;
	}

	/**
	 * The smallest finish amount wins
	 */
	private int getCommonFinishAmount(List<Channel<?>> channels) {
		int finishAmount = 0;
		for (Channel<?> channel : channels) {
			if (channel.getFinishAmount() > 0 && (finishAmount == 0 || channel.getFinishAmount() < finishAmount)) {
				finishAmount = channel.getFinishAmount();
			}
		}
		return finishAmount;
	}
	
	public boolean isUseSingleBatch() {
		return useSingleBatch;
	}

	public void setUseSingleBatch(boolean useSingleBatch) {
		this.useSingleBatch = useSingleBatch;
	}
	
	public boolean isPushFailedTransactions() {
		return pushFailedTransactions;
	}

	public void setPushFailedTransactions(boolean pushFailedTransactions) {
		this.pushFailedTransactions = pushFailedTransactions;
	}

	private List<Channel<?>> rewrite(List<Channel<?>> channels) {
		if (getRewriter() == null) {
			return channels;
		}
		List<Channel<?>> rewritten = new ArrayList<Channel<?>>();
		for (Channel<?> channel : channels) {
			rewritten.addAll(getRewriter().rewrite(channel));
		}
		return rewritten;
	}
	
	ChannelRewriter getRewriter() {
		return rewriter;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void recover(ProviderResolver<ChannelProvider<?>> providerResolver, ProviderResolver<ChannelResultHandler> resultHandlerResolver, Date from, ChannelRecoverySelector selector) throws IOException, ChannelException {
		ChannelException exception = null;
		for (DataTransaction<?> transaction : transactionProvider.getPendingTransactions(creatorId, from)) {
			if (selector.recover(transaction)) {
				DataTransactionHandle handle = transactionProvider.getHandle(transaction.getId());
				try {
					ChannelProvider provider = providerResolver.getProvider(transaction.getProviderId());
					if (provider == null) {
						handle.fail("Could not resolve provider when attempting to recover");
					}
					else {
						// if it is in "started", it has not successfully reached commit yet, run that phase again
						if (DataTransactionState.STARTED.equals(transaction.getState())) {
							if (transaction.getRequest() == null) {
								provider.transact(transaction.getProperties(), datastore, new RetryTransactionBatch(transaction.getBatchId(), handle));
							}
							else {
								provider.transact(transaction.getProperties(), datastore, new RetryTransactionBatch(transaction.getBatchId(), handle), transaction.getRequest());
							}
						}
						// at this point it should be in COMMIT or DONE, either way you decided that it had to be run again
						// if it's a twophase, run the second phase again to be sure
						if (provider instanceof TwoPhaseChannelProvider) {
							((TwoPhaseChannelProvider) provider).finish(transaction.getProperties());
						}
						ChannelResultHandler resultHandler = resultHandlerResolver.getProvider(transaction.getHandlerId());
						if (resultHandler == null) {
							throw new IllegalArgumentException("Could not find result handler: " + transaction.getHandlerId());
						}
						// then run the handler
						resultHandler.handle(handle);
					}
				}
				catch (ChannelException e) {
					StringWriter writer = new StringWriter();
					PrintWriter printer = new PrintWriter(writer);
					e.printStackTrace(printer);
					printer.flush();
					handle.fail(writer.toString());
					if (exception == null) {
						exception = e;
					}
					else {
						exception.addSuppressed(e);
					}
				}
			}
			// if the transaction is not in the state DONE but you still decide not to recover it, fail it
			else if (!DataTransactionState.DONE.equals(transaction.getState())) {
				transactionProvider.getHandle(transaction.getId()).fail("Explicitly not recovered");
			}
		}
		if (exception != null) {
			throw exception;
		}
	}
}
