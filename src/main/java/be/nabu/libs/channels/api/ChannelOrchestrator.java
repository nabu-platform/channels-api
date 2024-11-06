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
	public void recover(ProviderResolver<ChannelProvider<?>> provider, ProviderResolver<ChannelResultHandler> resultHandlerResolver, Date from, ChannelRecoverySelector selector) throws IOException, ChannelException;
}
