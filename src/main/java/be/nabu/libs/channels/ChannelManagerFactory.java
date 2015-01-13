package be.nabu.libs.channels;

import be.nabu.libs.channels.api.ChannelManager;

/**
 * This factory allows for centralized configuration but does not currently allow service-like detection
 */
public class ChannelManagerFactory {

	private static ChannelManagerFactory instance;
	
	public static ChannelManagerFactory getInstance() {
		if (instance == null) {
			synchronized(ChannelManagerFactory.class) {
				if (instance == null) {
					instance = new ChannelManagerFactory();
				}
			}
		}
		return instance;
	}
	
	private ChannelManager channelManager;
	
	public void setChannelManager(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}
	
	public void unsetChannelManager(ChannelManager channelManager) {
		this.channelManager = null;
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}
}
