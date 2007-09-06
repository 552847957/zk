/* VariablesInfo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Feb 28 19:19:49     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.metainfo;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.util.Condition;
import org.zkoss.zk.ui.util.ConditionImpl;
import org.zkoss.zk.xel.ExValue;
import org.zkoss.zk.xel.Evaluator;
import org.zkoss.zk.xel.impl.EvaluatorRef;

/**
 * The information about the variables element in the ZUML page.
 * 
 * @author tomyeh
 */
public class VariablesInfo extends EvalRefStub
implements Condition, java.io.Serializable {
	/** Map(String name, ExValue value). */
	private final Map _vars;
	private final ConditionImpl _cond;
	private final boolean _local;

	/**
	 * @param evalr the evaluator reference. It cannot be null.
	 * Retrieve it from {@link LanguageDefinition#getEvaluatorRef}
	 * or {@link PageDefinition#getEvaluatorRef}, depending which it
	 * belongs.
	 * @param vars a map of (String name, String value).
	 * Note: once called, the caller cannot access it any more.
	 * In other words, it becomes part of this object.
	 */
	public VariablesInfo(EvaluatorRef evalr, Map vars, boolean local,
	ConditionImpl cond) {
		if (evalr == null) throw new IllegalArgumentException();
		_evalr = evalr;
		_vars = vars;
		if (_vars != null) {
			for (Iterator it = _vars.entrySet().iterator(); it.hasNext();) {
				final Map.Entry me = (Map.Entry)it.next();
				me.setValue(new ExValue((String)me.getValue(), Object.class));
			}
		}

		_local = local;
		_cond = cond;
	}

	/** Applies the variable element against the parent component.
	 *
	 * @param comp the parent component (it cannot be null)
	 */
	public void apply(Component comp) {
		if (_vars != null && isEffective(comp)) {
			final Evaluator eval = _evalr.getEvaluator();
			for (Iterator it = _vars.entrySet().iterator(); it.hasNext();) {
				final Map.Entry me = (Map.Entry)it.next();
				final String name = (String)me.getKey();
				final ExValue value = (ExValue)me.getValue();
				comp.setVariable(name, value.getValue(eval, comp), _local);
			}
		}
	}
	/** Applies the variable element against the page.
	 * It is called if the element doesn't belong to any component.
	 */
	public void apply(Page page) {
		if (_vars != null && isEffective(page)) {
			final Evaluator eval = _evalr.getEvaluator();
			for (Iterator it = _vars.entrySet().iterator(); it.hasNext();) {
				final Map.Entry me = (Map.Entry)it.next();
				final String name = (String)me.getKey();
				final ExValue value = (ExValue)me.getValue();
				page.setVariable(name, value.getValue(eval, page));
			}
		}
	}

	//Condition//
	public boolean isEffective(Component comp) {
		return _cond == null || _cond.isEffective(_evalr, comp);
	}
	public boolean isEffective(Page page) {
		return _cond == null || _cond.isEffective(_evalr, page);
	}

	//Object//
	public String toString() {
		final StringBuffer sb = new StringBuffer(40).append("[variables:");
		if (_vars != null)
			for (Iterator it = _vars.keySet().iterator(); it.hasNext();)
				sb.append(' ').append(it.next());
		return sb.append(']').toString();
	}
}
