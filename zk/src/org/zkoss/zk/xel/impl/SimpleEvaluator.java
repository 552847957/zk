/* SimpleEvaluator.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 30 22:43:08     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.xel.impl;

import org.zkoss.xel.XelContext;
import org.zkoss.xel.FunctionMapper;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.Expression;
import org.zkoss.xel.Expressions;
import org.zkoss.xel.ExpressionFactory;
import org.zkoss.xel.XelException;
import org.zkoss.xel.util.SimpleXelContext;

import org.zkoss.web.servlet.xel.RequestContext;
import org.zkoss.web.servlet.xel.RequestContexts;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.xel.Evaluator;

/**
 * A simple implementation of {@link Evaluator}.
 *
 * @author tomyeh
 * @since 3.0.0
 */
public class SimpleEvaluator implements Evaluator {
	private transient ExpressionFactory _expf;
	private final Class _expfcls;
	private final FunctionMapper _mapper;

	/**
	 * @param expfcls the class that implements the expression factory.
	 * If null, the default one is used.
	 */
	public SimpleEvaluator(FunctionMapper mapper, Class expfcls) {
		_expfcls = expfcls;
		_mapper = mapper;
	}

	//Evaluator//
	public Expression parseExpression(String expression, Class expectedType)
	throws XelException {
		return getExpressionFactory()
			.parseExpression(getXelContext(null), expression, expectedType);
	}
	public Object evaluate(Page page, Expression expression)
	throws XelException {
		return expression.evaluate(getXelContext(page));
	}
	public Object evaluate(Component comp, Expression expression)
	throws XelException {
		return expression.evaluate(getXelContext(comp));
	}

	/** Returns the implementation class of the expression factory,
	 * or null to use the default.
	 */
	public Class getExpressionFactoryClass() {
		return _expfcls;
	}

	/** Returns the expression factory. */
	private ExpressionFactory getExpressionFactory() {
		if (_expf == null)
			_expf = Expressions.newExpressionFactory(_expfcls);
		return _expf;
	}
	/** Returns the XEL context.
	 */
	private XelContext getXelContext(Object ref) {
		final FunctionMapper mapper = getFunctionMapper(ref);
		final VariableResolver resolver = getVariableResolver(ref);
		return new SimpleXelContext(resolver, mapper);
			//Bug 1814838: don't cache the instance
	}

	/** Returns the function mapper, or null if not available.
	 *
	 * <p>Default: returns the function mapper passed thru
	 * {@link #SimpleEvaluator}.
	 *
	 * @param ref the object,either page, component, or null.
	 * It is passed to {@link #evaluate}
	 */
	public FunctionMapper getFunctionMapper(Object ref) {
		return _mapper;
	}
	/** Returns the variable resolver, or null if not available.
	 *
	 * <p>Default: it returns the variable resolver of the
	 * current execution, if any.
	 * Otherwise, it returns the current {@link RequestContext}, if any.
	 * If both not available, null is returned.
	 *
	 * @param ref the object,either page, component, or null.
	 * It is passed to {@link #evaluate}
	 */
	public VariableResolver getVariableResolver(Object ref) {
		final Execution exec = Executions.getCurrent();
		VariableResolver resolver = exec.getVariableResolver();
		if (resolver == null) {
			final RequestContext rc = RequestContexts.getCurrent();
			if (rc != null)
				resolver = rc.getVariableResolver();
			if (resolver == null)
				return null;
		}
		if (resolver instanceof ExecutionResolver)
			((ExecutionResolver)resolver).setSelf(ref);
		return resolver;
	}
}
