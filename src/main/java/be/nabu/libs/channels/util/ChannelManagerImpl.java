package be.nabu.libs.channels.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.util.ChannelManagerConfiguration.ChannelProviderConfiguration;

public class ChannelManagerImpl implements ChannelManager {

	private ChannelManagerConfiguration configuration;
	private Map<String, ChannelProvider<?>> providers;

	public ChannelManagerImpl(ChannelManagerConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public Map<String, ChannelProvider<?>> getProviders() {
		if (providers == null) {
			providers = new HashMap<String, ChannelProvider<?>>();
			for (ChannelProviderConfiguration<?> providerConfiguration : getConfiguration().getProviders()) {
				// if a provider no longer exists, delete it from the configuration, otherwise it will keep throwing errors
				try {
					Class<?> providerClass = Thread.currentThread().getContextClassLoader().loadClass(providerConfiguration.getProviderClass());
					ChannelProvider<?> instance = providerConfiguration.getProperties() != null
						? (ChannelProvider<?>) providerClass.getConstructor(providerConfiguration.getProperties().getClass()).newInstance(providerConfiguration.getProperties())
						: (ChannelProvider<?>) providerClass.newInstance();
					providers.put(providerConfiguration.getName(), instance);
				}
				catch (InstantiationException e) {
					throw new RuntimeException(e);
				}
				catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
				catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
				catch (SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return providers;
	}

	@Override
	public List<? extends Channel<?>> getChannels() {
		return getConfiguration().getChannels();
	}

	public ChannelManagerConfiguration getConfiguration() {
		return configuration;
	}
}
