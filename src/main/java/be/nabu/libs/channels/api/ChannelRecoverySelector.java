package be.nabu.libs.channels.api;

import javax.jws.WebParam;

import be.nabu.libs.datatransactions.api.DataTransaction;

public interface ChannelRecoverySelector {
	public boolean recover(@WebParam(name = "transaction") DataTransaction<?> transaction);
}
