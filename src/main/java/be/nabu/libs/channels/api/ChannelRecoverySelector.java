package be.nabu.libs.channels.api;

import be.nabu.libs.datatransactions.api.DataTransaction;

public interface ChannelRecoverySelector {
	public boolean recover(DataTransaction<?> transaction);
}
