/* Layoutregion.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Aug 27, 2007 3:30:53 PM , Created by jumperchen
 }}IS_NOTE

 Copyright (C) 2007 Potix Corporation. All Rights Reserved.

 {{IS_RIGHT
 This program is distributed under GPL Version 2.0 in the hope that
 it will be useful, but WITHOUT ANY WARRANTY.
 }}IS_RIGHT
 */
package org.zkoss.zkex.zul;

import org.zkoss.lang.Objects;
import org.zkoss.xml.HTMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.client.Openable;
import org.zkoss.zul.impl.XulElement;
import org.zkoss.zul.impl.Utils;

/**
 * This class represents a region in a layout manager.
 * <p>
 * Events:<br/> onOpen.<br/>
 * 
 * The default class of CSS is specified "layout-region" and
 * "layout-region-normal". If the border specifies "none" or null or "0", the
 * "layout-region-normal" class will remove.
 * 
 * @author jumperchen
 * @since 3.0.0
 */
public abstract class LayoutRegion extends XulElement {

	private boolean _flex;

	private boolean _splittable;

	private boolean _collapsible;

	private boolean _open = true;

	private boolean _autoscroll;

	private String _border = "normal";

	private int _maxsize = 2000;

	private int _minsize = 0;

	private int[] _margins = new int[] { 0, 0, 0, 0 };

	public LayoutRegion() {
		addSclass("layout-region");
		addSclass("layout-region-normal");
	}

	/**
	 * Returns the border.
	 * <p>
	 * The border actually controls what CSS class to use: If border is null, it
	 * implies "none".
	 * 
	 * <p>
	 * If you also specify the CSS class ({@link #setClass}), it overwrites
	 * whatever border you specify here.
	 * 
	 * <p>
	 * Default: "normal".
	 */
	public String getBorder() {
		return _border;
	}

	/**
	 * Sets the border (either none or normal).
	 * 
	 * @param border
	 *            the border. If null or "0", "none" is assumed.
	 */
	public void setBorder(String border) {
		if (border == null || "0".equals(border))
			border = "none";
		if (!_border.equals(border)) {
			_border = border;
			if (_border.equals("none")) {
				removeSclass("layout-region-normal");
			} else {
				addSclass("layout-region-normal");
			}
		}
	}

	/**
	 * Returns whether enable the split functionality.
	 * <p>
	 * Default: false.
	 */
	public boolean isSplittable() {
		return _splittable;
	}

	/**
	 * Sets whether enable the split functionality.
	 */
	public void setSplittable(boolean splittable) {
		if (_splittable != splittable) {
			_splittable = splittable;
			smartUpdate("z.splt", _splittable);
		}
	}

	/**
	 * Sets the maximum size of the resizing element.
	 */
	public void setMaxsize(int maxsize) {
		if (_maxsize != maxsize) {
			_maxsize = maxsize;
			smartUpdate("z.maxs", _maxsize);
		}
	}

	/**
	 * Returns the maximum size of the resizing element.
	 * <p>
	 * Default: 2000.
	 */
	public int getMaxsize() {
		return _maxsize;
	}

	/**
	 * Sets the minimum size of the resizing element.
	 */
	public void setMinsize(int minsize) {
		if (_minsize != minsize) {
			_minsize = minsize;
			smartUpdate("z.mins", _minsize);
		}
	}

	/**
	 * Returns the minimum size of the resizing element.
	 * <p>
	 * Default: 0.
	 */
	public int getMinsize() {
		return _minsize;
	}

	/**
	 * Returns whether to grow and shrink vertical/horizontal to fit their given
	 * space, so called flexibility.
	 * 
	 * <p>
	 * Default: false.
	 */
	public final boolean isFlex() {
		return _flex;
	}

	/**
	 * Sets whether to grow and shrink vertical/horizontal to fit their given
	 * space, so called flexibility.
	 * 
	 */
	public void setFlex(boolean flex) {
		if (_flex != flex) {
			_flex = flex;
			invalidate();
		}
	}

	/**
	 * Returns the margins, which is a list of numbers separated by comma.
	 * 
	 * <p>
	 * Default: "0,0,0,0".
	 */
	public String getMargins() {
		return Utils.intsToString(_margins);
	}

	/**
	 * Sets margins for the element "0,1,2,3" that direction is
	 * "top,left,right,bottom"
	 */
	public void setMargins(String margins) {
		final int[] imargins = Utils.stringToInts(margins, 0);
		if (!Objects.equals(imargins, _margins)) {
			_margins = imargins;
			smartUpdate("z.mars", Utils.intsToString(_margins));
		}
	}

	/**
	 * Returns whether set the initial display to collapse.
	 * <p>
	 * Default: false.
	 */
	public boolean isCollapsible() {
		return _collapsible;
	}

	/**
	 * Sets whether set the initial display to collapse.
	 */
	public void setCollapsible(boolean collapsible) {
		if (collapsible != _collapsible) {
			_collapsible = collapsible;
			smartUpdate("z.colps", _collapsible);
		}
	}

	/**
	 * Returns whether enable overflow scrolling.
	 * <p>
	 * Default: false.
	 */
	public boolean isAutoscroll() {
		return _autoscroll;
	}

	/**
	 * Sets whether enable overflow scrolling.
	 */
	public void setAutoscroll(boolean autoscroll) {
		if (_autoscroll != autoscroll) {
			_autoscroll = autoscroll;
			smartUpdate("z.autoscl", _autoscroll);
		}
	}

	/**
	 * Returns whether it is opne (i.e., not collapsed. Meaningful only if
	 * {@link #isCollapsible} is not false.
	 * <p>
	 * Default: true.
	 */
	public boolean isOpen() {
		return _open;
	}

	/**
	 * Opens or collapses the splitter. Meaningful only if
	 * {@link #isCollapsible} is not false.
	 */
	public void setOpen(boolean open) {
		if (_open != open) {
			_open = open;
			smartUpdate("z.open", open);
		}
	}

	/**
	 * Returns this regions position (north/south/east/west/center).
	 * 
	 * @see Borderlayout#NORTH
	 * @see Borderlayout#SOUTH
	 * @see Borderlayout#EAST
	 * @see Borderlayout#WEST
	 * @see Borderlayout#CENTER
	 */
	abstract public String getPosition();

	/**
	 * Sets the size of this region. This method is shortcut for
	 * {@link #setHeight(String)} and {@link #setWidth(String)}. If this region
	 * is {@link North} or {@link South}, this method will invoke
	 * {@link #setHeight(String)}. If this region is {@link West} or
	 * {@link East}, this method will invoke {@link #setWidth(String)}.
	 * Otherwise it will throw a {@link UnsupportedOperationException}.
	 */
	abstract public void setSize(String size);

	/**
	 * Returns the size of this region. This method is shortcut for
	 * {@link #getHeight()} and {@link #getWidth()}. If this region is
	 * {@link North} or {@link South}, this method will invoke
	 * {@link #getHeight()}. If this region is {@link West} or {@link East},
	 * this method will invoke {@link #getWidth()}. Otherwise it will throw a
	 * {@link UnsupportedOperationException}.
	 */
	abstract public String getSize();

	protected void addSclass(String cls) {
		final String sclass = getSclass();
		if (!hasSclass(cls))
			setSclass(sclass == null ? cls : sclass + " " + cls);
	}

	protected boolean hasSclass(String cls) {
		String sclass = getSclass();
		if (sclass == null)
			sclass = "";
		return cls == null
				|| ((" " + sclass + " ").indexOf(" " + cls + " ") > -1);
	}

	protected void removeSclass(String cls) {
		final String sclass = getSclass();
		if (sclass != null && cls != null && hasSclass(cls)) {
			setSclass(sclass.replaceAll("(?:^|\\s+)" + cls + "(?:\\s+|$)", " "));
		}
	}

	public void onChildRemoved(Component child) {
		smartUpdate("z.cid", "zk_n_a");
		if (child instanceof Borderlayout) {
			setFlex(false);
			removeSclass("layout-nested");
		}
	}

	public boolean insertBefore(Component child, Component insertBefore) {
		if (getChildren().size() > 0)
			throw new UiException("Only one child is allowed: " + this);
		if (super.insertBefore(child, insertBefore)) {
			smartUpdate("z.cid", child.getUuid());
			if (child instanceof Borderlayout) {
				setFlex(true);
				addSclass("layout-nested");
			}
			return true;
		}
		return false;
	}

	public String getOuterAttrs() {
		final StringBuffer sb = new StringBuffer(80).append(super
				.getOuterAttrs());
		appendAsapAttr(sb, Events.ON_OPEN);
		HTMLs.appendAttribute(sb, "z.cid", getChildren().isEmpty() ? "zk_n_a"
				: ((Component) getChildren().get(0)).getUuid());
		HTMLs.appendAttribute(sb, "z.pos", getPosition());
		HTMLs.appendAttribute(sb, "z.flex", isFlex());
		HTMLs.appendAttribute(sb, "z.mars", getMargins());
		HTMLs.appendAttribute(sb, "z.colps", isCollapsible());
		HTMLs.appendAttribute(sb, "z.open", isOpen());
		HTMLs.appendAttribute(sb, "z.splt", isSplittable());
		HTMLs.appendAttribute(sb, "z.maxs", getMaxsize());
		HTMLs.appendAttribute(sb, "z.mins", getMinsize());
		HTMLs.appendAttribute(sb, "z.autoscl", isAutoscroll());
		return sb.toString();
	}
	//-- ComponentCtrl --//
	protected Object newExtraCtrl() {
		return new ExtraCtrl();
	}
	/** A utility class to implement {@link #getExtraCtrl}.
	 * It is used only by component developers.
	 */
	protected class ExtraCtrl extends XulElement.ExtraCtrl
	implements Openable {
		//-- Openable --//
		public void setOpenByClient(boolean open) {
			_open = open;
		}
	}
}
