/* BSHInterpreter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun  1 14:28:43     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.scripting.bsh;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

import bsh.BshClassManager;
import bsh.NameSpace;
import bsh.BshMethod;
import bsh.Variable;
import bsh.Primitive;
import bsh.EvalError;
import bsh.UtilEvalError;

import org.zkoss.lang.Classes;
import org.zkoss.xel.Function;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.scripting.Namespace;
import org.zkoss.zk.scripting.NamespaceChangeListener;
import org.zkoss.zk.scripting.util.GenericInterpreter;
import org.zkoss.zk.scripting.SerializableAware;
import org.zkoss.zk.scripting.HierachicalAware;

/**
 * The interpreter that uses BeanShell to interpret zscript codes.
 *
 * <p>Unlike many other implementations, it supports the hierachical
 * scopes ({@link HierachicalAware}).
 * That is, it uses an independent BeanShell NameSpace
 * (aka. interpreter's scope) to store the variables/classes/methods
 * defined in BeanShell script for each ZK namespace ({@link Namespace}).
 * Since one-to-one relationship between BeanShell's scope and ZK namespace,
 * the invocation of BeanShell methods can execute correctly without knowing
 * what namespace it is.
 * However, if you want your codes portable across different interpreters,
 * you had better to call
 * {@link org.zkoss.zk.scripting.Namespaces#beforeInterpret}
 * to prepare the proper namespace, before calling any method defined in
 * zscript.
 *
 * @author tomyeh
 */
public class BSHInterpreter extends GenericInterpreter
implements SerializableAware, HierachicalAware {
	/** A variable of {@link Namespace}. The value is an instance of
	 * BeanShell's NameSpace.
	 */
	private static final String VAR_NS = "z_bshnS";
	private bsh.Interpreter _ip;
	private GlobalNS _bshns;

	public BSHInterpreter() {
	}

	//GenericInterpreter//
	protected void exec(String script) {
		try {
			final Namespace ns = getCurrent();
			if (ns != null) _ip.eval(script, prepareNS(ns));
			else _ip.eval(script); //unlikely (but just in case)
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}

	protected boolean contains(String name) {
		try {
			return _ip.getNameSpace().getVariable(name) != Primitive.VOID;
				//Primitive.VOID means not defined
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	protected Object get(String name) {
		try {
			return Primitive.unwrap(_ip.get(name));
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	protected void set(String name, Object val) {
		try {
			_ip.set(name, val);
				//unlike NameSpace.setVariable, _ip.set() handles null
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	protected void unset(String name) {
		try {
			_ip.unset(name);
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}

	protected boolean contains(Namespace ns, String name) {
		if (ns != null) {
			final NameSpace bshns = prepareNS(ns);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		try {
			 		return bshns.getVariable(name) != Primitive.VOID;
				} catch (UtilEvalError ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		return contains(name);
	}
	protected Object get(Namespace ns, String name) {
		if (ns != null) {
			final NameSpace bshns = prepareNS(ns);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		try {
			 		return Primitive.unwrap(bshns.getVariable(name));
				} catch (UtilEvalError ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		return get(name);
	}
	protected void set(Namespace ns, String name, Object val) {
		if (ns != null) {
			final NameSpace bshns = prepareNS(ns);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		try {
			 		bshns.setVariable(
			 			name, val != null ? val: Primitive.NULL, false);
		 			return;
				} catch (UtilEvalError ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		set(name, val);
	}
	protected void unset(Namespace ns, String name) {
		if (ns != null) {
			final NameSpace bshns = prepareNS(ns);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		bshns.unsetVariable(name);
	 			return;
			}
		}
		unset(name);
	}

	//-- Interpreter --//
	public void init(Page owner, String zslang) {
		super.init(owner, zslang);

		_ip = new bsh.Interpreter();
		_ip.setClassLoader(Thread.currentThread().getContextClassLoader());

		_bshns = new GlobalNS(_ip.getClassManager(), "global");
		_ip.setNameSpace(_bshns);
	}
	public void destroy() {
		getOwner().getNamespace().unsetVariable(VAR_NS, false);
		_ip = null;
		_bshns = null;
		super.destroy();
	}

	public Class getClass(String clsnm) {
		try {
			return _bshns.getClass(clsnm);
		} catch (UtilEvalError ex) {
			throw new UiException("Failed to load class "+clsnm, ex);
		}
	}
	public Function getFunction(String name, Class[] argTypes) {
		return getFunction0(_bshns, name, argTypes);
	}
	public Function getFunction(Namespace ns, String name, Class[] argTypes) {
		return getFunction0(prepareNS(ns), name, argTypes);
	}
	private Function getFunction0(NameSpace bshns, String name, Class[] argTypes) {
		try {
		 	final BshMethod m = bshns.getMethod(
		 		name, argTypes != null ? argTypes: new Class[0], false);
		 	return m != null ? new BSHFunction(m): null;
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}

	/** Prepares the namespace for non-top-level namespace.
	 */
	private NameSpace prepareNS(Namespace ns) {
		if (ns == getOwner().getNamespace())
			return _bshns;

		NSX nsx = (NSX)ns.getVariable(VAR_NS, true);
		if (nsx != null)
			return nsx.ns;

		//bind bshns and ns
		Namespace p = ns.getParent();
		NameSpace bshns = new NS(p != null ? prepareNS(p): null, ns);
		ns.setVariable(VAR_NS, new NSX(bshns), true);
		return bshns;
	}

	//supporting classes//
	/** The global namespace. */
	private class GlobalNS extends NameSpace {
		private boolean _inGet;

		/** Don't call this method. */
	    private GlobalNS(BshClassManager classManager, String name) {
	    	super(classManager, name);
	    }
	    protected GlobalNS(NameSpace ns, String name) {
	    	super(ns, name);
	    }

		protected Object getFromNamespace(String name) {
			return BSHInterpreter.this.getFromNamespace(name);
		}

		//super//
		protected Variable getVariableImpl(String name, boolean recurse)
		throws UtilEvalError {
			//Note: getVariableImpl returns null if not defined,
			//while getVariable return Primitive.VOID if not defined

			//Tom M Yeh: 20060606:
			//We cannot override getVariable because BeanShell use
			//getVariableImpl to resolve a variable recusrivly
			//
			//setVariable will callback this method,
			//so use _inGet to prevent dead loop
			Variable var = super.getVariableImpl(name, recurse);
			if (!_inGet && var == null) {
				Object v = getFromNamespace(name);
				if (v != UNDEFINED) {
			//Variable has no public/protected contructor, so we have to
			//store the value back (with setVariable) and retrieve again
					_inGet = true;
					try {
						this.setVariable(name,
							v != null ? v: Primitive.NULL, false);
						var = super.getVariableImpl(name, false);
						this.unsetVariable(name); //restore
					} finally {
						_inGet = false;
					}
				}
			}
			return var;
		}
	    public void loadDefaultImports() {
	    	 //to speed up the formance
	    }
	}
	/** The per-Namespace NameSpace. */
	private class NS extends GlobalNS {
		private final Namespace _ns;

		private NS(NameSpace parent, Namespace ns) {
			super(parent, "ns" + System.identityHashCode(ns));
			_ns = ns;
			_ns.addChangeListener(new NSCListener(this));
		}

		/** Search _ns instead. */
		protected Object getFromNamespace(String name) {
			return BSHInterpreter.this.getFromNamespace(_ns, name);
		}
	}
	private class NSCListener implements NamespaceChangeListener {
		private final NameSpace _bshns;
		private NSCListener(NameSpace bshns) {
			_bshns = bshns;
		}
		public void onAdd(String name, Object value) {
		}
		public void onRemove(String name) {
		}
		public void onParentChanged(Namespace newparent) {
			_bshns.setParent(
				newparent != null ? prepareNS(newparent): null);
		}
	};
	/** Non-serializable namespace. It is used to prevent itself from
	 * being serialized
	 */
	private static class NSX {
		NameSpace ns;
		private NSX(NameSpace ns) {
			this.ns = ns;
		}
	}

	//SerializableAware//
	public void write(java.io.ObjectOutputStream s, Filter filter)
	throws java.io.IOException {
		//1. variables
		final String[] vars = _bshns.getVariableNames();
		for (int j = vars != null ? vars.length: 0; --j >= 0;) {
			final String nm = vars[j];
			if (nm != null && !"bsh".equals(nm)) {
				final Object val = get(nm);
				if ((val == null || (val instanceof java.io.Serializable)
					|| (val instanceof java.io.Externalizable))
				&& (filter == null || filter.accept(nm, val))) {
					s.writeObject(nm);
					s.writeObject(val);
				}
			}
		}
		s.writeObject(null); //denote end-of-vars

		//2. methods
		final BshMethod[] mtds = _bshns.getMethods();
		for (int j = mtds != null ? mtds.length: 0; --j >= 0;) {
			final String nm = mtds[j].getName();
			if (filter == null || filter.accept(nm, mtds[j])) {
				//hack BeanShell 2.0b4 which cannot be serialized correctly
				Field f = null;
				boolean acs = false;
				try {
					f = Classes.getAnyField(BshMethod.class, "declaringNameSpace");
					acs = f.isAccessible();
					f.setAccessible(true);
					final Object old = f.get(mtds[j]);
					try {
						f.set(mtds[j], null);				
						s.writeObject(mtds[j]);
					} finally {
						f.set(mtds[j], old);
					}
				} catch (java.io.IOException ex) {
					throw ex;
				} catch (Throwable ex) {
					throw UiException.Aide.wrap(ex);
				} finally {
					if (f != null) f.setAccessible(acs);
				}
			}
		}
		s.writeObject(null); //denote end-of-mtds

		//3. imported class
		Field f = null;
		boolean acs = false;
		try {
			f = Classes.getAnyField(NameSpace.class, "importedClasses");
			acs = f.isAccessible();
			f.setAccessible(true);
			final Map clses = (Map)f.get(_bshns);
			if (clses != null)
				for (Iterator it = clses.values().iterator(); it.hasNext();) {
					final String clsnm = (String)it.next();
					if (!clsnm.startsWith("bsh."))
						s.writeObject(clsnm);
				}
		} catch (java.io.IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw UiException.Aide.wrap(ex);
		} finally {
			if (f != null) f.setAccessible(acs);
		}
		s.writeObject(null); //denote end-of-cls

		//4. imported package
		f = null;
		acs = false;
		try {
			f = Classes.getAnyField(NameSpace.class, "importedPackages");
			acs = f.isAccessible();
			f.setAccessible(true);
			final Collection pkgs = (Collection)f.get(_bshns);
			if (pkgs != null)
				for (Iterator it = pkgs.iterator(); it.hasNext();) {
					final String pkgnm = (String)it.next();
					if (!pkgnm.startsWith("java.awt")
					&& !pkgnm.startsWith("javax.swing"))
						s.writeObject(pkgnm);
				}
		} catch (java.io.IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw UiException.Aide.wrap(ex);
		} finally {
			if (f != null) f.setAccessible(acs);
		}
		s.writeObject(null); //denote end-of-cls
	}
	public void read(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		for (;;) {
			final String nm = (String)s.readObject();
			if (nm == null) break; //no more

			set(nm, s.readObject());
		}

		try {
			for (;;) {
				final BshMethod mtd = (BshMethod)s.readObject();
				if (mtd == null) break; //no more

				//fix declaringNameSpace
				Field f = null;
				boolean acs = false;
				try {
					f = Classes.getAnyField(BshMethod.class, "declaringNameSpace");
					acs = f.isAccessible();
					f.setAccessible(true);
					f.set(mtd, _bshns);				
				} catch (Throwable ex) {
					throw UiException.Aide.wrap(ex);
				} finally {
					if (f != null) f.setAccessible(acs);
				}

				_bshns.setMethod(mtd.getName(), mtd);
			}
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}

		for (;;) {
			final String nm = (String)s.readObject();
			if (nm == null) break; //no more

			_bshns.importClass(nm);
		}

		for (;;) {
			final String nm = (String)s.readObject();
			if (nm == null) break; //no more

			_bshns.importPackage(nm);
		}
	}

	private class BSHFunction implements Function {
		private final bsh.BshMethod _method;
		private BSHFunction(bsh.BshMethod method) {
			if (method == null)
				throw new IllegalArgumentException("null");
			_method = method;
		}

		//-- Function --//
		public Class[] getParameterTypes() {
			return _method.getParameterTypes();
		}
		public Class getReturnType() {
			return _method.getReturnType();
		}
		public Object invoke(Object obj, Object[] args) throws Exception {
			return _method.invoke(args != null ? args: new Object[0], _ip);
		}
		public java.lang.reflect.Method toMethod() {
			return null;
		}
	}
}
