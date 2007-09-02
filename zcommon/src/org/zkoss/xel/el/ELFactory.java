/* ELFactory.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Aug 31 17:00:40     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.xel.el;

import org.zkoss.xel.Expression;
import org.zkoss.xel.ExpressionFactory;
import org.zkoss.xel.XelContext;
import org.zkoss.xel.XelException;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.ELException;

/**
 * An implementation based on org.apache.commons.el.ExpressionEvaluatorImpl.
 *
 * @author tomyeh
 * @since 3.0.0
 */
public class ELFactory implements ExpressionFactory {
	private final ExpressionEvaluator _eval;

	public ELFactory() {
		_eval = newExpressionEvaluator();
	}

	//ExpressionFactory//
	public boolean isSupported(int feature) {
		return feature == FEATURE_FUNCTION;
	}
	public Expression parseExpression(XelContext xelc, String expression,
	Class expectedType)
	throws XelException {
		try {
			return new ELXelExpression(
				_eval.parseExpression(expression, expectedType,
					xelc != null ? new XelELMapper(xelc.getFunctionMapper()): null));
		} catch (ELException ex) {
			throw new XelException("Failed to parse "+expression, ex);
		}
	}
	public Object evaluate(XelContext xelc, String expression,
	Class expectedType)
	throws XelException {
		try {
			return _eval.evaluate(expression, expectedType,
				xelc != null ?
					new XelELResolver(xelc.getVariableResolver()): null,
				xelc != null ?
					new XelELMapper(xelc.getFunctionMapper()): null);
		} catch (ELException ex) {
			throw new XelException("Failed to evaluate "+expression, ex);
		}
	}

	/** Returns the EL expression factory.
	 * <p>Default: Use org.apache.commons.el.ExpressionEvaluatorImpl.
	 * <p>You might override it to use a different implementation.
	 */
	protected ExpressionEvaluator newExpressionEvaluator() {
		return new org.apache.commons.el.ExpressionEvaluatorImpl();
	}
}
