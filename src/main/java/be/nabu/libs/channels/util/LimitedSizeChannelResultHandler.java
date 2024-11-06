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
