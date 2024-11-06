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

package be.nabu.libs.channels;

import be.nabu.libs.channels.api.ChannelOrchestrator;

/**
 * This factory allows for centralized configuration but does not currently allow service-like detection
 */
public class ChannelOrchestratorFactory {

	private static ChannelOrchestratorFactory instance;
	
	public static ChannelOrchestratorFactory getInstance() {
		if (instance == null) {
			synchronized(ChannelOrchestratorFactory.class) {
				if (instance == null) {
					instance = new ChannelOrchestratorFactory();
				}
			}
		}
		return instance;
	}
	
	private ChannelOrchestrator channelOrchestrator;
	
	public void setChannelOrchestrator(ChannelOrchestrator channelOrchestrator) {
		this.channelOrchestrator = channelOrchestrator;
	}
	
	public void unsetChannelOrchestrator(ChannelOrchestrator channelOrchestrator) {
		this.channelOrchestrator = null;
	}

	public ChannelOrchestrator getChannelOrchestrator() {
		return channelOrchestrator;
	}
}
