/* Tabbox.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jul 12 10:42:31     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.zul.html;

import java.util.Iterator;
import java.util.Set;

import com.potix.lang.Objects;
import com.potix.xml.HTMLs;

import com.potix.zk.ui.Component;
import com.potix.zk.ui.UiException;
import com.potix.zk.ui.WrongValueException;
import com.potix.zk.ui.event.Events;
import com.potix.zk.ui.ext.Selectable;
import com.potix.zk.au.AuScript;

import com.potix.zul.html.impl.XulElement;

/**
 * A tabbox.
 *
 * <p>Event:
 * <ol>
 * <li>com.potix.zk.ui.event.SelectEvent is sent when user changes
 * the tab.</li>
 * </ol>
 *
 * <p>Mold:
 * <dl>
 * <dt>default</dt>
 * <dd>The default tabbox.</dd>
 * <dt>accordion</dt>
 * <dd>The accordion tabbox.</dd>
 * </dl>
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class Tabbox extends XulElement implements Selectable {
	private transient Tabs _tabs;
	private transient Tabpanels _tabpanels;
	private transient Tab _seltab;
	private String _panelSpacing;
	private String _orient = "horizontal";

	/** Returns the tabs that this tabbox owns.
	 */
	public Tabs getTabs() {
		return _tabs;
	}
	/** Returns the tabpanels that this tabbox owns.
	 */
	public Tabpanels getTabpanels() {
		return _tabpanels;
	}

	/** Returns the spacing between {@link Tabpanel}.
	 * This is used by certain molds, such as accordion.
	 * <p>Default: null (no spacing).
	 */
	public String getPanelSpacing() {
		return _panelSpacing;
	}
	/** Sets the spacing between {@link Tabpanel}.
	 * This is used by certain molds, such as accordion.
	 */
	public void setPanelSpacing(String panelSpacing) {
		if (panelSpacing != null && panelSpacing.length() == 0)
			panelSpacing = null;

		if (!Objects.equals(_panelSpacing, panelSpacing)) {
			_panelSpacing = panelSpacing;
			invalidate(INNER);
		}
	}

	/** Returns the selected index.
	 */
	public int getSelectedIndex() {
		return _seltab != null ? _seltab.getIndex(): -1;
	}
	/*** Sets the selected index.
	 */
	public void setSelectedIndex(int j) {
		final Tabs tabs = getTabs();
		if (tabs == null)
			throw new IllegalStateException("No tab at all");
		setSelectedTab((Tab)tabs.getChildren().get(j));
	}

	/** Returns the selected tab panel.
	 */
	public Tabpanel getSelectedPanel() {
		return _seltab != null ? _seltab.getLinkedPanel(): null;
	}
	/** Sets the selected tab panel.
	 */
	public void setSelectedPanel(Tabpanel panel) {
		if (panel != null && panel.getTabbox() != this)
			throw new UiException("Not a child: "+panel);
		final Tab tab = panel.getLinkedTab();
		if (tab != null)
			setSelectedTab(tab);
	}
	/** Returns the selected tab.
	 */
	public Tab getSelectedTab() {
		return _seltab;
	}
	/** Sets the selected tab.
	 */
	public void setSelectedTab(Tab tab) {
		setSelectedTab0(tab, true);
	}
	private void setSelectedTab0(Tab tab, boolean update) {
		if (tab == null)
			throw new IllegalArgumentException("null tab");
		if (tab.getTabbox() != this)
			throw new UiException("Not my child: "+tab);
		if (tab != _seltab) {
			if (_seltab != null)
				_seltab.setSelectedDirectly(false);

			_seltab = tab;
			_seltab.setSelectedDirectly(true);
			if (update)
				response("sel", new AuScript(
					this, "zkTab.selTab('"+_seltab.getUuid()+"')"));
		}
	}

	/** Returns the orient.
	 *
	 * <p>Default: "horizontal".
	 *
	 * <p>Note: only the default mold supports it (not supported if accordion).
	 */
	public String getOrient() {
		return _orient;
	}
	/** Sets the orient.
	 * @param orient either "horizontal" or "vertical".
	 */
	public void setOrient(String orient) throws WrongValueException {
		if (!"horizontal".equals(orient) && !"vertical".equals(orient))
			throw new WrongValueException(orient);
		checkOrient(getMold(), orient);

		if (!Objects.equals(_orient, orient)) {
			_orient = orient;
			invalidate(OUTER);
		}
	}
	private static void checkOrient(String mold, String orient)
	throws WrongValueException {
		if ("vertical".equals(orient) && !"default".equals(mold))
			throw new WrongValueException("vertical can be used with the default mold");
	}

	//-- Selectable --//
	public void selectItemsByClient(Set selItems) {
		if (selItems != null && selItems.size() == 1)
			setSelectedTab0((Tab)selItems.iterator().next(), false);
		else
			throw new UiException("Exactly one selected tab is required: "+selItems); //debug purpose
	}

	//-- Component --//
	public void setMold(String mold) {
		checkOrient(mold, getOrient());
		super.setMold(mold);
	}

	/** Auto-creates {@link Tabpanel} and select one of tabs if necessary.
	 */
	public void onCreate() {
		if (_tabs != null) {
			final int sz = _tabs.getChildren().size();
			if (_tabpanels == null)
				insertBefore(new Tabpanels(), null);
			for (int n = _tabpanels.getChildren().size(); n < sz; ++n)
				_tabpanels.insertBefore(new Tabpanel(), null);
			if (sz > 0 && _seltab == null)
				setSelectedTab((Tab)_tabs.getChildren().get(0));
		}
	}
	public boolean insertBefore(Component child, Component insertBefore) {
		if (child instanceof Tabs) {
			if (_tabs != null && _tabs != child)
				throw new UiException("Only one tabs is allowed: "+this);
			_tabs = (Tabs)child;
		} else if (child instanceof Tabpanels) {
			if (_tabpanels != null && _tabpanels != child)
				throw new UiException("Only one tabpanels is allowed: "+this);
			_tabpanels = (Tabpanels)child;
		} else {
			throw new UiException("Unsupported child for tabbox: "+child);
		}
		if (super.insertBefore(child, insertBefore)) {
			invalidate(INNER); //due to DSP might implemented diff for children order
			return true;
		}
		return false;
	}
	public void onChildRemoved(Component child) {
		if (child instanceof Tabs) {
			_tabs = null;
			_seltab = null;
		} else if (child instanceof Tabpanels) {
			_tabpanels = null;
		}
		super.onChildRemoved(child);
	}

	//-- super --//
	public String getOuterAttrs() {
		final StringBuffer sb =
			new StringBuffer(64).append(super.getOuterAttrs());
		appendAsapAttr(sb, Events.ON_SELECT);
		appendAsapAttr(sb, Events.ON_RIGHT_CLICK);
			//no zk_dbclk/zk_lfclk since it is covered by both Tab and Tabpanel

		//HTMLs.appendAttribute(sb, "zk_orient", _orient);
		return sb.toString();
	}

	//Cloneable//
	public Object clone() {
		final Tabbox clone = (Tabbox)super.clone();

		int cnt = 0;
		if (clone._tabs != null) ++cnt;
		if (clone._tabpanels != null) ++cnt;
		if (cnt > 0) clone.afterUnmarshal(cnt);

		return clone;
	}
	private void afterUnmarshal(int cnt) {
		for (Iterator it = getChildren().iterator(); it.hasNext();) {
			final Object child = it.next();
			if (child instanceof Tabs) {
				_tabs = (Tabs)child;
				for (Iterator e = _tabs.getChildren().iterator();
				e.hasNext();) {
					final Tab tab = (Tab)e.next();
					if (tab.isSelected()) {
						_seltab = tab;
						break;
					}
				}
				if (--cnt == 0) break;
			} else if (child instanceof Tabpanels) {
				_tabpanels = (Tabpanels)child;
				if (--cnt == 0) break;
			}
		}
	}

	//-- Serializable --//
	private synchronized void readObject(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		afterUnmarshal(-1);
	}
}
