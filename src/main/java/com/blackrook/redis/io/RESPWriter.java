/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;

import com.blackrook.redis.data.RedisObject;

/**
 * Writer class for writing requests to a Redis Socket. 
 * @author Matthew Tropiano
 */
public class RESPWriter implements Closeable
{
	/** Endline. */
	private static final String CRLF = "\r\n";
	
	/** The wrapped writer. */
	private PrintWriter out;
	
	/**
	 * Opens a RedisWriter attached to an output stream. 
	 * @param out the {@link OutputStream} to use.
	 */
	public RESPWriter(OutputStream out)
	{
		try {
			this.out = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), false);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Opens a RedisWriter attached to a writer. 
	 * @param out the {@link Writer} to use.
	 */
	public RESPWriter(Writer out)
	{
		this.out = new PrintWriter(out, false);
	}

	@Override
	public void close() throws IOException
	{
		out.close();
	}
	
	/**
	 * Writes a null object.
	 */
	public void writeNull()
	{
		writeNull(true);
	}

	/**
	 * Writes a null object.
	 */
	public void writeNullArray()
	{
		out.write("*-1" + CRLF);
		out.flush();
	}

	/**
	 * Writes a number to output.
	 * @param n the number to write.
	 */
	public void writeNumber(Number n)
	{
		writeNumber(n, true);
	}

	/**
	 * Writes a simple string to output.
	 * @param s the string to write.
	 */
	public void writeSimpleString(String s)
	{
		writeSimpleString(s, true);
	}

	/**
	 * Writes a bulk string to output.
	 * @param s the string to write.
	 */
	public void writeBulkString(String s)
	{
		writeBulkString(s, true);
	}

	/**
	 * Writes a bulk string array.
	 * @param iterable the objects to write.
	 */
	public void writeArray(Iterable<Object> iterable)
	{
		int len = 0;
		StringBuilder sb = new StringBuilder();
		Iterator<Object> it = iterable.iterator();
		while (it.hasNext())
		{
			Object obj = it.next();
			String s = obj != null ? String.valueOf(obj) : null;
			if (s == null)
				sb.append("$-1").append(CRLF);
			else
			{
				sb.append("$").append(s.length()).append(CRLF);
				sb.append(s).append(CRLF);
			}
			len++;
		}
			
		out.write("*" + len + CRLF);
		out.write(sb.toString());
		out.flush();
	}

	/**
	 * Writes a bulk string array.
	 * @param objects the objects to write.
	 */
	public void writeArray(Object ...objects)
	{
		out.write("*" + objects.length + CRLF);
		for (Object obj : objects)
			writeBulkString(obj != null ? String.valueOf(obj) : null, true);
		out.flush();
	}

	/**
	 * Writes a bulk string array.
	 * @param strings the series of strings.
	 */
	public void writeArray(String ...strings)
	{
		out.write("*" + strings.length + CRLF);
		for (String s : strings)
			writeBulkString(s, true);
		out.flush();
	}
	
	/**
	 * Writes a full object that represents a Redis request.
	 * @param object the Redis object to write.
	 */
	public void writeObject(RedisObject object)
	{
		out.write(object.asRaw(true));
		out.flush();
	}
	
	/**
	 * Writes a character to output.
	 * @param c the character.
	 */
	public void writeChar(char c)
	{
		writeSimpleString(String.valueOf(c), true);
	}

	/**
	 * Writes an error to output.
	 * @param s the error string to write.
	 */
	public void writeError(String s)
	{
		out.write("-" + s + CRLF);
		out.flush();
	}

	/**
	 * Writes a Redis command as though it will be executed from a REPL-like command prompt.
	 * The full command is sent to the server as a raw request.
	 * @param commandString the command to send to the server.
	 */
	public void writeCommand(String commandString)
	{
		writeArray(parseCommand(commandString));
	}

	/**
	 * Writes raw content into this writer and flushes it.
	 * @param rawcontent the content to send.
	 */
	public void writeRaw(String rawcontent)
	{
		out.write(rawcontent);
		out.flush();
	}
	
	/**
	 * Writes a null object.
	 * @param flush if true, flushes the stream.
	 */
	protected void writeNull(boolean flush)
	{
		out.write("$-1" + CRLF);
		if (flush) 
			out.flush();
	}

	/**
	 * Writes a number to output.
	 * @param n the number to write.
	 * @param flush if true, flushes the stream.
	 */
	protected void writeNumber(Number n, boolean flush)
	{
		if (n == null)
			writeNull(flush);
		if (n instanceof BigDecimal)
			writeSimpleString(String.valueOf(n), flush);
		else if (n instanceof Double || n instanceof Float)
			writeSimpleString(String.valueOf(n), flush);
		else
		{
			out.write(":" + String.valueOf(n) + CRLF);
		}
		if (flush) 
			out.flush();
	}

	/**
	 * Writes a bulk string to output.
	 * @param s the string to write.
	 * @param flush if true, flushes the stream.
	 */
	protected void writeSimpleString(String s, boolean flush)
	{
		out.write("+" + s + CRLF);
		if (flush) 
			out.flush();
	}

	/**
	 * Writes a bulk string.
	 * @param s the string to write.
	 * @param flush if true, flushes the stream.
	 */
	protected void writeBulkString(String s, boolean flush)
	{
		if (s == null)
			out.write("$-1" + CRLF);
		else
		{
			out.write("$" + s.length() + CRLF);
			out.write(s + CRLF);
		}
		if (flush) 
			out.flush();
	}

	// Parses a command line into tokens.
	private static String[] parseCommand(String input)
	{
		final int STATE_INIT = 0;
		final int STATE_TEXT = 1;
		final int STATE_QUOTED_TEXT = 2;
		int state = STATE_INIT;
		
		LinkedList<String> list = new LinkedList<>();
		StringBuilder token = new StringBuilder();
		
		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			switch (state)
			{
				case STATE_INIT:
				{
					if (!Character.isWhitespace(c))
					{
						if (c == '"')
						{
							state = STATE_QUOTED_TEXT;
						}
						else
						{
							token.append(c);
							state = STATE_TEXT;
						}
					}
				}
				break;

				case STATE_TEXT:
				{
					if (c == '"')
					{
						list.add(token.toString());
						token.delete(0, token.length());
						state = STATE_QUOTED_TEXT;
					}
					else if (Character.isWhitespace(c))
					{
						list.add(token.toString());
						token.delete(0, token.length());
						state = STATE_INIT;
					}
					else
					{
						token.append(c);
					}
				}
				break;
				
				case STATE_QUOTED_TEXT:
				{
					if (c == '"')
					{
						list.add(token.toString());
						token.delete(0, token.length());
						state = STATE_INIT;
					}
					else
					{
						token.append(c);
					}
				}
				break;
			}
		}
		
		String[] cmd = new String[list.size()];
		list.toArray(cmd);
		return cmd;
	}
	
}
