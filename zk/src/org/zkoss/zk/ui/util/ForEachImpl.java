/* ForEachImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Mar  8 14:21:08     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.util;

import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Enumeration;
import java.util.Iterator;

import org.zkoss.lang.Classes;
import org.zkoss.util.CollectionsX;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.xel.ExValue;
import org.zkoss.zk.xel.impl.EvaluatorRef;

/**
 * An implementation of {@link ForEach}.
 *
 * <p>Note: the use of {@link ForEachImpl} is different from
 * {@link ConditionImpl}. While you could use the same instance of
 * {@link ConditionImpl} for all evaluation, each instance of
 * {@link ForEachImpl} can be used only once (drop it after {@link #next}
 * returns false).
 *
 * @author tomyeh
 */
public class ForEachImpl implements ForEach {
	private final EvaluatorRef _evalr;
	private final Page _page;
	private final Component _comp;
	private final ExValue _expr;
	private final ExValue _begin, _end;
	private Status _status;
	private Iterator _it;
	private Object _oldEach;
	private boolean _done;

	/** Returns an instance that represents the iterator for the
	 * specified collection, or null if expr is null or empty.
	 *
	 * @param expr an EL expression that shall return a collection of objects.
	 */
	public static
	ForEach getInstance(EvaluatorRef evalr, Component comp, String expr, String begin, String end) {
		if (expr == null || expr.length() == 0)
			return null;
		return new ForEachImpl(evalr, comp, expr, begin, end);
	}
	/** Returns an instance that represents the iterator for the
	 * specified collection, or null if expr is null or empty.
	 *
	 * @param expr an EL expression that shall return a collection of objects.
	 * @since 3.0.0
	 */
	public static
	ForEach getInstance(EvaluatorRef evalr, Page page, String expr, String begin, String end) {
		if (expr == null || expr.length() == 0)
			return null;
		return new ForEachImpl(evalr, page, expr, begin, end);
	}

	/** Constructor.
	 * In most cases, use {@link #getInstance(EvaluatorRef, Component, String, String, String)}
	 * instead of this constructor.
	 * @exception IllegalArgumentException if comp or evalr is null
	 * @since 3.0.0
	 */
	public ForEachImpl(EvaluatorRef evalr, Component comp, String expr, String begin, String end) {
		if (comp == null || evalr == null)
			throw new IllegalArgumentException();

		_evalr = evalr;
		_page = null;
		_comp = comp;
		_expr = expr != null ? new ExValue(expr, Object.class): null;
		_begin = begin != null && begin.length() > 0 ? new ExValue(begin, Integer.class): null;
		_end = end != null && end.length() > 0 ? new ExValue(end, Integer.class): null;
	}
	/** Constructor.
	 * In most cases, use {@link #getInstance(EvaluatorRef, Component, String, String, String)}
	 * instead of this constructor.
	 * @exception IllegalArgumentException if page or evalr is null
	 * @since 3.0.0
	 */
	public ForEachImpl(EvaluatorRef evalr, Page page, String expr, String begin, String end) {
		if (page == null || evalr == null)
			throw new IllegalArgumentException();

		_evalr = evalr;
		_page = page;
		_comp = null;
		_expr = expr != null ? new ExValue(expr, Object.class): null;
		_begin = begin != null && begin.length() > 0 ? new ExValue(begin, Integer.class): null;
		_end = end != null && end.length() > 0 ? new ExValue(end, Integer.class): null;
	}
	private Object eval(ExValue value) {
		return value == null ? null:
			_comp != null ?
				value.getValue(_evalr, _comp): value.getValue(_evalr, _page);
	}

	//-- ForEach --//
	public boolean next() {
		if (_done)
			throw new IllegalStateException("Iterate twice not allowed");

		if (_status == null) {
			final Object o = eval(_expr);
			if (o == null) {
				_done = true;
				return false;
			}

			final Integer iend = (Integer)eval(_end);
			final Integer ibeg = (Integer)eval(_begin);
			final int vbeg = ibeg != null ? ibeg.intValue(): 0;
			if (vbeg < 0)
				throw new UiException("Negative forEachBegin is not allowed: "+ibeg);
			prepare(o, vbeg); //prepare iterator

			//preserve
			if (_comp != null) {
				_oldEach = _comp.getVariable("each", true);
				_status = new Status(_comp.getVariable("forEachStatus", true), ibeg, iend);
				_comp.setVariable("forEachStatus", _status, true);
			} else {
				_oldEach = _page.getVariable("each");
				_status = new Status(_page.getVariable("forEachStatus"), ibeg, iend);
				_page.setVariable("forEachStatus", _status);
			}
		}

		if ((_status.end == null || _status.index < _status.end.intValue())
		&& _it.hasNext()) {
			++_status.index;
			_status.each = _it.next();
			if (_comp != null) _comp.setVariable("each", _status.each, true);
			else _page.setVariable("each", _status.each);
			return true;
		}

		//restore
		_done = true;
		if (_comp != null) {
			if (_status.previous != null)
				_comp.setVariable("forEachStatus", _status.previous, true);
			else
				_comp.unsetVariable("forEachStatus", true);
			if (_oldEach != null)
				_comp.setVariable("each", _oldEach, true);
			else
				_comp.unsetVariable("each", true);
		} else {
			if (_status.previous != null)
				_page.setVariable("forEachStatus", _status.previous);
			else
				_page.unsetVariable("forEachStatus");
			if (_oldEach != null)
				_page.setVariable("each", _oldEach);
			else
				_page.unsetVariable("each");
		}
		_it = null; _status = null; //recycle (just in case)
		return false;
	}

	private void prepare(Object o, final int begin) {
		if (begin > 0 && (o instanceof List)) {
			final List l = (List)o;
			final int size = l.size();
			_it = l.listIterator(begin > size ? size: begin);
		} else if (o instanceof Collection) {
			_it = ((Collection)o).iterator();
			forward(begin);
		} else if (o instanceof Map) {
			_it = ((Map)o).entrySet().iterator();
			forward(begin);
		} else if (o instanceof Iterator) {
			_it = (Iterator)o;
			forward(begin);
		} else if (o instanceof Enumeration) {
			_it = new CollectionsX.EnumerationIterator((Enumeration)o);
			forward(begin);
		} else if (o instanceof Object[]) {
			final Object[] ary = (Object[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return ary[_j++];
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof int[]) {
			final int[] ary = (int[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Integer(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof long[]) {
			final long[] ary = (long[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Long(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof short[]) {
			final short[] ary = (short[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Short(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof byte[]) {
			final byte[] ary = (byte[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Byte(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof float[]) {
			final float[] ary = (float[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Float(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof double[]) {
			final double[] ary = (double[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Double(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else if (o instanceof char[]) {
			final char[] ary = (char[])o;
			_it = new Iterator() {
				private int _j = begin;
				public boolean hasNext() {
					return _j < ary.length;
				}
				public Object next() {
					return new Character(ary[_j++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else {
			_it = new CollectionsX.OneIterator(o);
			forward(begin);
		}
	}
	private void forward(int begin) {
		while (--begin >= 0 && _it.hasNext())
			_it.next();
	}
	private static class Status implements ForEachStatus {
		private final Object previous;
		private Object each;
		private int index;
		private final Integer begin, end;

		private Status(Object previous, Integer begin, Integer end) {
			this.previous = previous;
			this.begin = begin;
			this.end = end;
			this.index = begin != null ? begin.intValue() - 1: -1;
		}

		public ForEachStatus getPrevious() {
			return this.previous instanceof ForEachStatus ?
				(ForEachStatus)this.previous: null;
		}
		public Object getEach() {
			return this.each;
		}
		public int getIndex() {
			return this.index;
		}
		public Integer getBegin() {
			return this.begin;
		}
		public Integer getEnd() {
			return this.end;
		}
	}
}
