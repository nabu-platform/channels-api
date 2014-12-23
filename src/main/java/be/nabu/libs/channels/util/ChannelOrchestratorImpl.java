package be.nabu.libs.channels.util;

import java.io.IOException;
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
	
	private boolean useSingleBatch = false;
	private ChannelRewriter rewriter;
	private ContextualWritableDatastore<String> datastore;
	private DataTransactionProvider transactionProvider;
	private String creatorId;
	
	public ChannelOrchestratorImpl(ContextualWritableDatastore<String> datastore, DataTransactionProvider transactionProvider) {
		this.datastore = datastore;
		this.transactionProvider = transactionProvider;
		this.creatorId = DataTransactionUtils.generateCreatorId();
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
		DataTransactionBatch<ChannelProvider<?>> batch = new ChannelDataTransactionBatch(transactionProvider.newBatch(new ChannelProviderResolver(manager), context, DataTransactionUtils.generateCreatorId(), null, direction, getCommonTransactionality(channels)), limitedSizeChannelResultHandler);
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
			DataTransactionBatch<ChannelProvider<?>> batch = new ChannelDataTransactionBatch(transactionProvider.newBatch(new ChannelProviderResolver(manager), channel.getContext(), creatorId, null, direction, channel.getTransactionality()), limitedSizeChannelResultHandler);
			try {
				ChannelException channelException = transactSingleChannel(manager, context, limitedSizeChannelResultHandler, channel, batch, requests);
				if (channelException != null) {
					if (exception == null) {
						exception = new ChannelException();
					}
					exception.addSuppressedException(channelException);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ChannelException transactSingleChannel(ChannelManager manager, String context, ChannelResultHandler resultHandler, Channel channel, DataTransactionBatch batch, URI...requests) {
		if (requests.length == 0) {
			requests = new URI[] { null };
		}
		ChannelException exception = null;
		// run 1 more time than the retry amount, the first is a try, not a retry
		for (int retried = 0; retried < channel.getRetryAmount() + 1; retried++) {
			// if this is a retry, do a sleep first
			if (retried > 0) {
				try {
					Thread.sleep(channel.getRetryInterval());
				}
				catch (InterruptedException e) {
					// stop
					break;
				}
			}
			try {
				ChannelProvider provider = manager.getProviders().get(channel.getProviderId());
				provider.transact(channel.getProperties(), DatastoreUtils.scope(datastore, channel.getContext()), batch, requests);
				// stop the retry loop
				break;
			}
			catch (ChannelException e) {
				if (exception == null) {
					exception = new ChannelException();
				}
				exception.addSuppressedException(e);
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
	
	boolean isUseSingleBatch() {
		return useSingleBatch;
	}

	void setUseSingleBatch(boolean useSingleBatch) {
		this.useSingleBatch = useSingleBatch;
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
	public void recover(ProviderResolver<ChannelProvider<?>> providerResolver, Date from, ChannelRecoverySelector selector, ChannelResultHandler resultHandler) throws IOException {
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
						// then run the handler
						resultHandler.handle(handle);
					}
				}
				catch (Exception e) {
					handle.fail("Error occurred while recovering: " + e.getMessage());
				}
			}
			// if the transaction is not in the state DONE but you still decide not to recover it, fail it
			else if (!DataTransactionState.DONE.equals(transaction.getState())) {
				transactionProvider.getHandle(transaction.getId()).fail("Explicitly not recovered");
			}
		}
	}
}
