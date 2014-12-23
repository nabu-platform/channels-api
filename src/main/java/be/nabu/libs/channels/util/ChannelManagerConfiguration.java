package be.nabu.libs.channels.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;

@XmlRootElement(name = "channels")
@XmlType(propOrder = { "provider", "channel" })
public class ChannelManagerConfiguration {

	private List<ChannelImpl<?>> channels = new ArrayList<ChannelImpl<?>>();
	private List<ChannelProviderConfiguration<?>> providers = new ArrayList<ChannelProviderConfiguration<?>>();
	
	@XmlElement(name = "channel")
	public List<ChannelImpl<?>> getChannels() {
		return channels;
	}
	public void setChannels(List<ChannelImpl<?>> channels) {
		this.channels = channels;
	}

	@XmlElement(name = "provider")
	public List<ChannelProviderConfiguration<?>> getProviders() {
		return providers;
	}
	public void setProviders(List<ChannelProviderConfiguration<?>> providers) {
		this.providers = providers;
	}

	@XmlType(propOrder = { "name", "providerClass", "properties" })
	public static class ChannelProviderConfiguration<T> {
		private String name, providerClass;
		private T properties;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getProviderClass() {
			return providerClass;
		}

		public void setProviderClass(String providerClass) {
			this.providerClass = providerClass;
		}

		public T getProperties() {
			return properties;
		}

		public void setProperties(T properties) {
			this.properties = properties;
		}
	}
	
	@XmlType(propOrder = { "providerId", "context", "direction", "transactionality", "priority", "retryAmount", "retryInterval", "finishAmount", "continueOnFailure", "properties" })
	public static class ChannelImpl<T> implements Channel<T> {
		private String context, providerId;
		private Direction direction;
		private T properties;
		private Transactionality transactionality;
		private int priority, retryAmount, finishAmount;
		private long retryInterval;
		private boolean continueOnFailure;
		
		@Override
		public String getContext() {
			return context;
		}

		@Override
		public Direction getDirection() {
			return direction;
		}

		@Override
		public String getProviderId() {
			return providerId;
		}

		@Override
		public T getProperties() {
			return properties;
		}

		@Override
		public Transactionality getTransactionality() {
			return transactionality;
		}

		@Override
		public int getPriority() {
			return priority;
		}

		@Override
		public int getFinishAmount() {
			return finishAmount;
		}

		@Override
		public boolean isContinueOnFailure() {
			return continueOnFailure;
		}

		@Override
		public int getRetryAmount() {
			return retryAmount;
		}

		@Override
		public long getRetryInterval() {
			return retryInterval;
		}

		public void setContext(String context) {
			this.context = context;
		}

		public void setProviderId(String providerId) {
			this.providerId = providerId;
		}

		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		public void setProperties(T properties) {
			this.properties = properties;
		}

		public void setTransactionality(Transactionality transactionality) {
			this.transactionality = transactionality;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public void setRetryAmount(int retryAmount) {
			this.retryAmount = retryAmount;
		}

		public void setFinishAmount(int finishAmount) {
			this.finishAmount = finishAmount;
		}

		public void setRetryInterval(long retryInterval) {
			this.retryInterval = retryInterval;
		}

		public void setContinueOnFailure(boolean continueOnFailure) {
			this.continueOnFailure = continueOnFailure;
		}
	}
}
