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

/**
 * This interface allows for a two-phase commit of data transactions
 * Imagine that you are picking up a file on the file system, you do:
 * 
 * startTransaction();
 * uri = storeFile();
 * deleteFile();
 * commitTransaction(uri)								// Set state = committed
 * 
 * If your server fails at exactly the right moment (after the delete and before the commit), the original file
 * will be gone and while the data does exist in your datastore, you won't be able to automatically link the URI to the transaction.
 * 
 * The delete in this case should be a post processor so the framework can do this:
 * 
 * startTransaction();
 * uri = storeFile();
 * commitTransaction(uri);								// Set state = precommit but add URI
 * finalize();
 * secondCommit();										// Set state = committed
 */
public interface TwoPhaseChannelProvider<T> extends ChannelProvider<T> {
	
	/**
	 * This method can be called multiple times for recovery purposes (e.g. called first, then crash, then called again)
	 */
	public void finish(T properties) throws ChannelException;
}
