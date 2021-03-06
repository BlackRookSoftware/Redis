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
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import com.blackrook.redis.event.RedisMonitorEvent;
import com.blackrook.redis.event.RedisMonitorListener;
import com.blackrook.redis.event.RedisSubscriptionListener;
import com.blackrook.redis.exception.RedisException;
import com.blackrook.redis.struct.Utils;

/**
 * A connection to Redis that sends MONITOR to the database
 * and constantly fires events on reception of responses.
 * This connection cannot have any commands issued to it.
 * @author Matthew Tropiano
 */
public class RedisMonitorConnection extends RedisConnectionAbstract
{
	/** List of subscription listeners. */
	private Queue<RedisMonitorListener> listeners;

	/** Thread counter for ids. */
	private AtomicLong counter;
	/** Subscription listener thread. */
	private MonitorThread monitorThread;
	
	/**
	 * Creates an open connection to localhost, port 6379, the default Redis port.
	 * @param listeners the listeners to add to this monitor connection.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 */
	public RedisMonitorConnection(RedisMonitorListener... listeners) throws IOException
	{
		super();
		construct(listeners);
	}

	/**
	 * Creates an open connection.
	 * @param host the server hostname or address.
	 * @param port the server connection port.
	 * @param listeners the listeners to add to this monitor connection.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 */
	public RedisMonitorConnection(String host, int port, RedisMonitorListener... listeners) throws IOException
	{
		super(new RedisInfo(host, port));
		construct(listeners);
	}

	/**
	 * Creates an open connection.
	 * @param host the server hostname or address.
	 * @param port the server connection port.
	 * @param password the server database password.
	 * @param listeners the listeners to add to this monitor connection.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 * @throws RedisException if the password in the server information is incorrect. 
	 */
	public RedisMonitorConnection(String host, int port, String password, RedisMonitorListener... listeners) throws IOException
	{
		super(new RedisInfo(host, port, password));
		construct(listeners);
	}

	/**
	 * Creates an open connection.
	 * @param info the {@link RedisInfo} class detailing a connection's parameters.
	 * @param listeners the listeners to add to this monitor connection.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 * @throws RedisException if the password in the server information is incorrect. 
	 */
	public RedisMonitorConnection(RedisInfo info, RedisMonitorListener... listeners) throws IOException
	{
		super(info);
		construct(listeners);
	}

	// Finishes the constructor.
	private void construct(RedisMonitorListener... listeners)
	{
		this.listeners = new LinkedList<RedisMonitorListener>();
		this.counter = new AtomicLong(0L);

		// start monitor
		writer.writeArray("MONITOR");
		reader.readOK();
		
		addListener(listeners);
		(this.monitorThread = new MonitorThread()).start();
		while (!monitorThread.isAlive()) 
			Utils.sleep(0, 250000);
	}

	/**
	 * Adds {@link RedisMonitorListener}s to this connection.
	 * All listeners on this connection are alerted when a message is received.
	 * @param listeners the listeners to add.
	 */
	public void addListener(RedisMonitorListener... listeners)
	{
		for (RedisMonitorListener listener : listeners)
			this.listeners.add(listener);
	}

	/**
	 * Removes {@link RedisSubscriptionListener}s from this connection.
	 * @param listeners the listeners to remove.
	 */
	public void removeListeners(RedisMonitorListener... listeners)
	{
		for (RedisMonitorListener listener : listeners)
			this.listeners.remove(listener);
	}

	/**
	 * Fires an event to listeners when this connection receives a monitor event.
	 * @param event the event to send to the listeners.
	 */
	protected void fireOnMonitorEvent(RedisMonitorEvent event)
	{
		for (RedisMonitorListener listener : listeners)
			listener.onMonitorEvent(event);
	}

	/**
	 * A thread spawned for monitor connections.
	 */
	private class MonitorThread extends Thread
	{
		MonitorThread()
		{
			setName("RedisMonitor-"+counter.getAndIncrement());
			setDaemon(true);
		}
		
		@Override
		public void run()
		{
			String response = null;
			while (isConnected() && (response = reader.readString()) != null)
			{
				fireOnMonitorEvent(RedisMonitorEvent.parse(response));
			}
		}
		
	}

}
