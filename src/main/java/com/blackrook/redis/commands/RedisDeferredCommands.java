/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.blackrook.redis.data.KeyValue;
import com.blackrook.redis.enums.Aggregation;
import com.blackrook.redis.enums.BitwiseOperation;
import com.blackrook.redis.enums.SortOrder;

/**
 * Interface for Redis connection stuff from deferred calls like pipelines or transactions
 * that don't require immediate feedback.
 * @author Matthew Tropiano
 */
@SuppressWarnings("javadoc")
public interface RedisDeferredCommands
{
	/**
	 * <p>From <a href="http://redis.io/commands/echo">http://redis.io/commands/echo</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 */
	public void echo(String message);

	/**
	 * <p>From <a href="http://redis.io/commands/del">http://redis.io/commands/del</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of keys that 
	 * will be removed. When a key to remove holds a value other than a string, the 
	 * individual complexity for this key is O(M) where M is the number of elements 
	 * in the list, set, sorted set or hash. Removing a single key that holds a 
	 * string value is O(1).
	 * <p>Removes the specified keys. A key is ignored if it does not exist.
	 * @param key the first key to delete.
	 * @param keys the additional keys to delete.
	 */
	public void del(String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/dump">http://redis.io/commands/dump</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1) to access the key and 
	 * additional O(N*M) to serialized it, where N is the number of Redis objects 
	 * composing the value and M their average size. For small string values the 
	 * time complexity is thus O(1)+O(1*M) where M is small, so simply O(1).
	 * <p>Serialize the value stored at key in a Redis-specific format and return
	 * it to the user. The returned value can be synthesized back into a Redis 
	 * key using the {@link #restore} command.
	 * 
	 */
	public void dump(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/exists">http://redis.io/commands/exists</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns if <code>key</code> exists.
	 * 
	 */
	public void exists(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/expire">http://redis.io/commands/expire</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Set a timeout on <code>key</code>. After the timeout has expired, the key
	 * will automatically be deleted. A key with an associated timeout is often 
	 * said to be <em>volatile</em> in Redis terminology.
	 * @param key the key to expire.
	 * @param seconds the time-to-live in seconds.
	 * 
	 */
	public void expire(String key, long seconds);

	/**
	 * <p>From <a href="http://redis.io/commands/expireat">http://redis.io/commands/expireat</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p><b>expireat</b> has the same effect and semantic as {@link #expire}, but 
	 * instead of specifying the number of seconds representing the TTL (time to 
	 * live), it takes an absolute <a href="http://en.wikipedia.org/wiki/Unix_time">Unix timestamp</a> 
	 * (seconds since January 1, 1970).
	 * @param key the key to expire.
	 * @param timestamp the timestamp in from-Epoch milliseconds.
	 * 
	 */
	public void expireat(String key, long timestamp);

	/**
	 * <p>From <a href="http://redis.io/commands/keys">http://redis.io/commands/keys</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the number of keys 
	 * in the database, under the assumption that the key names in the database 
	 * and the given pattern have limited length.
	 * <p>Returns all keys matching <code>pattern</code>.
	 * @param pattern a wildcard pattern for matching key names.
	 * 
	 */
	public void keys(String pattern);

	/**
	 * <p>From <a href="http://redis.io/commands/move">http://redis.io/commands/move</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Move <code>key</code> from the currently selected database 
	 * to the specified destination database. When <code>key</code> already exists in the 
	 * destination database, or it does not exist in the source database, it does nothing.
	 * It is possible to use <a href="/commands/move">MOVE</a> as a locking primitive because of this.
	 * @param key the key to move.
	 * @param db the target database. 
	 * 
	 */
	public void move(String key, long db);

	/**
	 * <p>From <a href="http://redis.io/commands/persist">http://redis.io/commands/persist</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Remove the existing timeout on <code>key</code>, turning the key 
	 * from <em>volatile</em> (a key with an expire set) to <em>persistent</em> 
	 * (a key that will never expire as no timeout is associated).
	 * @param key the key to persist (remove TTL).
	 * 
	 */
	public void persist(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/pexpire">http://redis.io/commands/pexpire</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>This command works exactly like {@link #expire} but the time to 
	 * live of the key is specified in milliseconds instead of seconds.
	 * @param key the key to expire.
	 * @param milliseconds the time-to-live in milliseconds.
	 * 
	 */
	public void pexpire(String key, long milliseconds);

	/**
	 * <p>From <a href="http://redis.io/commands/pexpireat">http://redis.io/commands/pexpireat</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>PEXPIREAT has the same effect and semantic as {@link #expireat}, but 
	 * the Unix time at which the key will expire is specified in milliseconds 
	 * instead of seconds.
	 * @param key the key to expire.
	 * @param timestamp the timestamp in from-Epoch milliseconds.
	 * 
	 */
	public void pexpireat(String key, long timestamp);

	/**
	 * <p>From <a href="http://redis.io/commands/pttl">http://redis.io/commands/pttl</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Like {@link #ttl}, this command returns the remaining time to live 
	 * of a key that has an expire set, with the sole difference that TTL returns 
	 * the amount of remaining time in seconds while PTTL returns it in milliseconds.
	 * @param key the key to inspect.
	 * 
	 */
	public void pttl(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/publish">http://redis.io/commands/publish</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N+M) where N is the number of clients subscribed 
	 * to the receiving channel and M is the total number of subscribed patterns (by any client).
	 * <p>Posts a message to the given channel.
	 * 
	 */
	public void publish(String channel, String message);

	/**
	 * <p>From <a href="http://redis.io/commands/randomkey">http://redis.io/commands/randomkey</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Return a random key from the currently selected database.
	 * 
	 */
	public void randomkey();

	/**
	 * <p>From <a href="http://redis.io/commands/rename">http://redis.io/commands/rename</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Renames <code>key</code> to <code>newkey</code>. It returns an error
	 * when the source and destination names are the same, or when <code>key</code> 
	 * does not exist. If <code>newkey</code> already exists it is overwritten, when 
	 * this happens {@link #rename} executes an implicit {@link #del} operation, so 
	 * if the deleted key contains a very big value it may cause high latency even 
	 * if {@link #rename} itself is usually a constant-time operation.
	 * @param key the old name. 
	 * @param newkey the new name. 
	 * 
	 */
	public void rename(String key, String newkey);

	/**
	 * <p>From <a href="http://redis.io/commands/renamenx">http://redis.io/commands/renamenx</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Renames <code>key</code> to <code>newkey</code> if <code>newkey</code> 
	 * does not yet exist. It returns an error under the same conditions as {@link #rename}.
	 * @param key the old name. 
	 * @param newkey the new name. 
	 * 
	 */
	public void renamenx(String key, String newkey);

	/**
	 * <p>From <a href="http://redis.io/commands/restore">http://redis.io/commands/restore</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1) to create the new key and 
	 * additional O(N*M) to recostruct the serialized value, where N is the number
	 * of Redis objects composing the value and M their average size. For small string 
	 * values the time complexity is thus O(1)+O(1*M) where M is small, so simply O(1). 
	 * However for sorted set values the complexity is O(N*M*log(N)) because inserting 
	 * values into sorted sets is O(log(N)).
	 * <p>Create a key associated with a value that is obtained by deserializing 
	 * the provided serialized value (obtained via {@link #dump}).
	 * @param key the key to restore.
	 * @param ttl the time-to-live in milliseconds.
	 * @param serializedvalue the serialized value (from a {@link #dump} call). 
	 * 
	 */
	public void restore(String key, long ttl, String serializedvalue);

	/**
	 * <p>From <a href="http://redis.io/commands/sort">http://redis.io/commands/sort</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N+M*log(M)) where N is the number 
	 * of elements in the list or set to sort, and M the number of returned elements. 
	 * When the elements are not sorted, complexity is currently O(N) as there is a 
	 * copy step that will be avoided in next releases.
	 * <p>Returns or stores the elements contained in the list, set, or sorted set
	 * at <code>key</code>. By default, sorting is numeric and elements are compared 
	 * by their value interpreted as double precision floating point number.
	 * @param key the key to sort the contents of.
	 * @param pattern if not null, 
	 * @param sortOrder if true, sort descending. if false or null, sort ascending.
	 * @param alpha if true, sort lexicographically, not by a score.
	 * @param limitOffset if not null, the starting offset into the list (0-based).
	 * @param limitCount if not null, the amount of objects from the offset to sort. else, return all the way to the end.
	 * @param storeKey if not null, this is the key to store the result in.
	 * @param getPatterns the patterns for finding the sort score.
	 * 
	 */
	public void sort(String key, String pattern, SortOrder sortOrder, boolean alpha, Long limitOffset, Long limitCount, String storeKey, String... getPatterns);

	/**
	 * <p>From <a href="http://redis.io/commands/ttl">http://redis.io/commands/ttl</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the remaining time to live of a key that has a timeout. This 
	 * introspection capability allows a Redis client to check how many seconds 
	 * a given key will continue to be part of the dataset.
	 * @param key the key to inspect.
	 * 
	 */
	public void ttl(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/type">http://redis.io/commands/type</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the string representation of the type of the value stored at 
	 * <code>key</code>.
	 * 
	 */
	public void type(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/append">http://redis.io/commands/append</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1). The amortized time complexity is O(1) assuming the appended value is small and the already present value is of any size, since the dynamic string library used by Redis will double the free space available on every reallocation.
	 * <p>If <code>key</code> already exists and is a string, this command appends the <code>value</code> at the end of the string. If <code>key</code> does not exist it is created and set as an empty string, so <a href="/commands/append">APPEND</a> will be similar to <a href="/commands/set">SET</a> in this special case.
	 * 
	 */
	public void append(String key, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/bitcount">http://redis.io/commands/bitcount</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N)
	 * <p>Count the number of set bits (population counting) in a string.
	 * 
	 */
	public void bitcount(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/bitcount">http://redis.io/commands/bitcount</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N)
	 * <p>Count the number of set bits (population counting) in a string between a start and end bit.
	 * 
	 */
	public void bitcount(String key, long start, long end);

	/**
	 * <p>From <a href="http://redis.io/commands/bitop">http://redis.io/commands/bitop</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N)
	 * <p>Perform a bitwise operation between multiple keys (containing 
	 * string values) and store the result in the destination key.
	 * 
	 * equal to the size of the longest input string.
	 */
	public void bitop(BitwiseOperation operation, String destkey, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/bitpos">http://redis.io/commands/bitpos</a>:
	 * <p><strong>Available since 2.8.7.</strong>
	 * <p><strong>Time complexity:</strong> O(N)
	 * <p>Return the position of the first bit set to 1 or 0 in a string.
	 * 
	 */
	public void bitpos(String key, long bit);

	/**
	 * <p>From <a href="http://redis.io/commands/bitpos">http://redis.io/commands/bitpos</a>:
	 * <p><strong>Available since 2.8.7.</strong>
	 * <p><strong>Time complexity:</strong> O(N)
	 * <p>Return the position of the first bit set to 1 or 0 in a string.
	 * 
	 */
	public void bitpos(String key, long bit, Long start, Long end);

	/**
	 * <p>From <a href="http://redis.io/commands/decr">http://redis.io/commands/decr</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Decrements the number stored at <code>key</code> by one. If the key does not exist, 
	 * it is set to <code>0</code> before performing the operation. An error is returned if 
	 * the key contains a value of the wrong type or contains a string that can not be 
	 * represented as integer. This operation is limited to <strong>64 bit signed integers</strong>.
	 * 
	 */
	public void decr(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/decrby">http://redis.io/commands/decrby</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Decrements the number stored at <code>key</code> by <code>decrement</code>. If 
	 * the key does not exist, it is set to <code>0</code> before performing the operation. 
	 * An error is returned if the key contains a value of the wrong type or contains a 
	 * string that can not be represented as integer. This operation is limited to 64 
	 * bit signed integers.
	 * 
	 */
	public void decrby(String key, long decrement);

	/**
	 * <p>From <a href="http://redis.io/commands/get">http://redis.io/commands/get</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Get the value of <code>key</code>. If the key does not exist the special value 
	 * <code>null</code> is returned. An error is returned if the value stored at 
	 * <code>key</code> is not a string, because <a href="/commands/get">GET</a> only 
	 * handles string values.
	 * 
	 * does not exist.
	 */
	public void get(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/getbit">http://redis.io/commands/getbit</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the bit value at <em>offset</em> in the string value stored at <em>key</em>.
	 * 
	 */
	public void getbit(String key, long offset);

	/**
	 * <p>From <a href="http://redis.io/commands/getrange">http://redis.io/commands/getrange</a>:
	 * <p><strong>Available since 2.4.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the length of the returned 
	 * string. The complexity is ultimately determined by the returned length, but because 
	 * creating a substring from an existing string is very cheap, it can be considered 
	 * O(1) for small strings.
	 * it is called <code>SUBSTR</code> in Redis versions <code>&lt;= 2.0</code>.
	 * 
	 */
	public void getrange(String key, long start, long end);

	/**
	 * <p>From <a href="http://redis.io/commands/getset">http://redis.io/commands/getset</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Atomically sets <code>key</code> to <code>value</code> and returns the 
	 * old value stored at <code>key</code>. Returns an error when <code>key</code> 
	 * exists but does not hold a string value.
	 * 
	 */
	public void getset(String key, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/getset">http://redis.io/commands/getset</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Atomically sets <code>key</code> to <code>value</code> and returns the 
	 * old value stored at <code>key</code>. Returns an error when <code>key</code> 
	 * exists but does not hold a string value.
	 * 
	 */
	public void getset(String key, Number value);

	/**
	 * <p>From <a href="http://redis.io/commands/incr">http://redis.io/commands/incr</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Increments the number stored at <code>key</code> by one. If the key does not 
	 * exist, it is set to <code>0</code> before performing the operation. An error is 
	 * returned if the key contains a value of the wrong type or contains a string that 
	 * can not be represented as integer. This operation is limited to 64 bit signed integers.
	 * 
	 */
	public void incr(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/incrby">http://redis.io/commands/incrby</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Increments the number stored at <code>key</code> by <code>increment</code>. 
	 * If the key does not exist, it is set to <code>0</code> before performing the operation. 
	 * An error is returned if the key contains a value of the wrong type or contains a 
	 * string that can not be represented as integer. This operation is limited to 64 bit 
	 * signed integers.
	 * 
	 */
	public void incrby(String key, long increment);

	/**
	 * <p>From <a href="http://redis.io/commands/incrbyfloat">http://redis.io/commands/incrbyfloat</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Increment the string representing a floating point number stored at <code>key</code> by the specified <code>increment</code>. If the key does not exist, it is set to <code>0</code> before performing the operation. An error is returned if one of the following conditions occur:
	 * 
	 */
	public void incrbyfloat(String key, double increment);

	/**
	 * <p>From <a href="http://redis.io/commands/mget">http://redis.io/commands/mget</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of keys to retrieve.
	 * <p>Returns the values of all specified keys. For every key that does not hold a string 
	 * value or does not exist, the special value <code>nil</code> is returned. Because of 
	 * this, the operation never fails.
	 * 
	 */
	public void mget(String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/mset">http://redis.io/commands/mset</a>:
	 * <p><strong>Available since 1.0.1.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of keys to set.
	 * <p>Sets the given keys to their respective values. <code>MSET</code> replaces existing 
	 * values with new values, just as regular <a href="/commands/set">SET</a>. See {@link #msetnx} 
	 * if you don't want to overwrite existing values.
	 * 
	 */
	public void mset(String key, String value, String... keyValues);

	/**
	 * Like {@link #mset(String, String, String...)}, but takes key-value pairs.
	 */
	@SuppressWarnings("unchecked")
	public void mset(KeyValue<String, Object>... pairs);

	/**
	 * <p>From <a href="http://redis.io/commands/msetnx">http://redis.io/commands/msetnx</a>:
	 * <p><strong>Available since 1.0.1.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of keys to set.
	 * <p>Sets the given keys to their respective values. <code>MSETNX</code> will not 
	 * perform any operation at all even if just a single key already exists.
	 * 
	 */
	public void msetnx(String key, String value, String... keyValues);

	/** 
	 * Like {@link #msetnx(String, String, String...)}, but takes key-value pairs.
	 */
	@SuppressWarnings("unchecked")
	public void msetnx(KeyValue<String, Object>... pairs);

	/**
	 * <p>From <a href="http://redis.io/commands/psetex">http://redis.io/commands/psetex</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p><code>PSETEX</code> works exactly like {@link #setex(String, long, String)} with the 
	 * sole difference that the expire time is specified in milliseconds instead of seconds.
	 * 
	 */
	public void psetex(String key, long milliseconds, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/set">http://redis.io/commands/set</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Set <code>key</code> to hold the string <code>value</code>. If <code>key</code> 
	 * already holds a value, it is overwritten, regardless of its type. Any previous time 
	 * to live associated with the key is discarded on successful <code>SET</code> operation.
	 * 
	 */
	public void set(String key, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/set">http://redis.io/commands/set</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Set <code>key</code> to hold the string <code>value</code>. If <code>key</code> 
	 * already holds a value, it is overwritten, regardless of its type. Any previous time 
	 * to live associated with the key is discarded on successful <code>SET</code> operation.
	 * 
	 */
	public void set(String key, Number value);

	/**
	 * <p>From <a href="http://redis.io/commands/setbit">http://redis.io/commands/setbit</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Sets or clears the bit at <em>offset</em> in the string value stored at <em>key</em>.
	 * 
	 */
	public void setbit(String key, long offset, long value);

	/**
	 * <p>From <a href="http://redis.io/commands/setex">http://redis.io/commands/setex</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Set <code>key</code> to hold the string <code>value</code> and set <code>key</code> 
	 * to timeout after a given number of seconds. This command is equivalent to executing 
	 * the following commands:
	 * 
	 */
	public void setex(String key, long seconds, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/setnx">http://redis.io/commands/setnx</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Set <code>key</code> to hold string <code>value</code> if <code>key</code> does not 
	 * exist. In that case, it is equal to {@link #set(String, String)}. When <code>key</code> 
	 * already holds a value, no operation is performed. <code>SETNX</code> is short for &quot;<strong>SET</strong> 
	 * if <strong>N</strong> ot e <strong>X</strong> ists&quot;.
	 * 
	 */
	public void setnx(String key, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/setrange">http://redis.io/commands/setrange</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1), not counting the time taken to copy 
	 * the new string in place. Usually, this string is very small so the amortized 
	 * complexity is O(1). Otherwise, complexity is O(M) with M being the length of 
	 * the value argument.
	 * <p>Overwrites part of the string stored at <em>key</em>, starting at the specified 
	 * offset, for the entire length of <em>value</em>. If the offset is larger than the 
	 * current length of the string at <em>key</em>, the string is padded with zero-bytes 
	 * to make <em>offset</em> fit. Non-existing keys are considered as empty strings, so 
	 * this command will make sure it holds a string large enough to be able to 
	 * set <em>value</em> at <em>offset</em>.
	 * 
	 */
	public void setrange(String key, long offset, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/strlen">http://redis.io/commands/strlen</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the length of the string value stored at <code>key</code>. 
	 * An error is returned when <code>key</code> holds a non-string value.
	 * 
	 */
	public void strlen(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/hdel">http://redis.io/commands/hdel</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of fields to be removed.
	 * <p>Removes the specified fields from the hash stored at <code>key</code>. 
	 * Specified fields that do not exist within this hash are ignored. If <code>key</code> 
	 * does not exist, it is treated as an empty hash and this command returns <code>0</code>.
	 * 
	 * specified but non existing fields.
	 */
	public void hdel(String key, String field, String... fields);

	/**
	 * <p>From <a href="http://redis.io/commands/hexists">http://redis.io/commands/hexists</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns if <code>field</code> is an existing field in the hash stored at <code>key</code>.
	 * 
	 */
	public void hexists(String key, String field);

	/**
	 * <p>From <a href="http://redis.io/commands/hget">http://redis.io/commands/hget</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the value associated with <code>field</code> in the hash stored 
	 * at <code>key</code>.
	 * 
	 * when <code>field</code> is not present in the hash or <code>key</code> does not exist.
	 */
	public void hget(String key, String field);

	/**
	 * <p>From <a href="http://redis.io/commands/hgetall">http://redis.io/commands/hgetall</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the size of the hash.
	 * <p>Returns all fields and values of the hash stored at <code>key</code>. In 
	 * the returned value, every field name is followed by its value, so the length 
	 * of the reply is twice the size of the hash.
	 * 
	 * list when <code>key</code> does not exist.
	 */
	public void hgetall(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/hincrby">http://redis.io/commands/hincrby</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Increments the number stored at <code>field</code> in the hash stored 
	 * at <code>key</code> by <code>increment</code>. If <code>key</code> does 
	 * not exist, a new key holding a hash is created. If <code>field</code> does 
	 * not exist the value is set to <code>0</code> before the operation is performed.
	 * 
	 */
	public void hincrby(String key, String field, long increment);

	/**
	 * <p>From <a href="http://redis.io/commands/hincrbyfloat">http://redis.io/commands/hincrbyfloat</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Increment the specified <code>field</code> of an hash stored at <code>key</code>, 
	 * and representing a floating point number, by the specified <code>increment</code>. 
	 * If the field does not exist, it is set to <code>0</code> before performing the 
	 * operation.
	 * 
	 */
	public void hincrbyfloat(String key, String field, double increment);

	/**
	 * <p>From <a href="http://redis.io/commands/hkeys">http://redis.io/commands/hkeys</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the size of the hash.
	 * <p>Returns all field names in the hash stored at <code>key</code>.
	 * 
	 */
	public void hkeys(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/hlen">http://redis.io/commands/hlen</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the number of fields contained in the hash stored at <code>key</code>.
	 * 
	 */
	public void hlen(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/hmget">http://redis.io/commands/hmget</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of fields being requested.
	 * <p>Returns the values associated with the specified <code>fields</code> in the hash stored at <code>key</code>.
	 * 
	 */
	public void hmget(String key, String field, String... fields);

	/**
	 * <p>From <a href="http://redis.io/commands/hmset">http://redis.io/commands/hmset</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of fields being set.
	 * <p>Sets the specified fields to their respective values in the hash stored 
	 * at <code>key</code>. This command overwrites any existing fields in the hash. 
	 * If <code>key</code> does not exist, a new key holding a hash is created.
	 * <p>Parameters should alternate between field, value, field, value ...
	 * 
	 */
	public void hmset(String key, String field, String value, String... fieldvalues);

	/**
	 * <p>From <a href="http://redis.io/commands/hset">http://redis.io/commands/hset</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Sets <code>field</code> in the hash stored at <code>key</code> to <code>value</code>. 
	 * If <code>key</code> does not exist, a new key holding a hash is created. If 
	 * <code>field</code> already exists in the hash, it is overwritten.
	 * 
	 */
	public void hset(String key, String field, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/hset">http://redis.io/commands/hset</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Sets <code>field</code> in the hash stored at <code>key</code> to <code>value</code>. 
	 * If <code>key</code> does not exist, a new key holding a hash is created. If 
	 * <code>field</code> already exists in the hash, it is overwritten.
	 * 
	 */
	public void hset(String key, String field, Number value);

	/**
	 * <p>From <a href="http://redis.io/commands/hsetnx">http://redis.io/commands/hsetnx</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Sets <code>field</code> in the hash stored at <code>key</code> to <code>value</code>, 
	 * only if <code>field</code> does not yet exist. If <code>key</code> does not exist, a 
	 * new key holding a hash is created. If <code>field</code> already exists, this 
	 * operation has no effect.
	 * 
	 */
	public void hsetnx(String key, String field, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/hsetnx">http://redis.io/commands/hsetnx</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Sets <code>field</code> in the hash stored at <code>key</code> to <code>value</code>, 
	 * only if <code>field</code> does not yet exist. If <code>key</code> does not exist, a 
	 * new key holding a hash is created. If <code>field</code> already exists, this 
	 * operation has no effect.
	 * 
	 */
	public void hsetnx(String key, String field, Number value);

	/**
	 * <p>From <a href="http://redis.io/commands/hvals">http://redis.io/commands/hvals</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the size of the hash.
	 * <p>Returns all values in the hash stored at <code>key</code>.
	 * 
	 */
	public void hvals(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/blpop">http://redis.io/commands/blpop</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p><code>BLPOP</code> is a blocking list pop primitive. It is the blocking version 
	 * of {@link #lpop(String)} because it blocks the connection when there are no elements 
	 * to pop from any of the given lists. An element is popped from the head of the first 
	 * list that is non-empty, with the given keys being checked in the order that they are 
	 * given. A <code>timeout</code> of zero can be used to block indefinitely. Timeout is in seconds.
	 * 
	 */
	public void blpop(long timeout, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/brpop">http://redis.io/commands/brpop</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p><code>BRPOP</code> is a blocking list pop primitive. It is the blocking 
	 * version of {@link #rpop} because it blocks the connection when there are 
	 * no elements to pop from any of the given lists. An element is popped from 
	 * the tail of the first list that is non-empty, with the given keys being 
	 * checked in the order that they are given. A <code>timeout</code> of zero 
	 * can be used to block indefinitely. Timeout is in seconds.
	 * 
	 */
	public void brpop(long timeout, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/brpoplpush">http://redis.io/commands/brpoplpush</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p><code>BRPOPLPUSH</code> is the blocking variant of {@link #rpoplpush(String, String)}. 
	 * When <code>source</code> contains elements, this command behaves exactly like 
	 * {@link #rpoplpush(String, String)}. When <code>source</code> is empty, 
	 * Redis will block the connection until another client pushes to it or 
	 * until <code>timeout</code> is reached. A <code>timeout</code> of zero 
	 * can be used to block indefinitely. Timeout is in seconds.
	 * 
	 */
	public void brpoplpush(long timeout, String source, String destination);

	/**
	 * <p>From <a href="http://redis.io/commands/lindex">http://redis.io/commands/lindex</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of elements 
	 * to traverse to get to the element at index. This makes asking for the first 
	 * or the last element of the list O(1).
	 * <p>Returns the element at index <code>index</code> in the list stored at 
	 * <code>key</code>. The index is zero-based, so <code>0</code> means the first 
	 * element, <code>1</code> the second element and so on. Negative indices can be 
	 * used to designate elements starting at the tail of the list. Here, <code>-1</code> 
	 * means the last element, <code>-2</code> means the penultimate and so forth.
	 * 
	 */
	public void lindex(String key, long index);

	/**
	 * <p>From <a href="http://redis.io/commands/linsert">http://redis.io/commands/linsert</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of elements to 
	 * traverse before seeing the value pivot. This means that inserting somewhere on 
	 * the left end on the list (head) can be considered O(1) and inserting somewhere 
	 * on the right end (tail) is O(N).
	 * <p>Inserts <code>value</code> in the list stored at <code>key</code> either 
	 * before or after the reference value <code>pivot</code>.
	 * 
	 * when the value <code>pivot</code> was not found.
	 */
	public void linsert(String key, boolean before, String pivot, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/linsert">http://redis.io/commands/linsert</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of elements to 
	 * traverse before seeing the value pivot. This means that inserting somewhere on 
	 * the left end on the list (head) can be considered O(1) and inserting somewhere 
	 * on the right end (tail) is O(N).
	 * <p>Inserts <code>value</code> in the list stored at <code>key</code> either 
	 * before or after the reference value <code>pivot</code>.
	 * 
	 * when the value <code>pivot</code> was not found.
	 */
	public void linsert(String key, boolean before, String pivot, Number value);

	/**
	 * <p>From <a href="http://redis.io/commands/llen">http://redis.io/commands/llen</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the length of the list stored at <code>key</code>. If <code>key</code> does 
	 * not exist, it is interpreted as an empty list and <code>0</code> is returned. An error 
	 * is returned when the value stored at <code>key</code> is not a list.
	 * 
	 */
	public void llen(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/lpop">http://redis.io/commands/lpop</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Removes and returns the first element of the list stored at <code>key</code>.
	 * 
	 */
	public void lpop(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/lpush">http://redis.io/commands/lpush</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Insert all the specified values at the head of the list stored at <code>key</code>. 
	 * If <code>key</code> does not exist, it is created as empty list before performing the 
	 * push operations. When <code>key</code> holds a value that is not a list, an error is returned.
	 * 
	 */
	public void lpush(String key, String value, String... values);

	/**
	 * <p>From <a href="http://redis.io/commands/lpushx">http://redis.io/commands/lpushx</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Inserts <code>value</code> at the head of the list stored at <code>key</code>,
	 * only if <code>key</code> already exists and holds a list. In contrary to 
	 * {@link #lpush}, no operation will be performed when 
	 * <code>key</code> does not yet exist.
	 * 
	 */
	public void lpushx(String key, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/lrange">http://redis.io/commands/lrange</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(S+N) where S is the start offset 
	 * and N is the number of elements in the specified range.
	 * <p>Returns the specified elements of the list stored at <code>key</code>. 
	 * The offsets <code>start</code> and <code>stop</code> are zero-based indexes, 
	 * with <code>0</code> being the first element of the list (the head of the 
	 * list), <code>1</code> being the next element and so on.
	 * 
	 */
	public void lrange(String key, long start, long stop);

	/**
	 * <p>From <a href="http://redis.io/commands/lrem">http://redis.io/commands/lrem</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the length of the list.
	 * <p>Removes the first <code>count</code> occurrences of elements equal to 
	 * <code>value</code> from the list stored at <code>key</code>. The 
	 * <code>count</code> argument influences the operation in the following ways:
	 * <ul>
	 * <li><code>count &gt; 0</code>: Remove elements equal to <code>value</code> moving from head to tail.</li>
	 * <li><code>count &lt; 0</code>: Remove elements equal to <code>value</code> moving from tail to head.</li>
	 * <li><code>count = 0</code>: Remove all elements equal to <code>value</code>.</li>
	 * </ul>
	 * 
	 */
	public void lrem(String key, long count, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/lset">http://redis.io/commands/lset</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the length 
	 * of the list. Setting either the first or the last element of the list is O(1).
	 * <p>Sets the list element at <code>index</code> to <code>value</code>. For 
	 * more information on the <code>index</code> argument, see {@link #lindex(String, long)}.
	 * 
	 */
	public void lset(String key, long index, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/ltrim">http://redis.io/commands/ltrim</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the 
	 * number of elements to be removed by the operation.
	 * <p>Trim an existing list so that it will contain only the 
	 * specified range of elements specified. Both <code>start</code> and <code>stop</code> 
	 * are zero-based indexes, where <code>0</code> is the first element of the list 
	 * (the head), <code>1</code> the next element and so on.
	 * 
	 */
	public void ltrim(String key, long start, long stop);

	/**
	 * <p>From <a href="http://redis.io/commands/rpop">http://redis.io/commands/rpop</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Removes and returns the last element of the list stored at <code>key</code>.
	 * 
	 */
	public void rpop(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/rpoplpush">http://redis.io/commands/rpoplpush</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Atomically returns and removes the last element (tail) of the list stored 
	 * at <code>source</code>, and pushes the element at the first element (head) 
	 * of the list stored at <code>destination</code>.
	 * 
	 */
	public void rpoplpush(String source, String destination);

	/**
	 * <p>From <a href="http://redis.io/commands/rpush">http://redis.io/commands/rpush</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Insert all the specified values at the tail of the list stored at <code>key</code>. 
	 * If <code>key</code> does not exist, it is created as empty list before performing the 
	 * push operation. When <code>key</code> holds a value that is not a list, an error is 
	 * returned.
	 * 
	 */
	public void rpush(String key, String value, String... values);

	/**
	 * <p>From <a href="http://redis.io/commands/rpushx">http://redis.io/commands/rpushx</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Inserts <code>value</code> at the tail of the list stored at <code>key</code>, 
	 * only if <code>key</code> already exists and holds a list. In contrary to 
	 * {@link #rpush}, no operation will be performed when 
	 * <code>key</code> does not yet exist.
	 * 
	 */
	public void rpushx(String key, String value);

	/**
	 * <p>From <a href="http://redis.io/commands/sadd">http://redis.io/commands/sadd</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of members to be added.
	 * <p>Add the specified members to the set stored at <code>key</code>. Specified members 
	 * that are already a member of this set are ignored. If <code>key</code> does not exist, 
	 * a new set is created before adding the specified members.
	 * 
	 */
	public void sadd(String key, String member, String... members);

	/**
	 * <p>From <a href="http://redis.io/commands/sadd">http://redis.io/commands/sadd</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of members to be added.
	 * <p>Add the specified members to the set stored at <code>key</code>. Specified members 
	 * that are already a member of this set are ignored. If <code>key</code> does not exist, 
	 * a new set is created before adding the specified members.
	 */
	public void sadd(String key, Object member, Object... members);

	/**
	 * <p>From <a href="http://redis.io/commands/scard">http://redis.io/commands/scard</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the set cardinality (number of elements) of the set stored at <code>key</code>.
	 * 
	 */
	public void scard(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/sdiff">http://redis.io/commands/sdiff</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the total number of elements in all given sets.
	 * <p>Returns the members of the set resulting from the difference between the first set and all the successive sets.
	 * 
	 */
	public void sdiff(String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/sdiffstore">http://redis.io/commands/sdiffstore</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the total number of elements in all given sets.
	 * <p>This command is equal to <a href="/commands/sdiff">SDIFF</a>, but instead of 
	 * returning the resulting set, it is stored in <code>destination</code>.
	 * 
	 */
	public void sdiffstore(String destination, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/sinter">http://redis.io/commands/sinter</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N*M) worst case where N is the cardinality 
	 * of the smallest set and M is the number of sets.
	 * <p>Returns the members of the set resulting from the intersection of all the given sets.
	 * 
	 */
	public void sinter(String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/sinterstore">http://redis.io/commands/sinterstore</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N*M) worst case where N is the cardinality of the smallest set and M is the number of sets.
	 * <p>This command is equal to <a href="/commands/sinter">SINTER</a>, but instead of 
	 * returning the resulting set, it is stored in <code>destination</code>.
	 * 
	 */
	public void sinterstore(String destination, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/sismember">http://redis.io/commands/sismember</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns if <code>member</code> is a member of the set stored at <code>key</code>.
	 * 
	 */
	public void sismember(String key, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/sismember">http://redis.io/commands/sismember</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns if <code>member</code> is a member of the set stored at <code>key</code>.
	 */
	public void sismember(String key, Number member);

	/**
	 * <p>From <a href="http://redis.io/commands/smembers">http://redis.io/commands/smembers</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the set cardinality.
	 * <p>Returns all the members of the set value stored at <code>key</code>.
	 * 
	 */
	public void smembers(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/smove">http://redis.io/commands/smove</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Move <code>member</code> from the set at <code>source</code> to the set at 
	 * <code>destination</code>. This operation is atomic. In every given moment the element 
	 * will appear to be a member of <code>source</code> <strong>or</strong> <code>destination</code> for other clients.
	 * 
	 */
	public void smove(String source, String destination, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/spop">http://redis.io/commands/spop</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Removes and returns a random element from the set value stored at <code>key</code>.
	 * 
	 */
	public void spop(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/srandmember">http://redis.io/commands/srandmember</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1).
	 * <p>When called with just the <code>key</code> argument, return a random element 
	 * from the set value stored at <code>key</code>.
	 * 
	 */
	public void srandmember(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/srandmember">http://redis.io/commands/srandmember</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the absolute value of the passed count.
	 * <p>When called with just the <code>key</code> argument, return a random element 
	 * from the set value stored at <code>key</code>.
	 * 
	 */
	public void srandmember(String key, long count);

	/**
	 * <p>From <a href="http://redis.io/commands/srem">http://redis.io/commands/srem</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of members to be removed.
	 * <p>Remove the specified members from the set stored at <code>key</code>. Specified 
	 * members that are not a member of this set are ignored. If <code>key</code> does not 
	 * exist, it is treated as an empty set and this command returns <code>0</code>.
	 * 
	 */
	public void srem(String key, String member, String... members);

	/**
	 * <p>From <a href="http://redis.io/commands/srem">http://redis.io/commands/srem</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of members to be removed.
	 * <p>Remove the specified members from the set stored at <code>key</code>. Specified 
	 * members that are not a member of this set are ignored. If <code>key</code> does not 
	 * exist, it is treated as an empty set and this command returns <code>0</code>.
	 */
	public void srem(String key, Object member, Object... members);

	/**
	 * <p>From <a href="http://redis.io/commands/sunion">http://redis.io/commands/sunion</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the total number of elements in all given sets.
	 * <p>Returns the members of the set resulting from the union of all the given sets.
	 * 
	 */
	public void sunion(String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/sunionstore">http://redis.io/commands/sunionstore</a>:
	 * <p><strong>Available since 1.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the total number of elements in all given sets.
	 * <p>This command is equal to <a href="/commands/sunion">SUNION</a>, but instead 
	 * of returning the resulting set, it is stored in <code>destination</code>.
	 * 
	 */
	public void sunionstore(String destination, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/zadd">http://redis.io/commands/zadd</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)) where N is the number of elements in the sorted set.
	 * <p>Adds all the specified members with the specified scores to the sorted set 
	 * stored at <code>key</code>. It is possible to specify multiple score/member pairs. 
	 * If a specified member is already a member of the sorted set, the score is updated 
	 * and the element reinserted at the right position to ensure the correct ordering. 
	 * If <code>key</code> does not exist, a new sorted set with the specified members as 
	 * sole members is created, like if the sorted set was empty. If the key exists but 
	 * does not hold a sorted set, an error is returned.
	 */
	public void zadd(String key, double score, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/zadd">http://redis.io/commands/zadd</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)) where N is the number of elements in the sorted set.
	 * <p>Adds all the specified members with the specified scores to the sorted set 
	 * stored at <code>key</code>. It is possible to specify multiple score/member pairs. 
	 * If a specified member is already a member of the sorted set, the score is updated 
	 * and the element reinserted at the right position to ensure the correct ordering. 
	 * If <code>key</code> does not exist, a new sorted set with the specified members as 
	 * sole members is created, like if the sorted set was empty. If the key exists but 
	 * does not hold a sorted set, an error is returned.
	 */
	public void zadd(String key, double score, Number member);

	/**
	 * <p>From <a href="http://redis.io/commands/zadd">http://redis.io/commands/zadd</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)) where N is the number of elements in the sorted set.
	 * <p>Adds all the specified members with the specified scores to the sorted set 
	 * stored at <code>key</code>. It is possible to specify multiple score/member pairs. 
	 * If a specified member is already a member of the sorted set, the score is updated 
	 * and the element reinserted at the right position to ensure the correct ordering. 
	 * If <code>key</code> does not exist, a new sorted set with the specified members as 
	 * sole members is created, like if the sorted set was empty. If the key exists but 
	 * does not hold a sorted set, an error is returned.
	 */
	@SuppressWarnings("unchecked")
	public void zadd(String key, KeyValue<Double, String>... pairs);

	/**
	 * <p>From <a href="http://redis.io/commands/zcard">http://redis.io/commands/zcard</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the sorted set cardinality (number of elements) of the sorted set stored at <code>key</code>.
	 * 
	 */
	public void zcard(String key);

	/**
	 * <p>From <a href="http://redis.io/commands/zcount">http://redis.io/commands/zcount</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)) with N being the number of elements in the sorted set.
	 * <p>Returns the number of elements in the sorted set at <code>key</code> with 
	 * a score between <code>min</code> and <code>max</code>.
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zcount(String key, String min, String max);

	/**
	 * Like {@link #zcount(String, String, String)}, 
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zcount(String key, double min, double max);

	/**
	 * <p>From <a href="http://redis.io/commands/zincrby">http://redis.io/commands/zincrby</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)) where N is the number of elements in the sorted set.
	 * <p>Increments the score of <code>member</code> in the sorted set stored at 
	 * <code>key</code> by <code>increment</code>. If <code>member</code> does not exist in the 
	 * sorted set, it is added with <code>increment</code> as its score (as if its previous 
	 * score was <code>0.0</code>). If <code>key</code> does not exist, a new sorted set with 
	 * the specified <code>member</code> as its sole member is created.
	 * 
	 */
	public void zincrby(String key, double increment, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/zrange">http://redis.io/commands/zrange</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of 
	 * elements in the sorted set and M the number of elements returned.
	 * <p>Returns the specified range of elements in the sorted set stored at <code>key</code>. 
	 * The elements are considered to be ordered from the lowest to the highest score. 
	 * Lexicographical order is used for elements with equal score.
	 * 
	 */
	public void zrange(String key, long start, long stop, boolean withScores);

	/**
	 * <p>From <a href="http://redis.io/commands/zrangebyscore">http://redis.io/commands/zrangebyscore</a>:
	 * <p><strong>Available since 1.0.5.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of elements 
	 * in the sorted set and M the number of elements being returned. If M is constant (e.g. 
	 * always asking for the first 10 elements with LIMIT), you can consider it O(log(N)).
	 * <p>Returns all the elements in the sorted set at <code>key</code> with a score 
	 * between <code>min</code> and <code>max</code> (including elements with score equal 
	 * to <code>min</code> or <code>max</code>). The elements are considered to be ordered 
	 * from low to high scores.
	 * <p>The optional <code>LIMIT</code> argument can be used to only get a range of the matching
	 * elements (similar to <em>SELECT LIMIT offset, count</em> in SQL).
	 * Keep in mind that if <code>offset</code> is large, the sorted set needs to be traversed for
	 * <code>offset</code> elements before getting to the elements to return, which can add up to
	 * <span class="math">O(N) </span>time complexity.
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zrangebyscore(String key, String min, String max, boolean withScores, Long limitOffset, Long limitCount);

	/**
	 * Like {@link #zrangebyscore(String, String, String, boolean)},
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zrangebyscore(String key, double min, double max, boolean withScores);

	/**
	 * Like {@link #zrangebyscore(String, String, String, boolean, Long, Long)}, except specifies no limit.
	 */
	public void zrangebyscore(String key, String min, String max, boolean withScores);

	/**
	 * Like {@link #zrangebyscore(String, String, String, boolean, Long, Long)}, 
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zrangebyscore(String key, double min, double max, boolean withScores, Long limitOffset, Long limitCount);

	/**
	 * <p>From <a href="http://redis.io/commands/zrank">http://redis.io/commands/zrank</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N))
	 * <p>Returns the rank of <code>member</code> in the sorted set stored at <code>key</code>,
	 * with the scores ordered from low to high. The rank (or index) is 0-based, which
	 * means that the member with the lowest score has rank <code>0</code>.
	 * 
	 * If <code>member</code> does not exist in the sorted set or <code>key</code> 
	 * does not exist, <code>null</code>.
	 */
	public void zrank(String key, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/zrank">http://redis.io/commands/zrank</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N))
	 * <p>Returns the rank of <code>member</code> in the sorted set stored at <code>key</code>,
	 * with the scores ordered from low to high. The rank (or index) is 0-based, which
	 * means that the member with the lowest score has rank <code>0</code>.
	 * 
	 * If <code>member</code> does not exist in the sorted set or <code>key</code> 
	 * does not exist, <code>null</code>.
	 */
	public void zrank(String key, Number member);

	/**
	 * <p>From <a href="http://redis.io/commands/zrem">http://redis.io/commands/zrem</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(M*log(N)) with N being the number of 
	 * elements in the sorted set and M the number of elements to be removed.
	 * <p>Removes the specified members from the sorted set stored at <code>key</code>. 
	 * Non existing members are ignored.
	 * 
	 */
	public void zrem(String key, String member, String... members);

	/**
	 * <p>From <a href="http://redis.io/commands/zrem">http://redis.io/commands/zrem</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(M*log(N)) with N being the number of 
	 * elements in the sorted set and M the number of elements to be removed.
	 * <p>Removes the specified members from the sorted set stored at <code>key</code>. 
	 * Non existing members are ignored.
	 */
	public void zrem(String key, Number member, Number... members);

	/**
	 * <p>From <a href="http://redis.io/commands/zremrangebyrank">http://redis.io/commands/zremrangebyrank</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of 
	 * elements in the sorted set and M the number of elements removed by the operation.
	 * <p>Removes all elements in the sorted set stored at <code>key</code> with rank 
	 * between <code>start</code> and <code>stop</code>. Both <code>start</code> and 
	 * <code>stop</code> are <code>0</code> -based indexes with <code>0</code> being 
	 * the element with the lowest score. These indexes can be negative numbers, where 
	 * they indicate offsets starting at the element with the highest score. For 
	 * example: <code>-1</code> is the element with the highest score, <code>-2</code> 
	 * the element with the second highest score and so forth.
	 * 
	 */
	public void zremrangebyrank(String key, long start, long stop);

	/**
	 * <p>From <a href="http://redis.io/commands/zremrangebyscore">http://redis.io/commands/zremrangebyscore</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of 
	 * elements in the sorted set and M the number of elements removed by the operation.
	 * <p>Removes all elements in the sorted set stored at <code>key</code> with a 
	 * score between <code>min</code> and <code>max</code> (inclusive).
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zremrangebyscore(String key, String min, String max);

	/**
	 * Like {@link #zremrangebyscore(String, String, String)},
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zremrangebyscore(String key, double min, double max);

	/**
	 * <p>From <a href="http://redis.io/commands/zrevrank">http://redis.io/commands/zrevrank</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N))
	 * <p>Returns the rank of <code>member</code> in the sorted set stored at <code>key</code>, 
	 * with the scores ordered from high to low. The rank (or index) is 0-based, which means 
	 * that the member with the highest score has rank <code>0</code>.
	 * 
	 * If <code>member</code> does not exist in the sorted set or <code>key</code> does not exist, <code>null</code>.
	 */
	public void zrevrank(String key, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/zrevrange">http://redis.io/commands/zrevrange</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of 
	 * elements in the sorted set and M the number of elements returned.
	 * <p>Returns the specified range of elements in the sorted set stored at 
	 * <code>key</code>. The elements are considered to be ordered from the highest 
	 * to the lowest score. Descending lexicographical order is used for elements with equal score.
	 * 
	 */
	public void zrevrange(String key, long start, long stop, boolean withScores);

	/**
	 * <p>From <a href="http://redis.io/commands/zrevrangebyscore">http://redis.io/commands/zrevrangebyscore</a>:
	 * <p><strong>Available since 2.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of 
	 * elements in the sorted set and M the number of elements removed by the operation.
	 * <p>Returns all the elements in the sorted set at key with a score between 
	 * <code>max</code> and <code>min</code> (including elements with score equal 
	 * to max or min). In contrary to the default ordering of sorted sets, for this 
	 * command the elements are considered to be ordered from high to low scores.
	 * <p>The optional <code>LIMIT</code> argument can be used to only get a range of the matching
	 * elements (similar to <em>SELECT LIMIT offset, count</em> in SQL).
	 * Keep in mind that if <code>offset</code> is large, the sorted set needs to be traversed for
	 * <code>offset</code> elements before getting to the elements to return, which can add up to
	 * <span class="math">O(N) </span>time complexity.
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zrevrangebyscore(String key, String min, String max, boolean withScores, Long limitOffset, Long limitCount);

	/**
	 * Like {@link #zrevrangebyscore(String, String, String, boolean)},
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zrevrangebyscore(String key, double min, double max, boolean withScores);

	/**
	 * Like {@link #zrevrangebyscore(String, String, String, boolean, Long, Long)}, except specifies no limit.
	 */
	public void zrevrangebyscore(String key, String min, String max, boolean withScores);

	/**
	 * Like {@link #zrevrangebyscore(String, String, String, boolean, Long, Long)}, 
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zrevrangebyscore(String key, double min, double max, boolean withScores, Long limitOffset, Long limitCount);

	/**
	 * <p>From <a href="http://redis.io/commands/zscore">http://redis.io/commands/zscore</a>:
	 * <p><strong>Available since 1.2.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Returns the score of <code>member</code> in the sorted set at <code>key</code>.
	 * 
	 */
	public void zscore(String key, String member);

	/**
	 * <p>From <a href="http://redis.io/commands/zinterstore">http://redis.io/commands/zinterstore</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N*K)+O(M*log(M)) worst case with N being the 
	 * smallest input sorted set, K being the number of input sorted sets and M being the 
	 * number of elements in the resulting sorted set.
	 * <p>Computes the intersection of <code>numkeys</code> sorted sets given by the 
	 * specified keys, and stores the result in <code>destination</code>. It is mandatory 
	 * to provide the number of input keys (<code>numkeys</code>) before passing the 
	 * input keys and the other (optional) arguments.
	 * 
	 */
	public void zinterstore(String destination, double[] weights, Aggregation aggregation, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/zunionstore">http://redis.io/commands/zunionstore</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N)+O(M log(M)) with N being the sum of 
	 * the sizes of the input sorted sets, and M being the number of elements in the 
	 * resulting sorted set.
	 * <p>Computes the union of <code>numkeys</code> sorted sets given by the specified 
	 * keys, and stores the result in <code>destination</code>. It is mandatory to 
	 * provide the number of input keys (<code>numkeys</code>) before passing the input 
	 * keys and the other (optional) arguments.
	 * 
	 */
	public void zunionstore(String destination, double[] weights, Aggregation aggregation, String key, String... keys);

	/**
	 * <p>From <a href="http://redis.io/commands/zlexcount">http://redis.io/commands/zlexcount</a>:
	 * <p><strong>Available since 2.8.9.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)) with N being the number of elements in the sorted set.
	 * <p>When all the elements in a sorted set are inserted with the same score, in 
	 * order to force lexicographical ordering, this command returns the number of elements 
	 * in the sorted set at <code>key</code> with a value between <code>min</code> and <code>max</code>.
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zlexcount(String key, String min, String max);

	/**
	 * <p>From <a href="http://redis.io/commands/zrangebylex">http://redis.io/commands/zrangebylex</a>:
	 * <p><strong>Available since 2.8.9.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of elements in 
	 * the sorted set and M the number of elements being returned. If M is constant (e.g. always 
	 * asking for the first 10 elements with LIMIT), you can consider it O(log(N)).
	 * <p>When all the elements in a sorted set are inserted with the same score, in order 
	 * to force lexicographical ordering, this command returns all the elements in the sorted 
	 * set at <code>key</code> with a value between <code>min</code> and <code>max</code>.
	 * <p>The optional <code>LIMIT</code> argument can be used to only get a range of the matching
	 * elements (similar to <em>SELECT LIMIT offset, count</em> in SQL).
	 * Keep in mind that if <code>offset</code> is large, the sorted set needs to be traversed for
	 * <code>offset</code> elements before getting to the elements to return, which can add up to
	 * <span class="math">O(N) </span>time complexity.
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zrangebylex(String key, String min, String max, Long limitOffset, Long limitCount);

	/**
	 * <p>From <a href="http://redis.io/commands/zremrangebylex">http://redis.io/commands/zremrangebylex</a>:
	 * <p><strong>Available since 2.8.9.</strong>
	 * <p><strong>Time complexity:</strong> O(log(N)+M) with N being the number of elements 
	 * in the sorted set and M the number of elements removed by the operation.
	 * <p>When all the elements in a sorted set are inserted with the same score, in order to 
	 * force lexicographical ordering, this command removes all elements in the sorted set stored 
	 * at <code>key</code> between the lexicographical range specified by <code>min</code> and <code>max</code>.
	 * <p>The arguments <code>min</code> and <code>max</code> are Strings so they can accept special ranges.
	 * 
	 */
	public void zremrangebylex(String key, String min, String max);

	/**
	 * Like {@link #zinterstore(String, double[], Aggregation, String, String...)}, except no weights are
	 * applied to the source value scores.
	 * <p>Equivalent to: <code>zinterstore(destination, null, aggregation, key, keys)</code>
	 */
	public void zinterstore(String destination, Aggregation aggregation, String key, String... keys);

	/**
	 * Like {@link #zinterstore(String, double[], Aggregation, String, String...)}, except it does no
	 * aggregation of scores.
	 * <p>Equivalent to: <code>zinterstore(destination, weights, null, key, keys)</code>
	 */
	public void zinterstore(String destination, double[] weights, String key, String... keys);

	/**
	 * Like {@link #zinterstore(String, double[], Aggregation, String, String...)}, except no weights are
	 * applied to the source value scores, and does no aggregation of scores.
	 * <p>Equivalent to: <code>zinterstore(destination, null, null, key, keys)</code>
	 */
	public void zinterstore(String destination, String key, String... keys);

	/**
	 * Like {@link #zunionstore(String, double[], Aggregation, String, String...)}, except no weights are
	 * applied to the source value scores.
	 * <p>Equivalent to: <code>zunionstore(destination, null, aggregation, key, keys)</code>
	 */
	public void zunionstore(String destination, Aggregation aggregation, String key, String... keys);

	/**
	 * Like {@link #zunionstore(String, double[], Aggregation, String, String...)}, except it does no
	 * aggregation of scores.
	 * <p>Equivalent to: <code>zunionstore(destination, weights, null, key, keys)</code>
	 */
	public void zunionstore(String destination, double[] weights, String key, String... keys);

	/**
	 * Like {@link #zunionstore(String, double[], Aggregation, String, String...)}, except no weights are
	 * applied to the source value scores, and does no aggregation of scores.
	 * <p>Equivalent to: <code>zunionstore(destination, null, null, key, keys)</code>
	 */
	public void zunionstore(String destination, String key, String... keys);

	/**
	 * Like {@link #zlexcount(String, String, String)}, 
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zlexcount(String key, double min, double max);

	/**
	 * Like {@link #zrangebylex(String, String, String, Long, Long)},
	 * except it accepts doubles for min and max, not strings.
	 */
	public void zrangebylex(String key, double min, double max, Long limitOffset, Long limitCount);

	/**
	 * Like {@link #zrangebylex(String, String, String, Long, Long)}, with no limit.
	 */
	public void zrangebylex(String key, String min, String max);

	/**
	 * Like {@link #zrangebylex(String, String, String)},
	 * except it accepts doubles for min and max, not strings, with no limit.
	 */
	public void zrangebylex(String key, double min, double max);

	/**
	 * Like {@link #zrangebylex(String, String, String)},
	 * except it accepts doubles for min and max.
	 */
	public void zremrangebylex(String key, double min, double max);

	/**
	 * <p>From <a href="http://redis.io/commands/eval">http://redis.io/commands/eval</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> Depends on the script that is executed.
	 * <p>Evaluates a Lua script. The keys specified in <code>keys</code> should 
	 * be used as a hint for Redis as to what keys are touched during the script call.
	 * 
	 */
	public void eval(String scriptContent, String[] keys, Object... args);

	/**
	 * <p>From <a href="http://redis.io/commands/evalsha">http://redis.io/commands/evalsha</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> Depends on the script that is executed.
	 * <p>Evaluates a script cached on the server side by its SHA1 digest. 
	 * Scripts are cached on the server side using the {@link #scriptLoad(String)} command. 
	 * The command is otherwise identical to {@link #eval(String, String[], Object...)}.
	 * 
	 */
	public void evalsha(String hash, String[] keys, Object... args);

	/**
	 * <p>From <a href="http://redis.io/commands/script-exists">http://redis.io/commands/script-exists</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the number of scripts 
	 * to check (so checking a single script is an O(1) operation).
	 * <p>Returns information about the existence of the scripts in the script cache.
	 * 
	 * SHA1 digest arguments. For every corresponding SHA1 digest of a script that actually 
	 * exists in the script cache, true is returned, otherwise false is returned.
	 */
	public void scriptExists(String scriptHash, String... scriptHashes);

	/**
	 * <p>From <a href="http://redis.io/commands/script-flush">http://redis.io/commands/script-flush</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the number of scripts in cache
	 * <p>Flush the Lua scripts cache.
	 * 
	 */
	public void scriptFlush();

	/**
	 * <p>From <a href="http://redis.io/commands/script-kill">http://redis.io/commands/script-kill</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(1)
	 * <p>Kills the currently executing Lua script, assuming no write operation was yet performed by the script.
	 * 
	 */
	public void scriptKill(String hash);

	/**
	 * <p>From <a href="http://redis.io/commands/script-load">http://redis.io/commands/script-load</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the length in bytes of the script body.
	 * <p>Load a script into the scripts cache, without executing it. After the specified 
	 * command is loaded into the script cache it will be callable using {@link #evalsha(String, String[], Object...)} 
	 * with the correct SHA1 digest of the script, exactly like after the first successful invocation of {@link #eval(String, String[], Object...)}.
	 * 
	 */
	public void scriptLoad(String content);

	/**
	 * <p>From <a href="http://redis.io/commands/script-load">http://redis.io/commands/script-load</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the length in bytes of the script body.
	 * <p>Load a script into the scripts cache from the specified file without executing it. After the specified 
	 * command is loaded into the script cache it will be callable using {@link #evalsha(String, String[], Object...)} 
	 * with the correct SHA1 digest of the script, exactly like after the first successful invocation of {@link #eval(String, String[], Object...)}.
	 * 
	 */
	public void scriptLoad(File content) throws IOException;

	/**
	 * <p>From <a href="http://redis.io/commands/script-load">http://redis.io/commands/script-load</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the length in bytes of the script body.
	 * <p>Load a script into the scripts cache from the specified input stream (until the end is reached) without executing it. 
	 * The stream is not closed after read. After the specified command is loaded into the 
	 * script cache it will be callable using {@link #evalsha(String, String[], Object...)} 
	 * with the correct SHA1 digest of the script, exactly like after the first 
	 * successful invocation of {@link #eval(String, String[], Object...)}.
	 * 
	 */
	public void scriptLoad(InputStream content) throws IOException;

}
