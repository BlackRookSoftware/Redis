/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.enums;

/**
 * List of data encoding types.
 * @author Matthew Tropiano
 */
public enum EncodingType
{
	RAW,
	INTEGER,
	ZIPLIST,
	LINKEDLIST,
	INTSET,
	HASHTABLE,
	ZIPMAP,
	SKIPLIST;
}
