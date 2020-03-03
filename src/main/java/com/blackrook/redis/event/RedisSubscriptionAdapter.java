/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.event;

/**
 * An adapter class for {@link RedisSubscriptionListener}. All methods do nothing on call.
 * @author Matthew Tropiano
 */
public class RedisSubscriptionAdapter implements RedisSubscriptionListener
{

	@Override
	public void onSubscribe(String channelName, long channelTotal)
	{
		// Do nothing.
	}

	@Override
	public void onUnsubscribe(String channelName, long channelTotal)
	{
		// Do nothing.
	}

	@Override
	public void onPatternSubscribe(String channelPattern, long channelTotal)
	{
		// Do nothing.
	}

	@Override
	public void onPatternUnsubscribe(String channelPattern, long channelTotal)
	{
		// Do nothing.
	}

	@Override
	public void onMessageReceive(String channel, String message)
	{
		// Do nothing.
	}

	@Override
	public void onPatternMessageReceive(String channelPattern, String channel, String message)
	{
		// Do nothing.
	}

}
