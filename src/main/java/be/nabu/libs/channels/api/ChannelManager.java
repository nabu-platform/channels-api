package be.nabu.libs.channels.api;

import java.util.List;

import be.nabu.libs.datatransactions.api.ProviderResolver;

/**
 * The channel manager is responsible for maintaining all the configuration related to the channels and the runnable environment
 */
public interface ChannelManager {
	/**
	 * The channels known to the channel manager
	 */
	public List<? extends Channel<?>> getChannels();
	/**
	 * A way to resolve channel providers
	 */
	public ProviderResolver<ChannelProvider<?>> getProviderResolver();
	/**
	 * A way to resolve channel result handlers
	 */
	public ProviderResolver<ChannelResultHandler> getResultHandlerResolver();
}
