package be.nabu.libs.channels.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;

public class LimitedSizeChannelResultHandler implements ChannelResultHandler {

	private ChannelResultHandler original;
	private int amountPerFinish;
	private List<DataTransactionHandle> buffer = new ArrayList<DataTransactionHandle>();

	public LimitedSizeChannelResultHandler(ChannelResultHandler original, int amountPerFinish) {
		this.original = original;
		this.amountPerFinish = amountPerFinish;
	}

	@Override
	public void handle(DataTransactionHandle...handle) {
		buffer.addAll(Arrays.asList(handle));
		if (amountPerFinish > 0 && buffer.size() >= amountPerFinish) {
			flush();
		}
	}

	public void flush() {
		original.handle(buffer.toArray(new DataTransactionHandle[buffer.size()]));
		buffer.clear();
	}
}
