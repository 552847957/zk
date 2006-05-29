/* CommonFns.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Apr 20 18:35:21     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.el.fn;

import java.util.Collection;
import java.util.Map;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.potix.lang.Classes;
import com.potix.mesg.Messages;
import com.potix.util.resource.Labels;
import com.potix.util.logging.Log;

/**
 * Functions used with EL.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class CommonFns {
	private static final Log log = Log.lookup(CommonFns.class);

	protected CommonFns() {}

	/** Converts the specified object to a boolean.
	 */
	public static boolean toBoolean(Object val) {
		return ((Boolean)Classes.coerce(boolean.class, val)).booleanValue();
	}
	/** Converts the specified object to a string.
	 */
	public static String toString(Object val) {
		return (String)Classes.coerce(String.class, val);
	}
	/** Converts the specified object to a number.
	 */
	public static Number toNumber(Object val) {
		return (Number)Classes.coerce(Number.class, val);
	}
	/** Converts the specified object to an integer.
	 */
	public static int toInt(Object val) {
		return ((Integer)Classes.coerce(int.class, val)).intValue();
	}
	/** Converts the specified object to a (big) decimal.
	 */
	public static BigDecimal toDecimal(Object val) {
		return (BigDecimal)Classes.coerce(BigDecimal.class, val);
	}
	/** Converts the specified object to an character.
	 */
	public static char toChar(Object val) {
		return ((Character)Classes.coerce(char.class, val)).charValue();
	}
	/** Tests whehter an object, o, is an instance of a class, c.
	 */
	public static boolean isInstance(Object c, Object o) {
		if (c instanceof Class) {
			return ((Class)c).isInstance(o);
		} else if (c instanceof String) {
			try {
				return Classes.forNameByThread((String)c).isInstance(o);
			} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Class not found: "+c);
			}
		} else {
			throw new IllegalArgumentException("Unknown class: "+c);
		}
	}

	/** Returns the label or message of the specified key.
	 * <ul>
	 * <li>If key is "class:x", Labels.getClassLabel("x") is called</li>
	 * <li>If key is "class:x:y", Labels.getFieldLabel("x", "y") is called</li>
	 * <li>If key is "mesg:class:MMM", Messages.get(class.MMM) is called</li>
	 * <li>Otherwise, Labels.getProperty is called.
	 * </ul>
	 */
	public static final String getLabel(String key) {
		if (key == null)
			return "";

		if (key.startsWith("class:")) {
			final int j = key.indexOf(':', 6);
			final String clsnm = j >= 0 ? key.substring(6, j): key.substring(6);
			try {
				final Class cls = Classes.forNameByThread(clsnm);
				return j >= 0 ?
					Labels.getFieldLabel(cls, key.substring(j + 1)):
					Labels.getClassLabel(cls);
			} catch (ClassNotFoundException ex) {
				log.warning("Class not found: "+clsnm, ex);
			}
		} else if (key.startsWith("mesg:")) {
			final int j = key.lastIndexOf(':');
			if (j > 5) {
				final String clsnm = key.substring(5, j);
				final String fldnm = key.substring(j + 1);
				try {
					final Class cls = Classes.forNameByThread(clsnm);
					final Field fld = cls.getField(fldnm);
					return Messages.get(((Integer)fld.get(null)).intValue());
				} catch (ClassNotFoundException ex) {
					log.warning("Class not found: "+clsnm, ex);
				} catch (NoSuchFieldException ex) {
					log.warning("Field not found: "+fldnm, ex);
				} catch (IllegalAccessException ex) {
					log.warning("Field not accessible: "+fldnm, ex);
				}
			} else if (log.debugable()) {
				log.debug("Not a valid format: "+key);
			}
		}
		return Labels.getProperty(key);
	}
	/** Returns the length of an array, string, collection or map.
	 */
	public static final int length(Object o) {
		if (o instanceof String) {
			return ((String)o).length();
		} else if (o == null) {
			return 0;
		} else if (o instanceof Collection) {
			return ((Collection)o).size();
		} else if (o instanceof Map) {
			return ((Map)o).size();
		} else if (o instanceof Object[]) {
			return ((Object[])o).length;
		} else if (o instanceof int[]) {
			return ((int[])o).length;
		} else if (o instanceof long[]) {
			return ((long[])o).length;
		} else if (o instanceof short[]) {
			return ((short[])o).length;
		} else if (o instanceof byte[]) {
			return ((byte[])o).length;
		} else if (o instanceof char[]) {
			return ((char[])o).length;
		} else if (o instanceof double[]) {
			return ((double[])o).length;
		} else if (o instanceof float[]) {
			return ((float[])o).length;
		} else {
			throw new IllegalArgumentException("Unknown object for length: "+o.getClass());
		}
	}

	/** Instantiates the specified class.
	 */
	public static final Object new_(Object o) throws Exception {
		if (o instanceof String) {
			return Classes.newInstanceByThread((String)o);
		} else if (o instanceof Class) {
			return ((Class)o).newInstance();
		} else {
			throw new IllegalArgumentException("Unknow object for new: "+o);
		}
	}
}
