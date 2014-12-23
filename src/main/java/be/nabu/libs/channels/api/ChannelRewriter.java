package be.nabu.libs.channels.api;

import java.util.List;

/**
 * Allows you to take a channel and rewrite it
 * For example you might want to replace generated date values etc
 */
public interface ChannelRewriter {
	public List<Channel<?>> rewrite(Channel<?> channel);
}
