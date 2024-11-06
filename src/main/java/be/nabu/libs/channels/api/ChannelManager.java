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
