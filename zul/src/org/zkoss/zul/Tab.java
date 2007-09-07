/* Tab.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jul 12 10:43:18     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.Set;
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.client.Selectable;

import org.zkoss.zul.impl.LabelImageElement;

/**
 * A tab.
 *
 * @author tomyeh
 */
public class Tab extends LabelImageElement {
	private boolean _selected;
	/** Whether to show a close button. */
	private boolean _closable;

	public Tab() {
	}
	public Tab(String label) {
		setLabel(label);
	}
	public Tab(String label, String image) {
		setLabel(label);
		setImage(image);
	}

	/** Returns whether this tab is closable.
	 * If closable, a button is displayed and the onClose event is sent
	 * if an user clicks the button.
	 * <p>Default: false.
	 */
	public boolean isClosable() {
		return _closable;
	}
	/** Sets whether this tab is closable.
	 * If closable, a button is displayed and the onClose event is sent
	 * if an user clicks the button.
	 * <p>Default: false.
	 * <p>You can intercept the default behavior by either overriding
	 * {@link #onClose}, or listening the onClose event.
	 */
	public void setClosable(boolean closable) {
		if (_closable != closable) {
			_closable = closable;
			invalidate(); //re-init is required
		}
	}

	/** Process the onClose event sent when the close button is pressed.
	 * <p>Default: detach itself and the corresponding {@link Tabpanel}.
	 */
	public void onClose() {
		if (_selected) {
			final Tabs tabs = (Tabs)getParent();
			if (tabs != null) {
				for (Iterator it = tabs.getChildren().iterator(); it.hasNext();) {
					final Tab t = (Tab)it.next();
					if (t != this) {
						t.setSelected(true);
						break;
					}
				}
			}
		}

		final Tabpanel panel = getLinkedPanel();
		if (panel != null) panel.detach();
		detach();
	}

	/** Returns the tabbox owns this component.
	 */
	public Tabbox getTabbox() {
		final Tabs tabs = (Tabs)getParent();
		return tabs != null ? tabs.getTabbox(): null;
	}
	/** Returns the panel associated with this tab.
	 */
	public Tabpanel getLinkedPanel() {
		final int j = getIndex();
		if (j >= 0) {
			final Tabbox tabbox = getTabbox();
			if (tabbox != null) {
				final Tabpanels tabpanels = tabbox.getTabpanels();
				if (tabpanels != null && tabpanels.getChildren().size() > j)
					return (Tabpanel)tabpanels.getChildren().get(j);
			}
		}
		return null;
	}

	/** Returns whether this tab is selected.
	 */
	public final boolean isSelected() {
		return _selected;
	}
	/** Sets whether this tab is selected.
	 */
	public void setSelected(boolean selected) {
		if (_selected != selected) {
			final Tabbox tabbox = (Tabbox)getTabbox();
			if (tabbox != null) {
				//Note: we don't update it here but let its parent does the job
				tabbox.setSelectedTab(this);
			} else {
				_selected = selected;
				invalidate();
			}
		}
	}
	/** Updates _selected directly without updating the client.
	 */
	/*package*/ void setSelectedDirectly(boolean selected) {
		_selected = selected;
	}

	/** Returns the index of this panel, or -1 if it doesn't belong to any
	 * tabs.
	 */
	public int getIndex() {
		final Tabs tabs = (Tabs)getParent();
		if (tabs == null)
			return -1;
		int j = 0;
		for (Iterator it = tabs.getChildren().iterator();; ++j)
			if (it.next() == this)
				return j;
	}

	//-- super --//
	public String getOuterAttrs() {
		final StringBuffer sb =
			new StringBuffer(64).append(super.getOuterAttrs());
		appendAsapAttr(sb, Events.ON_SELECT);

		final String clkattrs = getAllOnClickAttrs(false);
		if (clkattrs != null) 
			sb.append(clkattrs);
		return sb.toString();
	}
	/** Returns the style class.
	 * Note: 1) if not specified (or setSclass(null)), "tab" is assumed;
	 * 2) if selected, it appends "sel" to super's getSclass().
	 */
	public String getSclass() {
		String scls = super.getSclass();
		if (scls == null) scls = "tab";
		return isSelected() ? scls + "sel": scls;
	}

	//-- Component --//
	public void invalidate() {
		final Tabbox tabbox = getTabbox();
		if (tabbox != null && "accordion".equals(tabbox.getMold()))
			tabbox.invalidate();
		else
			super.invalidate();
	}
	/** No child is allowed.
	 */
	public boolean isChildable() {
		return false;
	}
	public void setParent(Component parent) {
		if (parent != null && !(parent instanceof Tabs))
			throw new UiException("Wrong parent: "+parent);
		super.setParent(parent);
	}

	//-- ComponentCtrl --//
	protected Object newExtraCtrl() {
		return new ExtraCtrl();
	}
	/** A utility class to implement {@link #getExtraCtrl}.
	 * It is used only by component developers.
	 */
	protected class ExtraCtrl extends LabelImageElement.ExtraCtrl
	implements Selectable {
		//-- Selectable --//
		public void selectItemsByClient(Set selItems) {
			if (selItems == null || selItems.size() != 1)
				throw new UiException("Exactly one selected tab is required: "+selItems); //debug purpose

			final Tabbox tabbox = getTabbox();
			if (tabbox != null)
				tabbox.selectTabDirectly((Tab)selItems.iterator().next(), true);
		}
	}
 }
