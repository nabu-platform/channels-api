package be.nabu.libs.channels.api;

import java.net.URI;

import be.nabu.libs.datastore.api.WritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.Direction;

public interface ChannelProvider<T> {
	public void transact(T properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionBatch, URI...requests) throws ChannelException;
	public Direction getDirection();
	public Class<T> getPropertyClass();
}
