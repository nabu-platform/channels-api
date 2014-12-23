package be.nabu.libs.channels.api;

import be.nabu.libs.datatransactions.api.DataTransaction;

public interface SingleChannelResultHandler {
	public void handle(DataTransaction<?> transaction) throws ChannelException;
}
