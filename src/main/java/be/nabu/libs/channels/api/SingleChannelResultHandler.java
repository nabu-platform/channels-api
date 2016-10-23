package be.nabu.libs.channels.api;

import javax.jws.WebParam;

import be.nabu.libs.datatransactions.api.DataTransaction;

public interface SingleChannelResultHandler {
	public void handle(@WebParam(name = "transaction") DataTransaction<?> transaction) throws ChannelException;
}
