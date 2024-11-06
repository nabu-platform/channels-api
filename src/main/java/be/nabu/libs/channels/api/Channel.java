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

import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;

public interface Channel<T> {
	/**
	 * The context of the channel
	 */
	public String getContext();
	/**
	 * The provider can be multi directional, we need to know which direction it is being used in
	 */
	public Direction getDirection();
	/**
	 * The provider
	 */
	public String getProviderId();
	/**
	 * The properties to run the provider
	 */
	public T getProperties();
	/**
	 * The transactionality to use for this channel
	 */
	public Transactionality getTransactionality();
	/**
	 * The priority of the resulting transactions
	 */
	public int getPriority();
	/**
	 * This determines after how many transactions a finish() is called on the resulting provider
	 * If you set 0, it will wait until everything is done
	 */
	public int getFinishAmount();
	/**
	 * When executing multiple channels and one fails, do we want to continue? 
	 */
	public boolean isContinueOnFailure();
	/**
	 * If it fails, how many times do we want to retry?
	 */
	public int getRetryAmount();
	/**
	 * How long do we want to wait to retry
	 */
	public long getRetryInterval();
}
