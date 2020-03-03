/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.redis.enums;

/**
 * Aggregate function for union/intersection queries.
 * @author Matthew Tropiano
 */
public enum Aggregation
{
	SUM,
	MIN,
	MAX;
}
