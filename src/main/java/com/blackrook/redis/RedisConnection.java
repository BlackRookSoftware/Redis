/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackrook.redis.commands.RedisConnectionCommands;
import com.blackrook.redis.commands.RedisHyperlogCommands;
import com.blackrook.redis.commands.RedisScanCommands;
import com.blackrook.redis.data.KeyValue;
import com.blackrook.redis.data.RedisCursor;
import com.blackrook.redis.data.RedisObject;
import com.blackrook.redis.enums.Aggregation;
import com.blackrook.redis.enums.BitwiseOperation;
import com.blackrook.redis.enums.DataType;
import com.blackrook.redis.enums.ReturnType;
import com.blackrook.redis.enums.SortOrder;
import com.blackrook.redis.exception.RedisException;
import com.blackrook.redis.hints.RedisIgnore;
import com.blackrook.redis.hints.RedisName;
import com.blackrook.redis.struct.TypeConverter;
import com.blackrook.redis.struct.TypeProfileFactory;
import com.blackrook.redis.struct.Utils;
import com.blackrook.redis.struct.TypeProfileFactory.MemberPolicy;
import com.blackrook.redis.struct.TypeProfileFactory.Profile;
import com.blackrook.redis.struct.TypeProfileFactory.Profile.FieldInfo;
import com.blackrook.redis.struct.TypeProfileFactory.Profile.MethodInfo;

/**
 * A single connection to a Redis server.
 * @author Matthew Tropiano
 */
public class RedisConnection extends RedisConnectionAbstract implements RedisConnectionCommands, RedisHyperlogCommands, RedisScanCommands
{
	private static final TypeProfileFactory PROFILE_FACTORY = new TypeProfileFactory(new MemberPolicy()
	{
		@Override
		public boolean isIgnored(Field field)
		{
			return field.getAnnotation(RedisIgnore.class) != null;
		}

		@Override
		public boolean isIgnored(Method method)
		{
			return method.getAnnotation(RedisIgnore.class) != null;
		}

		@Override
		public String getAlias(Field field)
		{
			RedisName anno = field.getAnnotation(RedisName.class);
			return anno != null ? anno.value() : null;
		}

		@Override
		public String getAlias(Method method)
		{
			RedisName anno = method.getAnnotation(RedisName.class);
			return anno != null ? anno.value() : null;
		}
	});
	
	private static final TypeConverter DEFAULT_CONVERTER = new TypeConverter(PROFILE_FACTORY);
	
	/**
	 * Creates an open connection to localhost, port 6379, the default Redis port.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 */
	public RedisConnection() throws IOException
	{
		super();
	}

	/**
	 * Creates an open connection.
	 * @param host the server hostname or address.
	 * @param port the server connection port.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 */
	public RedisConnection(String host, int port) throws IOException
	{
		super(new RedisInfo(host, port));
	}

	/**
	 * Creates an open connection.
	 * @param host the server hostname or address.
	 * @param port the server connection port.
	 * @param password the server database password.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 * @throws RedisException if the password in the server information is incorrect. 
	 */
	public RedisConnection(String host, int port, String password) throws IOException
	{
		super(new RedisInfo(host, port, password));
	}

	/**
	 * Creates an open connection.
	 * @param info the {@link RedisInfo} class detailing a connection.
	 * @throws IOException if an I/O error occurs when creating the socket.
	 * @throws UnknownHostException if the IP address of the host could not be determined.
	 * @throws SecurityException if a security manager exists and doesn't allow the connection to be made.
	 * @throws RedisException if the password in the server information is incorrect. 
	 */
	public RedisConnection(RedisInfo info) throws IOException
	{
		super(info);
	}

	/**
	 * Creates a pipelined set of commands.
	 * @return a new pipeline.
	 */
	public RedisPipeline startPipeline()
	{
		return RedisPipeline.createPipeline(this);
	}
	
	@Override
	public String echo(String message)
	{
		writer.writeArray("ECHO", message);
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public long ping()
	{
		long time = System.currentTimeMillis();
		writer.writeArray("PING");
		ReturnType.PONG.readFrom(reader);
		return System.currentTimeMillis() - time;
	}

	@Override
	public boolean quit()
	{
		writer.writeArray("QUIT");
		ReturnType.OK.readFrom(reader);
		disconnect();
		return true;
	}

	@Override
	public String clientGetName()
	{
		writer.writeArray("CLIENT", "GETNAME");
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public boolean clientSetName(String name)
	{
		writer.writeArray("CLIENT", "SETNAME", name);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public long del(String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"DEL", key}, keys));
		else
			writer.writeArray("DEL", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String dump(String key)
	{
		writer.writeArray("DUMP", key);
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public boolean exists(String key)
	{
		writer.writeArray("EXISTS", key);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean expire(String key, long seconds)
	{
		writer.writeArray("EXPIRE", key, seconds);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean expireat(String key, long timestamp)
	{
		writer.writeArray("EXPIREAT", key, timestamp);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public String[] keys(String pattern)
	{
		writer.writeArray("KEYS", pattern);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public boolean move(String key, long db)
	{
		writer.writeArray("MOVE", key, db);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean persist(String key)
	{
		writer.writeArray("PERSIST", key);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean pexpire(String key, long milliseconds)
	{
		writer.writeArray("PEXPIRE", key, milliseconds);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean pexpireat(String key, long timestamp)
	{
		writer.writeArray("PEXPIREAT", key, timestamp);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public long pttl(String key)
	{
		writer.writeArray("PTTL", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long publish(String channel, String message)
	{
		writer.writeArray("PUBLISH", channel, message);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String randomkey()
	{
		writer.writeArray("RANDOMKEY");
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public boolean rename(String key, String newkey)
	{
		writer.writeArray("RENAME", key, newkey);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean renamenx(String key, String newkey)
	{
		writer.writeArray("RENAMENX", key, newkey);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean restore(String key, long ttl, String serializedvalue)
	{
		writer.writeArray("RESTORE", key, ttl, serializedvalue);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public RedisCursor scan(long cursor)
	{
		return scan(cursor, null, null);
	}

	@Override
	public RedisCursor scan(long cursor, String pattern)
	{
		return scan(cursor, pattern, null);
	}

	@Override
	public RedisCursor scan(long cursor, long count)
	{
		return scan(cursor, null, count);
	}

	@Override
	public RedisCursor scan(long cursor, String pattern, Long count)
	{
		if (pattern == null)
		{
			if (count == null)
				writer.writeArray("SCAN", cursor);
			else
				writer.writeArray("SCAN", cursor, "COUNT", count);
		}
		else
		{
			if (count == null)
				writer.writeArray("SCAN", cursor, "MATCH", pattern);
			else
				writer.writeArray("SCAN", cursor, "MATCH", pattern, "COUNT", count);
		}
		return RedisCursor.create(ReturnType.INTEGER.readFrom(reader), ReturnType.ARRAY.readFrom(reader));
	}

	@Override
	public String[] sort(String key, String pattern, SortOrder sortOrder, boolean alpha, Long limitOffset, Long limitCount, String storeKey, String... getPatterns)
	{
		List<Object> out = new ArrayList<Object>(13 + getPatterns.length);

		out.add("SORT");
		out.add(key);
		
		if (pattern != null)
		{
			out.add("BY");
			out.add(pattern);
		}

		if (limitOffset != null && limitCount != null)
		{
			out.add("LIMIT");
			out.add(limitOffset);
			out.add(limitCount);
		}

		if (getPatterns.length > 0)
		{
			out.add("GET");
			for (String gp : getPatterns)
				out.add(gp);
		}
		
		out.add(sortOrder != null ? sortOrder.name() : SortOrder.ASC);

		if (alpha)
			out.add("ALPHA");

		if (storeKey != null)
		{
			out.add("STORE");
			out.add(storeKey);
		}
		
		writer.writeArray(out);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long ttl(String key)
	{
		writer.writeArray("TTL", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public DataType type(String key)
	{
		writer.writeArray("TYPE", key);
		return ReturnType.DATATYPE.readFrom(reader);
	}

	@Override
	public long append(String key, String value)
	{
		writer.writeArray("APPEND", key, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long bitcount(String key)
	{
		writer.writeArray("BITCOUNT", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long bitcount(String key, long start, long end)
	{
		writer.writeArray("BITCOUNT", key, start, end);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long bitop(BitwiseOperation operation, String destkey, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"BITOP", operation.name(), destkey, key}, keys));
		else
			writer.writeArray("BITOP", operation.name(), destkey, key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long bitpos(String key, long bit)
	{
		writer.writeArray("BITPOS", key, bit);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long bitpos(String key, long bit, Long start, Long end)
	{
		List<Object> out = new ArrayList<Object>(5);
		out.add("BITPOS");
		out.add(key);
		out.add(bit);
		if (start != null)
			out.add(start);
		if (end != null)
			out.add(end);
		writer.writeArray(out);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long decr(String key)
	{
		writer.writeArray("DECR", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long decrby(String key, long decrement)
	{
		writer.writeArray("DECRBY", key, decrement);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String get(String key)
	{
		writer.writeArray("GET", key);
		return ReturnType.STRING.readFrom(reader);
	}
	
	/**
	 * Just like {@link #get(String)}, but it casts the result to a long integer.
	 * @param key the data key.
	 * @return the value of <code>key</code>, or <code>null</code> when <code>key</code> 
	 * does not exist.
	 */
	public Long getLong(String key)
	{
		String out = get(key);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public long getbit(String key, long offset)
	{
		writer.writeArray("GETBIT", key, offset);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String getrange(String key, long start, long end)
	{
		writer.writeArray("GETRANGE", key, start, end);
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public String getset(String key, String value)
	{
		writer.writeArray("GETSET", key, value);
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public String getset(String key, Number value)
	{
		writer.writeArray("GETSET", key, value);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Just like {@link #getset(String, String)}, but it casts the result to a long integer.
	 * @param key th@param key the data key.
	 * @param value 
	 * @return the previous value.
	 */
	public Long getsetLong(String key, String value)
	{
		String out = getset(key, value);
		return out != null ? Utils.parseLong(out) : null;
	}

	/**
	 * Just like {@link #getset(String, String)}, but it casts the result to a long integer.
	 * @param key the data key.
	 * @param value 
	 * @return the previous value.
	 */
	public Long getsetLong(String key, Number value)
	{
		String out = getset(key, value);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public long incr(String key)
	{
		writer.writeArray("INCR", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long incrby(String key, long increment)
	{
		writer.writeArray("INCRBY", key, increment);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public double incrbyfloat(String key, double increment)
	{
		writer.writeArray("INCRBYFLOAT", key, increment);
		return ReturnType.DOUBLE.readFrom(reader);
	}

	@Override
	public String[] mget(String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"MGET", key}, keys));
		else
			writer.writeArray("MGET", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public boolean mset(String key, String value, String... keyValues)
	{
		if (keyValues.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"MSET", key, value}, keyValues));
		else
			writer.writeArray("MSET", key, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean mset(KeyValue<String, Object>... pairs)
	{
		if (pairs.length == 0)
			return true;
		
		List<Object> out = new ArrayList<Object>(1 + (pairs.length * 2));
		out.add("MSET");
		
		for (KeyValue<String, Object> p : pairs)
		{
			if (p.getValue() != null)
			{
				out.add(p.getKey());
				out.add(String.valueOf(p.getValue()));
			}
		}
		writer.writeArray(out);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean msetnx(String key, String value, String... keyValues)
	{
		if (keyValues.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"MSETNX", key, value}, keyValues));
		else
			writer.writeArray("MSETNX", key, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean msetnx(KeyValue<String, Object>... pairs)
	{
		if (pairs.length == 0)
			return false;
		
		List<Object> out = new ArrayList<Object>(1 + (pairs.length * 2));
		out.add("MSETNX");
		
		for (KeyValue<String, Object> p : pairs)
		{
			if (p.getValue() != null)
			{
				out.add(p.getKey());
				out.add(String.valueOf(p.getValue()));
			}
		}
		writer.writeArray(out);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean psetex(String key, long milliseconds, String value)
	{
		writer.writeArray("PSETEX", key, milliseconds, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean set(String key, String value)
	{
		writer.writeArray("SET", key, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean set(String key, Number value)
	{
		writer.writeArray("SET", key, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public long setbit(String key, long offset, long value)
	{
		writer.writeArray("SETBIT", key, offset, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public boolean setex(String key, long seconds, String value)
	{
		writer.writeArray("SETEX", key, seconds, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean setnx(String key, String value)
	{
		writer.writeArray("SETNX", key, value);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public long setrange(String key, long offset, String value)
	{
		writer.writeArray("SETRANGE", key, offset, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long strlen(String key)
	{
		writer.writeArray("STRLEN", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long hdel(String key, String field, String... fields)
	{
		if (fields.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"HDEL", key, field}, fields));
		else
			writer.writeArray("HDEL", key, field);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public boolean hexists(String key, String field)
	{
		writer.writeArray("HEXISTS", key, field);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public String hget(String key, String field)
	{
		writer.writeArray("HGET", key, field);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Just like {@link #hget(String, String)}, but it casts the result to a long integer.
	 * @param key the data key.
	 * @param field 
	 * @return the previous value.
	 */
	public Long hgetLong(String key, String field)
	{
		String out = hget(key, field);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public String[] hgetall(String key)
	{
		writer.writeArray("HGETALL", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	/**
	 * Just like {@link #hgetall(String)}, except the keys and values are returned in a map of key to value.
	 * @param key the data key.
	 * @return the returned map.
	 */
	public HashMap<String, String> hgetallMap(String key)
	{
		String[] keyvals = hgetall(key);
		HashMap<String, String> out = new HashMap<String, String>(keyvals.length / 2);
		for (int i = 0; i < keyvals.length; i += 2)
			out.put(keyvals[i], keyvals[i + 1]);
		return out;
	}

	/**
	 * Just like {@link #hgetall(String)}, except the keys and values are set on 
	 * a new instance of a Java object via reflection. Fields/Setter Methods annotated with
	 * {@link RedisIgnore} are ignored.
	 * @param key the data key.
	 * @param type the class type to crate and return.
	 * @param <T> the return type.
	 * @return the returned object.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 * @throws ClassCastException if a incoming type cannot be converted to a field value.
	 */
	public <T> T hgetallObject(String key, Class<T> type)
	{
		T out = Utils.create(type);
		hgetallObject(key, out);
		return out;
	}

	/**
	 * Just like {@link #hgetall(String)}, except the keys and values are set on 
	 * an existing instance of a Java object via reflection. Fields/Setter Methods annotated with
	 * {@link RedisIgnore} are ignored.
	 * @param key the data key.
	 * @param object the object to store.
	 * @param <T> the object type.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 * @throws ClassCastException if a incoming type cannot be converted to a field value.
	 */
	public <T> void hgetallObject(String key, T object)
	{
		String[] keyvals = hgetall(key);
		for (int i = 0; i < keyvals.length; i += 2)
		{
			String k = keyvals[i];
			String v = keyvals[i + 1];
			DEFAULT_CONVERTER.applyMemberToObject(k, v, object);
		}
	}

	@Override
	public long hincrby(String key, String field, long increment)
	{
		writer.writeArray("HINCRBY", key, field, increment);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public double hincrbyfloat(String key, String field, double increment)
	{
		writer.writeArray("HINCRBYFLOAT", key, field, increment);
		return ReturnType.DOUBLE.readFrom(reader);
	}

	@Override
	public String[] hkeys(String key)
	{
		writer.writeArray("HKEYS", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long hlen(String key)
	{
		writer.writeArray("HLEN", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String[] hmget(String key, String field, String... fields)
	{
		if (fields.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"HMGET", key, field}, fields));
		else
			writer.writeArray("HMGET", key, field);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public boolean hmset(String key, String field, String value, String... fieldvalues)
	{
		if (fieldvalues.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"HMSET", key, field, value}, fieldvalues));
		else
			writer.writeArray("HMSET", key, field, value);
		return ReturnType.OK.readFrom(reader);
	}

	/**
	 * <p>From <a href="http://redis.io/commands/hmset">http://redis.io/commands/hmset</a>:
	 * <p><strong>Available since 2.0.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) where N is the number of fields being set.
	 * <p>Sets the specified fields to their respective values in the hash stored 
	 * at <code>key</code>. This command overwrites any existing fields in the hash. 
	 * If <code>key</code> does not exist, a new key holding a hash is created.
	 * <p>Parameters should alternate between field, value, field, value ...
	 * @param key the key.
	 * @param field the first field.
	 * @param value the first value.
	 * @param fieldvalues the subsequent field-values for the hash.
	 * @return always true.
	 */
	public boolean hmset(String key, Object field, Object value, Object... fieldvalues)
	{
		if (fieldvalues.length > 0)
			writer.writeArray(Utils.joinArrays(new Object[]{"HMSET", key, field, value}, fieldvalues));
		else
			writer.writeArray("HMSET", key, field, value);
		return ReturnType.OK.readFrom(reader);
	}

	/**
	 * Like {@link #hmset(String, String, String, String...)}, except abstracted as {@link KeyValue}s of
	 * key-value pairs.
	 */
	@SuppressWarnings("unchecked")
	public boolean hmset(String key, KeyValue<String, Object>... pairs)
	{
		if (pairs.length == 0)
			return true;
		
		List<Object> out = new ArrayList<Object>(2 + (pairs.length * 2));
		out.add("HMSET");
		out.add(key);
		
		for (KeyValue<String, Object> p : pairs)
		{
			if (p.getValue() != null)
			{
				out.add(p.getKey());
				out.add(String.valueOf(p.getValue()));
			}
		}
		writer.writeArray(out);
		return ReturnType.OK.readFrom(reader);
	}

	/**
	 * Like {@link #hmset(String, String, String, String...)}, except abstracted as a map of
	 * key-value pairs.
	 * @param key the key.
	 * @param map the map of field-value pairs for the hash.
	 * @return true.
	 */
	public boolean hmsetMap(String key, Map<String, Object> map)
	{
		if (map.isEmpty())
			return true;
		
		List<Object> out = new ArrayList<>(2 + (map.size() * 2));
		out.add("HMSET");
		out.add(key);
		
		for (Map.Entry<String, Object> p : map.entrySet())
		{
			if (p.getValue() != null)
			{
				out.add(p.getKey());
				out.add(String.valueOf(p.getValue()));
			}
		}
		writer.writeArray(out);
		return ReturnType.OK.readFrom(reader);
	}

	/**
	 * Like {@link #hmset(String, String, String, String...)}, except each field or
	 * getter sets the fields and values. Fields/Getter Methods annotated with
	 * {@link RedisIgnore} are ignored.
	 * @param key the key.
	 * @param object the object to convert to a hash.
	 * @param <T> the object type.
	 * @return true.
	 */
	public <T> boolean hmsetObject(String key, T object)
	{
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>)object.getClass();
		Profile<T> profile = PROFILE_FACTORY.getProfile(type);
		
		List<Object> out = new ArrayList<Object>(2 + ((profile.getPublicFieldsByName().size() + profile.getSetterMethodsByName().size()) * 2));
		out.add("HMSET");
		out.add(key);

		for (Map.Entry<String, FieldInfo> entry : profile.getPublicFieldsByName().entrySet())
		{
			FieldInfo f = entry.getValue();
			String k = Utils.isNull(f.getAlias(), entry.getKey());
			Object v = Utils.getFieldValue(object, f.getField());
			if (v != null)
			{
				out.add(k);
				out.add(v);
			}
		}
		
		for (Map.Entry<String, MethodInfo> entry : profile.getGetterMethodsByName().entrySet())
		{
			MethodInfo m = entry.getValue();
			String k = Utils.isNull(m.getAlias(), entry.getKey());
			Object v = Utils.invokeBlind(m.getMethod(), object);
			if (v != null)
			{
				out.add(k);
				out.add(v);
			}
		}
		
		writer.writeArray(out);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean hset(String key, String field, String value)
	{
		writer.writeArray("HSET", key, field, value);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean hset(String key, String field, Number value)
	{
		writer.writeArray("HSET", key, field, value);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean hsetnx(String key, String field, String value)
	{
		writer.writeArray("HSETNX", key, field, value);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean hsetnx(String key, String field, Number value)
	{
		writer.writeArray("HSETNX", key, field, value);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public String[] hvals(String key)
	{
		writer.writeArray("HVALS", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public RedisCursor hscan(String key, long cursor)
	{
		return hscan(key, cursor, null, null);
	}
	
	@Override
	public RedisCursor hscan(String key, long cursor, String pattern)
	{
		return hscan(key, cursor, pattern, null);
	}
	
	@Override
	public RedisCursor hscan(String key, long cursor, long count)
	{
		return hscan(key, cursor, null, count);
	}
	
	@Override
	public RedisCursor hscan(String key, long cursor, String pattern, Long count)
	{
		if (pattern == null)
		{
			if (count == null)
				writer.writeArray("HSCAN", key, cursor);
			else
				writer.writeArray("HSCAN", key, cursor, "COUNT", count);
		}
		else
		{
			if (count == null)
				writer.writeArray("HSCAN", key, cursor, "MATCH", pattern);
			else
				writer.writeArray("HSCAN", key, cursor, "MATCH", pattern, "COUNT", count);
		}
		return RedisCursor.create(ReturnType.INTEGER.readFrom(reader), ReturnType.ARRAY.readFrom(reader));
	}

	@Override
	public boolean pfadd(String key, String element, String... elements)
	{
		if (elements.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"PFADD", key, element}, elements));
		else
			writer.writeArray("PFADD", key, element);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public long pfcount(String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"PFCOUNT",key}, keys));
		else
			writer.writeArray("PFCOUNT", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public boolean pfmerge(String destkey, String sourcekey, String... sourcekeys)
	{
		if (sourcekeys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"PFMERGE", destkey, sourcekey}, sourcekeys));
		else
			writer.writeArray("PFMERGE", destkey, sourcekey);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public KeyValue<String, String> blpop(long timeout, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"BLPOP", key}, keys, new Object[]{timeout}));
		else
			writer.writeArray("BLPOP", key, timeout);
		String[] resp = ReturnType.ARRAY.readFrom(reader);
		return resp != null ? new KeyValue<String, String>(resp[0], resp[1]) : null;
	}

	/**
	 * Like {@link #blpop(long, String, String...)}, except it casts the value to a long integer. 
	 * @param timeout the timeout in seconds.
	 * @param key the first key.
	 * @param keys the subsequent keys.
	 * @return an object pair consisting of popped list key and the numeric value popped, or null on timeout.
	 */
	public KeyValue<String, Long> blpopLong(long timeout, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"BLPOP", key}, keys, new Object[]{timeout}));
		else
			writer.writeArray("BLPOP", key, timeout);
		String[] resp = ReturnType.ARRAY.readFrom(reader);
		return resp != null ? new KeyValue<String, Long>(resp[0], Utils.parseLong(resp[1])) : null;
	}

	@Override
	public KeyValue<String, String> brpop(long timeout, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"BRPOP", key}, keys, new Object[]{timeout}));
		else
			writer.writeArray("BRPOP", key, timeout);
		return ReturnType.STRINGPAIR.readFrom(reader);
	}

	/**
	 * Like {@link #brpop(long, String, String...)}, except it casts the value to a long integer. 
	 * @param timeout the timeout in seconds.
	 * @param key the first key.
	 * @param keys the subsequent keys.
	 * @return an object pair consisting of popped list key and the numeric value popped, or null on timeout.
	 */
	public KeyValue<String, Long> brpopLong(long timeout, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"BRPOP", key}, keys, new Object[]{timeout}));
		else
			writer.writeArray("BRPOP", key, timeout);
		String[] resp = ReturnType.ARRAY.readFrom(reader);
		return resp != null ? new KeyValue<String, Long>(resp[0], Utils.parseLong(resp[1])) : null;
	}

	@Override
	public String brpoplpush(long timeout, String source, String destination)
	{
		writer.writeArray("BRPOPLPUSH", source, destination, timeout);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Like {@link #brpoplpush(long, String, String)}, except it casts the value to a long integer. 
	 * @param timeout the timeout in seconds.
	 * @param source the source key.
	 * @param destination the destination key.
	 * @return the value returned as a long.
	 */
	public Long brpoplpushLong(long timeout, String source, String destination)
	{
		String out = brpoplpush(timeout, source, destination);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public String lindex(String key, long index)
	{
		writer.writeArray("LINDEX", key, index);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Like {@link #lindex(String, long)}, except it casts the value to a long integer.
	 * @param key the key.
	 * @param index the list index.
	 * @return the value returned as a long.
	 */
	public Long lindexLong(String key, long index)
	{
		String out = lindex(key, index);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public long linsert(String key, boolean before, String pivot, String value)
	{
		writer.writeArray("LINSERT", key, before ? "BEFORE" : "AFTER", pivot, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long linsert(String key, boolean before, String pivot, Number value)
	{
		writer.writeArray("LINSERT", key, before ? "BEFORE" : "AFTER", pivot, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long llen(String key)
	{
		writer.writeArray("LLEN", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String lpop(String key)
	{
		writer.writeArray("LPOP", key);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Like {@link #lpop(String)}, except it casts the value to a long integer.
	 * @param key the key.
	 * @return the value returned as a long.
	 */
	public Long lpopLong(String key)
	{
		String out = lpop(key);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public long lpush(String key, String value, String... values)
	{
		if (values.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"LPUSH", key, value}, values));
		else
			writer.writeArray("LPUSH", key, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long lpushx(String key, String value)
	{
		writer.writeArray("LPUSHX", key, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String[] lrange(String key, long start, long stop)
	{
		writer.writeArray("LPUSHX", key, start, stop);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long lrem(String key, long count, String value)
	{
		writer.writeArray("LREM", key, count, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public boolean lset(String key, long index, String value)
	{
		writer.writeArray("LSET", key, index, value);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean ltrim(String key, long start, long stop)
	{
		writer.writeArray("LTRIM", key, start, stop);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public String rpop(String key)
	{
		writer.writeArray("RPOP", key);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Like {@link #rpop(String)}, except it casts the value to a long integer. 
	 * @param key the key.
	 * @return the value returned as a long.
	 */
	public Long rpopLong(String key)
	{
		String out = rpop(key);
		return out != null ? Utils.parseLong(out) : null;
	}
	
	@Override
	public String rpoplpush(String source, String destination)
	{
		writer.writeArray("RPOPLPUSH", source, destination);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Like {@link #rpoplpush(String, String)}, except it casts the value to a long integer. 
	 * @param source the source key.
	 * @param destination the destination key.
	 * @return the value returned as a long.
	 */
	public Long rpoplpushLong(String source, String destination)
	{
		String out = rpoplpush(source, destination);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public long rpush(String key, String value, String... values)
	{
		if (values.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"RPUSH", key, value}, values));
		else
			writer.writeArray("RPUSH", key, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long rpushx(String key, String value)
	{
		writer.writeArray("RPUSHX", key, value);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public RedisObject eval(String scriptContent, String[] keys, Object... args)
	{
		writer.writeArray(Utils.joinArrays(new Object[]{"EVAL", scriptContent, keys.length}, keys, args));
		return ReturnType.OBJECT.readFrom(reader);
	}

	@Override
	public RedisObject evalsha(String hash, String[] keys, Object... args)
	{
		writer.writeArray(Utils.joinArrays(new Object[]{"EVALSHA", hash, keys.length}, keys, args));
		return ReturnType.OBJECT.readFrom(reader);
	}

	@Override
	public boolean[] scriptExists(String scriptHash, String... scriptHashes)
	{
		if (scriptHashes.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SCRIPT", "EXISTS", scriptHash}, scriptHashes));
		else
			writer.writeArray("SCRIPT", "EXISTS", scriptHash);
		return ReturnType.BOOLEANARRAY.readFrom(reader);
	}

	@Override
	public boolean scriptFlush()
	{
		writer.writeArray("SCRIPT", "FLUSH");
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public boolean scriptKill(String hash)
	{
		writer.writeArray("SCRIPT", "KILL", hash);
		return ReturnType.OK.readFrom(reader);
	}

	@Override
	public String scriptLoad(String content)
	{
		writer.writeArray("SCRIPT", "LOAD", content);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * <p>From <a href="http://redis.io/commands/script-load">http://redis.io/commands/script-load</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the length in bytes of the script body.
	 * <p>Load a script into the scripts cache from the specified file without executing it. After the specified 
	 * command is loaded into the script cache it will be callable using {@link #evalsha(String, String[], Object...)} 
	 * with the correct SHA1 digest of the script, exactly like after the first successful invocation of {@link #eval(String, String[], Object...)}.
	 * @return the SHA1 digest of the script added into the script cache.
	 */
	public String scriptLoad(File content) throws IOException
	{
		return scriptLoad(Utils.getTextualContents(content));
	}

	/**
	 * <p>From <a href="http://redis.io/commands/script-load">http://redis.io/commands/script-load</a>:
	 * <p><strong>Available since 2.6.0.</strong>
	 * <p><strong>Time complexity:</strong> O(N) with N being the length in bytes of the script body.
	 * <p>Load a script into the scripts cache from the specified input stream (until the end is reached) without executing it. 
	 * The stream is not closed after read. After the specified command is loaded into the 
	 * script cache it will be callable using {@link #evalsha(String, String[], Object...)} 
	 * with the correct SHA1 digest of the script, exactly like after the first 
	 * successful invocation of {@link #eval(String, String[], Object...)}.
	 * @return the SHA1 digest of the script added into the script cache.
	 */
	public String scriptLoad(InputStream content) throws IOException
	{
		return scriptLoad(Utils.getTextualContents(content));
	}

	@Override
	public long sadd(String key, String member, String... members)
	{
		if (members.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SADD", key, member}, members));
		else
			writer.writeArray("SADD", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long sadd(String key, Object member, Object... members)
	{
		if (members.length > 0)
			writer.writeArray(Utils.joinArrays(new Object[]{"SADD", key, member}, members));
		else
			writer.writeArray("SADD", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long scard(String key)
	{
		writer.writeArray("SCARD", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String[] sdiff(String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SDIFF", key}, keys));
		else
			writer.writeArray("SDIFF", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long sdiffstore(String destination, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SDIFFSTORE", destination, key}, keys));
		else
			writer.writeArray("SDIFFSTORE", destination, key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String[] sinter(String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SINTER", key}, keys));
		else
			writer.writeArray("SINTER", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long sinterstore(String destination, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SINTERSTORE", destination, key}, keys));
		else
			writer.writeArray("SINTERSTORE", destination, key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public boolean sismember(String key, String member)
	{
		writer.writeArray("SISMEMBER", key, member);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public boolean sismember(String key, Number member)
	{
		writer.writeArray("SISMEMBER", key, member);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public String[] smembers(String key)
	{
		writer.writeArray("SMEMBERS", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public boolean smove(String source, String destination, String member)
	{
		writer.writeArray("SMOVE", source, destination, member);
		return ReturnType.BOOLEAN.readFrom(reader);
	}

	@Override
	public String spop(String key)
	{
		writer.writeArray("SPOP", key);
		return ReturnType.STRING.readFrom(reader);
	}

	/**
	 * Like {@link #spop(String)}, except it casts the value to a long integer. 
	 * @param key the key.
	 * @return the value returned as a long.
	 */
	public Long spopLong(String key)
	{
		String out = spop(key);
		return out != null ? Utils.parseLong(out) : null;
	}

	@Override
	public String srandmember(String key)
	{
		writer.writeArray("SRANDMEMBER", key);
		return ReturnType.STRING.readFrom(reader);
	}

	@Override
	public String[] srandmember(String key, long count)
	{
		writer.writeArray("SRANDMEMBER", key, count);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long srem(String key, String member, String... members)
	{
		if (members.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SREM", key, member}, members));
		else
			writer.writeArray("SREM", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long srem(String key, Object member, Object... members)
	{
		if (members.length > 0)
			writer.writeArray(Utils.joinArrays(new Object[]{"SREM", key, member}, members));
		else
			writer.writeArray("SREM", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String[] sunion(String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SUNION", key}, keys));
		else
			writer.writeArray("SUNION", key);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public long sunionstore(String destination, String key, String... keys)
	{
		if (keys.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"SUNIONSTORE", destination, key}, keys));
		else
			writer.writeArray("SUNIONSTORE", destination, key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public RedisCursor sscan(String key, long cursor)
	{
		return hscan(key, cursor, null, null);
	}
	
	@Override
	public RedisCursor sscan(String key, long cursor, String pattern)
	{
		return hscan(key, cursor, pattern, null);
	}
	
	@Override
	public RedisCursor sscan(String key, long cursor, long count)
	{
		return hscan(key, cursor, null, count);
	}
	
	@Override
	public RedisCursor sscan(String key, String cursor, String pattern, Long count)
	{
		if (pattern == null)
		{
			if (count == null)
				writer.writeArray("SSCAN", key, cursor);
			else
				writer.writeArray("SSCAN", key, cursor, "COUNT", count);
		}
		else
		{
			if (count == null)
				writer.writeArray("SSCAN", key, cursor, "MATCH", pattern);
			else
				writer.writeArray("SSCAN", key, cursor, "MATCH", pattern, "COUNT", count);
		}
		return RedisCursor.create(ReturnType.INTEGER.readFrom(reader), ReturnType.ARRAY.readFrom(reader));
	}

	@Override
	public long zadd(String key, double score, String member)
	{
		writer.writeArray("ZADD", key, score, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zadd(String key, double score, Number member)
	{
		writer.writeArray("ZADD", key, score, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	@SuppressWarnings("unchecked")
	public long zadd(String key, KeyValue<Double, String>... pairs)
	{
		List<Object> out = new ArrayList<Object>(2 + (pairs.length * 2));
		out.add("ZADD");
		out.add(key);
		
		for (KeyValue<Double, String> p : pairs)
		{
			out.add(p.getKey());
			out.add(p.getValue());
		}
		
		writer.writeArray(out);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zcard(String key)
	{
		writer.writeArray("ZCARD", key);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zcount(String key, String min, String max)
	{
		writer.writeArray("ZCOUNT", key, min, max);
		return ReturnType.INTEGER.readFrom(reader);
	}

	/**
	 * Like {@link #zcount(String, String, String)}, 
	 * except it accepts doubles for min and max, not strings.
	 */
	public long zcount(String key, double min, double max)
	{
		return zcount(key, specialDouble(min), specialDouble(max));
	}

	@Override
	public double zincrby(String key, double increment, String member)
	{
		writer.writeArray("ZINCRBY", key, increment, member);
		return ReturnType.DOUBLE.readFrom(reader);
	}

	@Override
	public String[] zrange(String key, long start, long stop, boolean withScores)
	{
		if (withScores)
			writer.writeArray("ZRANGE", key, start, stop, "WITHSCORES");
		else
			writer.writeArray("ZRANGE", key, start, stop);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public String[] zrangebyscore(String key, String min, String max, boolean withScores, Long limitOffset, Long limitCount)
	{
		List<Object> out = new ArrayList<Object>(8);
		out.add("ZRANGEBYSCORE");
		out.add(key);

		out.add(min);
		out.add(max);
		
		if (withScores)
			out.add("WITHSCORES");

		if (limitOffset != null && limitCount != null)
		{
			out.add("LIMIT");
			out.add(limitOffset);
			out.add(limitCount);
		}
		
		writer.writeArray(out);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public String[] zrangebyscore(String key, double min, double max, boolean withScores)
	{
		return zrangebyscore(key, specialDouble(min), specialDouble(max), withScores, null, null);
	}

	@Override
	public String[] zrangebyscore(String key, String min, String max, boolean withScores)
	{
		return zrangebyscore(key, min, max, withScores, null, null);
	}

	@Override
	public String[] zrangebyscore(String key, double min, double max, boolean withScores, Long limitOffset, Long limitCount)
	{
		return zrangebyscore(key, specialDouble(min), specialDouble(max), withScores, limitOffset, limitCount);
	}

	@Override
	public Long zrank(String key, String member)
	{
		writer.writeArray("ZRANK", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public Long zrank(String key, Number member)
	{
		writer.writeArray("ZRANK", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zrem(String key, String member, String... members)
	{
		if (members.length > 0)
			writer.writeArray(Utils.joinArrays(new String[]{"ZREM", key, member}, members));
		else
			writer.writeArray("ZREM", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zrem(String key, Number member, Number... members)
	{
		if (members.length > 0)
			writer.writeArray(Utils.joinArrays(new Object[]{"ZREM", key, member}, members));
		else
			writer.writeArray("ZREM", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zremrangebyrank(String key, long start, long stop)
	{
		writer.writeArray("ZREMRANGEBYRANK", key, start, stop);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zremrangebyscore(String key, String min, String max)
	{
		writer.writeArray("ZREMRANGEBYSCORE", key, min, max);
		return ReturnType.INTEGER.readFrom(reader);
	}

	/**
	 * Like {@link #zremrangebyscore(String, String, String)},
	 * except it accepts doubles for min and max, not strings.
	 */
	public long zremrangebyscore(String key, double min, double max)
	{
		return zremrangebyscore(key, specialDouble(min), specialDouble(max));
	}

	@Override
	public Long zrevrank(String key, String member)
	{
		writer.writeArray("ZREVRANK", key, member);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public String[] zrevrange(String key, long start, long stop, boolean withScores)
	{
		if (withScores)
			writer.writeArray("ZREVRANGE", key, start, stop, "WITHSCORES");
		else
			writer.writeArray("ZREVRANGE", key, start, stop);
		return ReturnType.ARRAY.readFrom(reader);
	}

	@Override
	public String[] zrevrangebyscore(String key, String min, String max, boolean withScores, Long limitOffset, Long limitCount)
	{
		List<Object> out = new ArrayList<Object>(8);
		out.add("ZREVRANGEBYSCORE");
		out.add(key);

		out.add(min);
		out.add(max);
		
		if (withScores)
			out.add("WITHSCORES");

		if (limitOffset != null && limitCount != null)
		{
			out.add("LIMIT");
			out.add(limitOffset);
			out.add(limitCount);
		}
		
		writer.writeArray(out);
		return ReturnType.ARRAY.readFrom(reader);
	}
	
	@Override
	public String[] zrevrangebyscore(String key, double min, double max, boolean withScores)
	{
		return zrevrangebyscore(key, specialDouble(min), specialDouble(max), withScores, null, null);
	}

	@Override
	public String[] zrevrangebyscore(String key, String min, String max, boolean withScores)
	{
		return zrevrangebyscore(key, min, max, withScores, null, null);
	}

	@Override
	public String[] zrevrangebyscore(String key, double min, double max, boolean withScores, Long limitOffset, Long limitCount)
	{
		return zrevrangebyscore(key, specialDouble(min), specialDouble(max), withScores, limitOffset, limitCount);
	}

	@Override
	public Double zscore(String key, String member)
	{
		writer.writeArray("ZSCORE", key, member);
		return ReturnType.DOUBLE.readFrom(reader);
	}

	@Override
	public long zinterstore(String destination, double[] weights, Aggregation aggregation, String key, String... keys)
	{
		List<Object> out = new ArrayList<Object>(6 + keys.length + (weights != null ? weights.length : 0));
		out.add("ZINTERSTORE");
		out.add(destination);

		out.add(keys.length + 1);
		out.add(key);
		for (String k : keys)
			out.add(k);
		
		if (weights.length > 0)
		{
			out.add("WEIGHTS");
			for (double w : weights)
				out.add(w);
		}
		
		if (aggregation != null)
			out.add(aggregation.name());
		
		writer.writeArray(out);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zinterstore(String destination, Aggregation aggregation, String key, String... keys)
	{
		return zinterstore(destination, null, aggregation, key, keys);
	}

	@Override
	public long zinterstore(String destination, double[] weights, String key, String... keys)
	{
		return zinterstore(destination, weights, null, key, keys);
	}

	@Override
	public long zinterstore(String destination, String key, String... keys)
	{
		return zinterstore(destination, null, null, key, keys);
	}
	
	@Override
	public long zunionstore(String destination, double[] weights, Aggregation aggregation, String key, String... keys)
	{
		List<Object> out = new ArrayList<Object>(6 + keys.length + (weights != null ? weights.length : 0));
		out.add("ZUNIONSTORE");
		out.add(destination);

		out.add(keys.length + 1);
		out.add(key);
		for (String k : keys)
			out.add(k);
		
		if (weights.length > 0)
		{
			out.add("WEIGHTS");
			for (double w : weights)
				out.add(w);
		}
		
		if (aggregation != null)
			out.add(aggregation.name());
		
		writer.writeArray(out);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zunionstore(String destination, Aggregation aggregation, String key, String... keys)
	{
		return zunionstore(destination, null, aggregation, key, keys);
	}

	@Override
	public long zunionstore(String destination, double[] weights, String key, String... keys)
	{
		return zunionstore(destination, weights, null, key, keys);
	}

	@Override
	public long zunionstore(String destination, String key, String... keys)
	{
		return zunionstore(destination, null, null, key, keys);
	}
	
	@Override
	public long zlexcount(String key, String min, String max)
	{
		writer.writeArray("ZLEXCOUNT", key, min, max);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zlexcount(String key, double min, double max)
	{
		return zlexcount(key, specialDouble(min), specialDouble(max));
	}

	@Override
	public long zrangebylex(String key, String min, String max, Long limitOffset, Long limitCount)
	{
		List<Object> out = new ArrayList<Object>(8);
		out.add("ZRANGEBYLEX");
		out.add(key);

		out.add(min);
		out.add(max);
		
		if (limitOffset != null && limitCount != null)
		{
			out.add("LIMIT");
			out.add(limitOffset);
			out.add(limitCount);
		}
		
		writer.writeArray(out);
		return ReturnType.INTEGER.readFrom(reader);
	}

	/**
	 * Like {@link #zrangebylex(String, String, String, Long, Long)},
	 * except it accepts doubles for min and max, not strings.
	 */
	public long zrangebylex(String key, double min, double max, Long limitOffset, Long limitCount)
	{
		return zrangebylex(key, specialDouble(min), specialDouble(max), limitOffset, limitCount);
	}

	/**
	 * Like {@link #zrangebylex(String, String, String, Long, Long)}, with no limit.
	 */
	public long zrangebylex(String key, String min, String max)
	{
		return zrangebylex(key, min, max, null, null);
	}

	/**
	 * Like {@link #zrangebylex(String, String, String)},
	 * except it accepts doubles for min and max, not strings, with no limit.
	 */
	public long zrangebylex(String key, double min, double max)
	{
		return zrangebylex(key, specialDouble(min), specialDouble(max), null, null);
	}

	@Override
	public long zremrangebylex(String key, String min, String max)
	{
		writer.writeArray("ZREMRANGEBYLEX", key, min, max);
		return ReturnType.INTEGER.readFrom(reader);
	}

	@Override
	public long zremrangebylex(String key, double min, double max)
	{
		return zremrangebylex(key, specialDouble(min), specialDouble(max));
	}

	@Override
	public RedisCursor zscan(String key, long cursor)
	{
		return zscan(key, cursor, null, null);
	}

	@Override
	public RedisCursor zscan(String key, long cursor, String pattern)
	{
		return zscan(key, cursor, pattern, null);
	}

	@Override
	public RedisCursor zscan(String key, long cursor, long count)
	{
		return zscan(key, cursor, null, count);
	}
	
	@Override
	public RedisCursor zscan(String key, long cursor, String pattern, Long count)
	{
		if (pattern == null)
		{
			if (count == null)
				writer.writeArray("ZSCAN", key, cursor);
			else
				writer.writeArray("ZSCAN", key, cursor, "COUNT", count);
		}
		else
		{
			if (count == null)
				writer.writeArray("ZSCAN", key, cursor, "MATCH", pattern);
			else
				writer.writeArray("ZSCAN", key, cursor, "MATCH", pattern, "COUNT", count);
		}
		return RedisCursor.create(ReturnType.INTEGER.readFrom(reader), ReturnType.ARRAY.readFrom(reader));
	}

	/**
	 * Converts some of Java's primitive doubles to Redis interval values.
	 * <ul>
	 * <li>{@link Double#NEGATIVE_INFINITY} converts to "-".</li>
	 * <li>{@link Double#POSITIVE_INFINITY} converts to "+".</li>
	 * <li>All others convert to their string representation.</li>
	 * </ul>
	 * @param d the input double.
	 * @return the string representation of the value.
	 */
	protected String specialDouble(double d)
	{
		if (d == Double.POSITIVE_INFINITY)
			return "+";
		else if (d == Double.NEGATIVE_INFINITY)
			return "-";
		else
			return String.valueOf(d);
	}

}
