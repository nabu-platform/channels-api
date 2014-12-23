package be.nabu.libs.channels.api;

import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public interface ChannelResultHandler {
	public void handle(DataTransactionHandle...transactionHandle);
}
