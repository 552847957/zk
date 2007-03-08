/* RowRenderer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Mar  8 10:55:50     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

/**
 * Identifies components that can be used as "rubber stamps" to paint
 * the cells in a {@link Grid}.
 *
 * <p>If you need better control, your renderer can also implement
 * {@link RowRendererExt}.
 *
 * <p>In addition, you could also
 * implement {@link RendererCtrl}. For example, starts an transaction,
 * and uses it to render all rows for the same request.
 *
 * @author tomyeh
 * @see ListModel
 * @see Listbox
 * @see RowRendererExt
 */
public interface RowRenderer {
	/** Renders the data to the specified row.
	 *
	 * @param row the row to render the result.
	 * Note: when this method is called, the row has no child
	 * at all, unless you don't return
	 * {@link RowRendererExt#DETACH_ON_RENDER} when
	 * {@link RowRendererExt#getControls} is called.
	 *
	 * @param data that is returned from {@link ListModel#getElementAt}
	 */
	public void render(Row row, Object data) throws Exception;
}
