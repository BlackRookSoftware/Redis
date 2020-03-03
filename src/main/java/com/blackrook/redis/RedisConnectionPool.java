/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * A connection pool for Redis socket connections.
 * Connections are fair - released connections are added to the end of an "available" queue.
 * @author Matthew Tropiano
 */
public class RedisConnectionPool
{
	/** Connection info. */
	private RedisInfo info;
	
	/** Available connections. */
	private Queue<RedisConnection> availableConnections;
	/** Used connections. */
	private Set<RedisConnection> usedConnections;
	
	/** Connection pool mutex. */
	private final Object POOLMUTEX = new Object();
	
	// private constructor.
	private RedisConnectionPool()
	{
		availableConnections = new LinkedList<RedisConnection>();
		usedConnections = new HashSet<RedisConnection>();
	}
	
	/**
	 * Creates a connection pool using a connection to a host.
	 * @param connections the number of connections to open.
	 * @param host the host to connect to.
	 * @param port the port to connect to on the host.
	 * @param password the Redis DB password.
	 * @throws IOException if a connection can't be made.
	 * @throws UnknownHostException if the server host can't be resolved.
	 */
	public RedisConnectionPool(int connections, String host, int port, String password) throws IOException
	{
		this(connections, new RedisInfo(host, port, password));
	}
	
	/**
	 * Creates a connection pool using a connection to a host.
	 * @param connectionCount the number of connections to open.
	 * @param info the {@link RedisInfo} object to use to describe DB information.
	 * @throws IOException if a connection can't be made.
	 * @throws UnknownHostException if the server host can't be resolved.
	 */
	public RedisConnectionPool(int connectionCount, RedisInfo info) throws IOException
	{
		this();
		this.info = info;
		for (int i = 0; i < connectionCount; i++)
			availableConnections.add(new RedisConnection(info));
	}
	
	/**
	 * Retrieves an available connection from the pool.
	 * @return an available connection.
	 * @throws InterruptedException	if an interrupt is thrown by the current thread waiting for an available connection. 
	 * @throws IOException if a connection cannot be re-created or re-established.
	 */
	public RedisConnection getAvailableConnection() throws InterruptedException, IOException
	{
		try {
			return getAvailableConnection(0L);
		} catch (TimeoutException e) {
			return null; // Does not happen.
		}
	}
	
	/**
	 * Retrieves an available connection from the pool.
	 * @param waitMillis the amount of time (in milliseconds) to wait for a connection.
	 * @return an available connection.
	 * @throws InterruptedException	if an interrupt is thrown by the current thread waiting for an available connection. 
	 * @throws TimeoutException if the wait lapses and there are no available connections.
	 * @throws IOException if a connection cannot be re-created or re-established.
	 */
	public RedisConnection getAvailableConnection(long waitMillis) throws InterruptedException, TimeoutException, IOException
	{
		synchronized (POOLMUTEX)
		{
			if (availableConnections.isEmpty())
			{
				availableConnections.wait(waitMillis);
				if (availableConnections.isEmpty())
					throw new TimeoutException("no available connections.");
			}
			
			RedisConnection out;
			if ((out = availableConnections.poll()).isClosed())
				out = new RedisConnection(info);
			
			usedConnections.add(out);
			return out;
		}
	}

	/**
	 * Releases a Redis connection back to this pool.
	 * @param connection the connection to release.
	 * @throws IllegalStateException if the connection was never maintained by this pool.
	 */
	public void releaseConnection(RedisConnection connection)
	{
		if (!usedConnections.contains(connection))
			throw new IllegalStateException("Tried to release a connection not maintained by this pool.");
		
		synchronized (POOLMUTEX)
		{
			usedConnections.remove(connection);
			availableConnections.add(connection);
			availableConnections.notifyAll();
		}
	}
	
	/**
	 * @return the amount of available connections.
	 */
	public int getAvailableConnectionCount()
	{
		return availableConnections.size();
	}
	
	/**
	 * @return the amount of used connections.
	 */
	public int getUsedConnectionCount()
	{
		return usedConnections.size();
	}

}
