/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.enums;

/**
 * The bitwise operation to perform on a BITOP call.
 * @author Matthew Tropiano
 */
public enum BitwiseOperation
{
	AND,
	OR,
	XOR,
	NOT;
}