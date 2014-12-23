package be.nabu.libs.channels.util;

import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.datatransactions.api.ProviderResolver;

public class ChannelProviderResolver implements ProviderResolver<ChannelProvider<?>> {

	private ChannelManager manager;

	public ChannelProviderResolver(ChannelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public String getId(ChannelProvider<?> provider) {
		for (String id : manager.getProviders().keySet()) {
			if (manager.getProviders().get(id).equals(provider)) {
				return id;
			}
		}
		throw new IllegalArgumentException("Can not find the provider " + provider);
	}

	@Override
	public ChannelProvider<?> getProvider(String id) {
		return manager.getProviders().get(id);
	}

}
