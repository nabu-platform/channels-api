package be.nabu.libs.channels.api;

import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;

public interface Channel<T> {
	/**
	 * The context of the channel
	 */
	public String getContext();
	/**
	 * The provider can be multi directional, we need to know which direction it is being used in
	 */
	public Direction getDirection();
	/**
	 * The provider
	 */
	public String getProviderId();
	/**
	 * The properties to run the provider
	 */
	public T getProperties();
	/**
	 * The transactionality to use for this channel
	 */
	public Transactionality getTransactionality();
	/**
	 * The priority of the resulting transactions
	 */
	public int getPriority();
	/**
	 * This determines after how many transactions a finish() is called on the resulting provider
	 * If you set 0, it will wait until everything is done
	 */
	public int getFinishAmount();
	/**
	 * When executing multiple channels and one fails, do we want to continue? 
	 */
	public boolean isContinueOnFailure();
	/**
	 * If it fails, how many times do we want to retry?
	 */
	public int getRetryAmount();
	/**
	 * How long do we want to wait to retry
	 */
	public long getRetryInterval();
}
