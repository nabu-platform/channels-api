/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.channels.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.channels.ChannelUtils;
import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.util.ChannelManagerConfiguration.ChannelProviderConfiguration;
import be.nabu.libs.datatransactions.api.ProviderResolver;

public class ChannelManagerImpl implements ChannelManager {

	private ChannelManagerConfiguration configuration;
	private Map<String, ChannelProvider<?>> providers;

	public ChannelManagerImpl(ChannelManagerConfiguration configuration) {
		this.configuration = configuration;
	}
	
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
	
	@Override
	public ProviderResolver<ChannelProvider<?>> getProviderResolver() {
		return new ProviderResolver<ChannelProvider<?>>() {
			@Override
			public String getId(ChannelProvider<?> provider) {
				for (String id : getProviders().keySet()) {
					if (getProviders().get(id).equals(provider)) {
						return id;
					}
				}
				throw new IllegalArgumentException("Can not find the provider " + provider);
			}

			@Override
			public ChannelProvider<?> getProvider(String id) {
				return getProviders().get(id);
			}
		};
	}

	@Override
	public ProviderResolver<ChannelResultHandler> getResultHandlerResolver() {
		return ChannelUtils.newChannelResultHandlerResolver();
	}
}
