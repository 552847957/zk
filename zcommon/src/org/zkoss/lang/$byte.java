/* $byte.java

	Purpose:
		
	Description:
		
	History:
		Wed Nov 12 14:41:57     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
package org.zkoss.lang;

/**
 * Represents a primitive byte.
 * It is usually if you want to distinquish byte and Byte.
 * @author tomyeh
 * @since 5.0.0
 */
public class $byte implements java.io.Serializable, $primitive {
	public final byte value;
	public $byte(byte value) {
		this.value = value;
	}
	public int hashCode() {
		return this.value;
	}
	public String toString() {
		return Byte.toString(this.value);
	}
	public boolean equals(Object o) {
		return (o instanceof $byte) && (($byte)o).value == this.value;
	}
	public Class getType() {
		return Byte.TYPE;
	}
}
