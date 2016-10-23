package be.nabu.libs.channels.api;

import javax.jws.WebParam;

import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public interface ChannelResultHandler {
	public void handle(@WebParam(name = "transactionHandles") DataTransactionHandle...transactionHandle);
}
