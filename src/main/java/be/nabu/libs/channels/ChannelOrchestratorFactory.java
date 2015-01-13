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
