/* ComponentDefinitionMap.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Sep  4 20:20:36     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.metainfo;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A map of component definitions.
 * Used with {@link PageDefinition#getComponentDefinitionMap}.
 *
 * @author tomyeh
 */
public class ComponentDefinitionMap
implements Cloneable, java.io.Serializable {
	/** A map of component definition defined in this page. */
	private transient Map _compdefs;
	/** Map(String clsnm, ComponentDefinition compdef). */
	private transient Map _compdefsByClass;
	/** Whether the element name is case-insensitive. */
	private final boolean _ignoreCase;

	/** Constructor.
	 */
	public ComponentDefinitionMap(boolean ignoreCase) {
		_ignoreCase = ignoreCase;
	}

	/** Returns whether the component names are case-insensitive.
	 */
	public boolean isCaseInsensitive() {
		return _ignoreCase;
	}


	/** Returns a collection of component definitions, {@link ComponentDefinition},
	 *  defined in this map.
	 */
	public Collection getNames() {
		return _compdefs != null ?
			_compdefs.keySet(): (Collection)Collections.EMPTY_LIST;
	}
	/** Adds a component definition to this map.
	 *
	 * <p>Thread safe.
	 */
	public void add(ComponentDefinition compdef) {
		if (compdef == null)
			throw new IllegalArgumentException("null");

		String name = compdef.getName();
		if (isCaseInsensitive())
			name = name.toLowerCase();

		Object implcls = compdef.getImplementationClass();
		if (implcls instanceof Class)
			implcls = ((Class)implcls).getName();

		synchronized (this) {
			if (_compdefs == null) {
				_compdefsByClass =
					Collections.synchronizedMap(new HashMap(3));
				_compdefs =
					Collections.synchronizedMap(new HashMap(3));
			}

			_compdefs.put(name, compdef);
			_compdefsByClass.put(implcls, compdef);
		}
	}
	/** Returns whether the specified component exists.
	 */
	public boolean contains(String name) {
		return _compdefs != null
			&& _compdefs.containsKey(
				isCaseInsensitive() ? name.toLowerCase(): name);
	}

	/** Returns the component definition of the specified name, or null if not
	 * not found.
	 *
	 * <p>Note: unlike {@link LanguageDefinition#getComponentDefinition},
	 * this method doesn't throw ComponentNotFoundException if not found.
	 * It just returns null.
	 */
	public ComponentDefinition get(String name) {
		return _compdefs != null ?
			(ComponentDefinition)_compdefs.get(
				isCaseInsensitive() ? name.toLowerCase(): name):
			null;
	}
	/** Returns the component definition of the specified class, or null if not
	 * found.
	 *
	 * <p>Note: unlike {@link LanguageDefinition#getComponentDefinition},
	 * this method doesn't throw ComponentNotFoundException if not found.
	 * It just returns null.
	 */
	public ComponentDefinition get(Class cls) {
		if (_compdefsByClass != null) {
			for (; cls != null; cls = cls.getSuperclass()) {
				final ComponentDefinition compdef =
					(ComponentDefinition)_compdefsByClass.get(cls.getName());
				if (compdef != null)
					return compdef;
			}
		}
		return null;
	}

	//Serializable//
	//NOTE: they must be declared as private
	private synchronized void writeObject(java.io.ObjectOutputStream s)
	throws java.io.IOException {
		s.defaultWriteObject();

		s.writeObject(_compdefs != null ? _compdefs.values(): null);
	}
	private synchronized void readObject(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		final Collection c = (Collection)s.readObject();
		if (c != null)
			for (Iterator it = c.iterator(); it.hasNext();)
				add((ComponentDefinition)it.next());
	}

	//Cloneable//
	public Object clone() {
		final ComponentDefinitionMap clone;
		try {
			clone = (ComponentDefinitionMap)super.clone();
			clone._compdefs = new HashMap(_compdefs);
			clone._compdefsByClass = new HashMap(_compdefsByClass);
		} catch (CloneNotSupportedException ex) {
			throw new InternalError();
		}
		return clone;
	}
}
