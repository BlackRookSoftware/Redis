/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.event;

import com.blackrook.redis.RedisMonitorConnection;

/**
 * A listener for {@link RedisMonitorConnection}s that encapsulate
 * events received via a MONITOR connection.
 * @author Matthew Tropiano
 */
public interface RedisMonitorListener
{
	/**
	 * Called when a MONITOR message is received.
	 * @param event the encapsulated event.
	 * @see RedisMonitorEvent
	 */
	public void onMonitorEvent(RedisMonitorEvent event);
}
