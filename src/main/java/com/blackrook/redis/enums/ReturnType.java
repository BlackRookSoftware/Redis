/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.enums;

import com.blackrook.redis.data.KeyValue;
import com.blackrook.redis.data.RedisObject;
import com.blackrook.redis.io.RESPReader;
import com.blackrook.redis.struct.Utils;

/**
 * Enumeration of Redis return types, used for figuring out what to return sometimes.
 * Necessary for pipelining commands.
 * @author Matthew Tropiano
 * @param <C> the type to parse.
 */
public class ReturnType<C extends Object>
{
	// Private Constructor.
	private ReturnType() {}

	/** OK return type. */
	public static final ReturnType<Boolean> OK = new ReturnType<Boolean>()
	{
		public Boolean readFrom(RESPReader reader)
		{
			return reader.readOK();
		}
	};
	
	/** PONG return type. */
	public static final ReturnType<Boolean> PONG = new ReturnType<Boolean>()
	{
		public Boolean readFrom(RESPReader reader)
		{
			return reader.readPong();
		}
	};
	
	/** BOOLEAN return type. */
	public static final ReturnType<Boolean> BOOLEAN = new ReturnType<Boolean>()
	{
		public Boolean readFrom(RESPReader reader)
		{
			return reader.readInteger() != 0;
		}
	};
	
	/** BOOLEANARRAY return type. */
	public static final ReturnType<boolean[]> BOOLEANARRAY = new ReturnType<boolean[]>()
	{
		public boolean[] readFrom(RESPReader reader)
		{
			String[] ret = ReturnType.ARRAY.readFrom(reader);
			boolean[] out = new boolean[ret.length];
			for (int i = 0; i < ret.length; i++)
				out[i] = Utils.parseLong(ret[i]) != 0;
			return out;
		}
	};
	
	/** INTEGER return type. */
	public static final ReturnType<Long> INTEGER = new ReturnType<Long>()
	{
		public Long readFrom(RESPReader reader)
		{
			return reader.readInteger();
		}
	};
	
	/** DOUBLE return type. */
	public static final ReturnType<Double> DOUBLE = new ReturnType<Double>()
	{
		public Double readFrom(RESPReader reader)
		{
			String out = reader.readString();
			return out != null ? Utils.parseDouble(out) : null;
		}
	};
	
	/** STRING return type. */
	public static final ReturnType<String> STRING = new ReturnType<String>()
	{
		public String readFrom(RESPReader reader)
		{
			return reader.readString();
		}
	};
	
	/** ARRAY return type. */
	public static final ReturnType<String[]> ARRAY = new ReturnType<String[]>()
	{
		public String[] readFrom(RESPReader reader)
		{
			return reader.readArray();
		}
	};
	
	/** DATATYPE return type. */
	public static final ReturnType<DataType> DATATYPE = new ReturnType<DataType>()
	{
		public DataType readFrom(RESPReader reader)
		{
			return DataType.valueOf(reader.readString().toUpperCase());
		}
	};
	
	/** STRINGPAIR return type. */
	public static final ReturnType<KeyValue<String, String>> STRINGPAIR = new ReturnType<KeyValue<String, String>>()
	{
		public KeyValue<String, String> readFrom(RESPReader reader)
		{
			String[] resp = reader.readArray();
			return resp != null ? new KeyValue<String, String>(resp[0], resp[1]) : null;
		}
	};
	
	/** ENCODING return type. */
	public static final ReturnType<EncodingType> ENCODING = new ReturnType<EncodingType>()
	{
		public EncodingType readFrom(RESPReader reader)
		{
			return EncodingType.valueOf(reader.readString().toUpperCase());
		}
	};
	
	/** OBJECT return type. */
	public static final ReturnType<RedisObject> OBJECT = new ReturnType<RedisObject>()
	{
		public RedisObject readFrom(RESPReader reader)
		{
			return reader.readObject();
		}
	};
	
	public C readFrom(RESPReader reader)
	{
		throw new IllegalArgumentException("I don't think that you will be able to call this.");
	}
	
}
