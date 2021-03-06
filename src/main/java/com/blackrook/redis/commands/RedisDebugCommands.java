/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.commands;

/**
 * Specifies the Redis commands for issuing debug conmmands.
 * @author Matthew Tropiano
 */
public interface RedisDebugCommands
{
	/**
	 * <p>From <a href="http://redis.io/commands/debug-object">http://redis.io/commands/debug-object</a>:</p>
	 * <p><strong>Available since 1.0.0.</strong></p>
	 * <p><code>DEBUG OBJECT</code> is a debugging command that should not be used
	 * by clients. Check the {@link RedisServerCommands#object(String, String)} command instead.</p>
	 * @param key the value key.
	 * @return the debug output.
	 */
	public String debugObject(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/debug-segfault">http://redis.io/commands/debug-segfault</a>:</p>
	 * <p><strong>Available since 1.0.0.</strong></p>
	 * <p><code>DEBUG SEGFAULT</code> performs an invalid memory access that 
	 * crashes Redis. It is used to simulate bugs during the development.</p>
	 */
	public void debugSegfault();

}
