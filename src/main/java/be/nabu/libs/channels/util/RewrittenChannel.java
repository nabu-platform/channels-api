package be.nabu.libs.channels.util;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;

public class RewrittenChannel<T> implements Channel<T> {

	private Channel<T> original;
	private T rewrittenProperties;

	public RewrittenChannel(Channel<T> original, T rewrittenProperties) {
		this.original = original;
		this.rewrittenProperties = rewrittenProperties;
	}
	
	@Override
	public String getContext() {
		return original.getContext();
	}

	@Override
	public Direction getDirection() {
		return original.getDirection();
	}

	@Override
	public String getProviderId() {
		return original.getProviderId();
	}

	@Override
	public T getProperties() {
		return rewrittenProperties;
	}

	@Override
	public Transactionality getTransactionality() {
		return original.getTransactionality();
	}

	@Override
	public int getPriority() {
		return original.getPriority();
	}

	@Override
	public int getFinishAmount() {
		return original.getFinishAmount();
	}

	@Override
	public boolean isContinueOnFailure() {
		return original.isContinueOnFailure();
	}

	@Override
	public int getRetryAmount() {
		return original.getRetryAmount();
	}

	@Override
	public long getRetryInterval() {
		return original.getRetryInterval();
	}
}
