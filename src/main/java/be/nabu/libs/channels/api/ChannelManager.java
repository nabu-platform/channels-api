package be.nabu.libs.channels.api;

import java.util.List;
import java.util.Map;

/**
 * The channel manager is responsible for maintaining all the configuration related to the channels and the runnable environment
 */
public interface ChannelManager {
	/**
	 * This should return the registered providers. The key is the id that is used to reference the provider
	 * This will be used to resolve the provider from the channel "providerId"
	 * It will also be used to resolve the provider for recovery or retrying
	 */
	public Map<String, ChannelProvider<?>> getProviders();
	/**
	 * The channels known to the channel manager
	 */
	public List<? extends Channel<?>> getChannels();

}
