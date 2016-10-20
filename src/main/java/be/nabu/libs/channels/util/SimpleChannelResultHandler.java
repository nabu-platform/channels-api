package be.nabu.libs.channels.util;

import java.io.IOException;

import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.SingleChannelResultHandler;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public class SimpleChannelResultHandler implements ChannelResultHandler {

	private SingleChannelResultHandler handler;

	public SimpleChannelResultHandler(SingleChannelResultHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void handle(DataTransactionHandle...transactionHandles) {
		for (DataTransactionHandle handle : transactionHandles) {
			try {
				try {
					handler.handle(handle.getTransaction());
					handle.done();
				}
				catch (ChannelException e) {
					handle.fail(e.getMessage());
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public SingleChannelResultHandler getHandler() {
		return handler;
	}
	
}
