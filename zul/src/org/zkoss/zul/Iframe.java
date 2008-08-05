/* Iframe.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jul 21 11:11:18     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import org.zkoss.lang.Objects;
import org.zkoss.util.media.RepeatableMedia;
import org.zkoss.util.media.Media;
import org.zkoss.xml.HTMLs;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.ext.render.DynamicMedia;

import org.zkoss.zul.impl.XulElement;
import org.zkoss.zul.impl.Utils;

/**
 * Includes an inline frame.
 *
 * <p>Unlike HTML iframe, this component doesn't have the frameborder
 * property. Rather, use the CSS style to customize the border (like
 * any other components).
 *
 * @author tomyeh
 */
public class Iframe extends XulElement {
	private String _align, _name;
	private String _src, _scrolling = "auto";
	/** The media. If not null, _src is generated automatically. */
	private Media _media; 
	/** Count the version of {@link #_media}. */
	private int _medver;
	/** Whether to hide when a popup or dropdown is placed on top of it. */
	private boolean _autohide;

	public Iframe() {
	}
	public Iframe(String src) {
		setSrc(src);
	}
	
	/**
	 * Define scroll bars
	 * @param scrolling "true", "false", "yes" or "no" or "auto", "auto" by default
	 * If null, "auto" is assumed.
	 * @since 3.0.4
	 */
	public void setScrolling(String scrolling) {
		if (scrolling == null) scrolling = "auto";
		if (!scrolling.equals(_scrolling)) {
			_scrolling = scrolling;
			invalidate();
		}
	}
	
	/**
	 * Return the scroll bars.
	 * <p>Defalut: "auto"
	 * @since 3.0.4
	 */
	public String getScrolling() {
		return _scrolling;
	}
	
	/** Returns the alignment.
	 * <p>Default: null (use browser default).
	 */
	public String getAlign() {
		return _align;
	}
	/** Sets the alignment: one of top, middle, bottom, left, right and
	 * center.
	 */
	public void setAlign(String align) {
		if (align != null && align.length() == 0)
			align = null;

		if (!Objects.equals(_align, align)) {
			_align = align;
			smartUpdate("align", _align);
		}
	}
	/** Returns the frame name.
	 * <p>Default: null (use browser default).
	 */
	public String getName() {
		return _name;
	}
	/** Sets the frame name.
	 */
	public void setName(String name) {
		if (name != null && name.length() == 0)
			name = null;

		if (!Objects.equals(_name, name)) {
			_name = name;
			smartUpdate("name", _name);
		}
	}

	/** Returns whether to automatically hide this component if
	 * a popup or dropdown is overlapped with it.
	 *
	 * <p>Default: false.
	 *
	 * <p>If an iframe contains PDF or other embeds, it will be placed
	 * on top of other components. It may then make popups and dropdowns
	 * obscure. In this case, you have to specify autohide="true" to
	 * ask ZK to hide the iframe when popups or dropdowns is overlapped
	 * with the iframe.
	 */
	public boolean isAutohide() {
		return _autohide;
	}
	/** Sets whether to automatically hide this component if
	 * a popup or dropdown is overlapped with it.
	 */
	public void setAutohide(boolean autohide) {
		if (_autohide != autohide) {
			_autohide = autohide;
			smartUpdate("z.autohide", _autohide ? "true": null);
		}
	}

	/** Returns the src.
	 * <p>Default: null.
	 */
	public String getSrc() {
		return _src;
	}
	/** Sets the src.
	 *
	 * @param src the source URL. If null or empty, nothing is included.
	 */
	public void setSrc(String src) {
		if (src != null && src.length() == 0)
			src = null;

		if (!Objects.equals(_src, src)) {
			_src = src;
			if (_media == null)
				smartUpdateDeferred("src", new EncodedSrc()); //Bug 1850895
				//_src is meaningful only if _media is null
		}
	}
	/** Returns the encoded src ({@link #getSrc}).
	 */
	private String getEncodedSrc() {
		final Desktop dt = getDesktop();
		return _media != null ? getMediaSrc(): //already encoded
			dt != null ? dt.getExecution().encodeURL(
				_src != null ? _src: "~./img/spacer.gif"):  "";
	}

	/** Sets the content directly.
	 * Default: null.
	 *
	 * @param media the media for this inline frame.
	 * If not null, it has higher priority than {@link #getSrc}.
	 */
	public void setContent(Media media) {
		if (media != _media) {
			_media = RepeatableMedia.getInstance(media);
				//Use RepeatableMedia since it might be reloaded
				//if the component is invalidated or overlapped wnd (Bug 1896797)
			if (_media != null) ++_medver; //enforce browser to reload
			smartUpdateDeferred("src", new EncodedSrc()); //Bug 1850895
		}
	}
	/** Returns the content set by {@link #setContent}.
	 * <p>Note: it won't fetch what is set thru by {@link #setSrc}.
	 * It simply returns what is passed to {@link #setContent}.
	 */
	public Media getContent() {
		return _media;
	}

	/** Returns the encoded URL for the current media content.
	 * Don't call this method unless _media is not null;
	 */
	private String getMediaSrc() {
		return Utils.getDynamicMediaURI(
			this, _medver, _media.getName(), _media.getFormat());
	}

	//-- super --//
	public String getOuterAttrs() {
		final StringBuffer sb =
			new StringBuffer(64).append(super.getOuterAttrs())
			.append(" frameborder=\"0\"");
			//frameborder is default to 0
			//User has to use style to customize the border

		HTMLs.appendAttribute(sb, "align", _align);
		HTMLs.appendAttribute(sb, "name", _name);
		HTMLs.appendAttribute(sb, "src", getEncodedSrc());

		if (!"auto".equals(_scrolling))
			HTMLs.appendAttribute(sb, "scrolling", 
				"true".equals(_scrolling) ? "yes":
				"false".equals(_scrolling) ? "no": _scrolling);
		if (_autohide)
			HTMLs.appendAttribute(sb, "z.autohide", _autohide);
		return sb.toString();
	}

	//-- Component --//
	/** Default: not childable.
	 */
	public boolean isChildable() {
		return false;
	}

	//-- ComponentCtrl --//
	protected Object newExtraCtrl() {
		return new ExtraCtrl();
	}
	/** A utility class to implement {@link #getExtraCtrl}.
	 * It is used only by component developers.
	 */
	protected class ExtraCtrl extends XulElement.ExtraCtrl
	implements DynamicMedia {
		//-- DynamicMedia --//
		public Media getMedia(String pathInfo) {
			return _media;
		}
	}

	private class EncodedSrc implements org.zkoss.zk.ui.util.DeferredValue {
		public String getValue() {
			return getEncodedSrc();
		}
	}
}
