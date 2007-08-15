/* TreeitemRenderer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 10 2007, Created by Jeff Liu
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;
/**
 * Identifies components that can be used as "rubber stamps" to paint
 * the cells in a {@link Tree}.
 *
 * @author Jeff Liu
 * @see TreeModel
 * @see Tree
 */
public interface TreeitemRenderer {
	/** 
	 * Renders the data to the specified tree item.
	 * 
	 * 
	 *
	 * @param item the Treeitem to render the result.
	 * <br>Note: 
	 * 
	 *<ol>
	 * <li>When this method is called, the treeitem should have no child
	 * at all, unless you don't return</li>
	 * <li>Treeitem and Treerow are only components that allowed to be
	 * <b>item</b>'s children.</li>
	 * <li>A new treerow should be contructed and append to <b>item</b>, when
	 * treerow of <b>item</b> is null.<br/> Otherwise, when treerow of <b>item</b> is not null, 
	 * modify the content of the treerow or detach the treerow first, since that only one treerow is allowed</li>
	 * <li>Do not append any treechildren to <b>item</b> in this method</li>, a treechildren will be appended afterward.
	 * <li>When a treerow is not appended to <b>item</b>,  generally label of <b>item</b> displayed.</li> 
	 * </ol>
	 * @param data that is used to render the Treeitem
	 * 
	 */
	public void render(Treeitem item, Object data) throws Exception;
}
