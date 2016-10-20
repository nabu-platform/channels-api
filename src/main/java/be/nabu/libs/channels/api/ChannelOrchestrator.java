package be.nabu.libs.channels.api;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.ProviderResolver;

/**
 * The channel orchestrator is responsible for actually running channels.
 * It can determine for example whether you want to run just one channel or all matching channels
 * It can then choose how to scope the batch, handle the retry (if possible),...
 * It can choose to rewrite channel properties (e.g. replace variables etc) or entire channels...
 */
public interface ChannelOrchestrator {
	public void transact(ChannelManager manager, String context, Direction direction, ChannelResultHandler resultHandler, URI...requests) throws ChannelException;
	public void recover(ProviderResolver<ChannelProvider<?>> provider, ProviderResolver<ChannelResultHandler> resultHandlerResolver, Date from, ChannelRecoverySelector selector) throws IOException;
}
