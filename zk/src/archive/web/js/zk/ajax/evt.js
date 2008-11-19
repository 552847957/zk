/* domevt.js

	Purpose:
		DOM Event, ZK Event and ZK Watch
	Description:
		
	History:
		Thu Oct 23 10:53:17     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
*/
/** DOM Event Utilities. */
zEvt = {
	BS:		8,
	TAB:	9,
	ENTER:	13,
	SHIFT:	16,
	CTRL:	17,
	ALT:	18,
	ESC:	27,
	LFT:	37,
	UP:		38,
	RGH:	39,
	DN:		40,
	INS:	45,
	DEL:	46,
	HOME:	36,
	END:	35,
	PGUP:	33,
	PGDN:	34,
	F1:		112,

	/** Returns the target element of the event. */
	target: function(evt) {
		if (!evt) evt = window.evt;
		return evt.target || evt.srcElement;
	},
	/** Returns the target widget (zk.Widget) of the event. */
	widget: function (evt) {
		for (var n = zEvt.target(evt), w; n; n = n.parentNode) {
			w = n.z_wgt;
			if (w) return w;

			w = n.id;
			if (w) {
				w = zDom.$(zk.Widget.uuid(w));
				if (w) {
					w = w.z_wgt;
					if (w) return w;
				}
			}
		}
		return null;
	},
	/** Stops the event propogation. */
	stop: function(evt) {
		if (!evt) evt = window.evt;
		if (evt.preventDefault) {
			evt.preventDefault();
			evt.stopPropagation();
		} else {
			evt.returnValue = false;
			evt.cancelBubble = true;
			if (!evt.shiftKey && !evt.ctrlKey)
				evt.keyCode = 0; //Bug 1834891
		}
	},

	//Mouse Info//
	/** Returns if it is the left click. */
	leftClick: function(evt) {
		if (!evt) evt = window.evt;
		return evt.which == 1 || evt.button == 0 || evt.button == 1;
	},
	/** Returns the mouse status.
	 */
	mouseData: function (evt, target) {
		if (!evt) evt = window.evt;
		var extra = "";
		if (evt.altKey) extra += "a";
		if (evt.ctrlKey) extra += "c";
		if (evt.shiftKey) extra += "s";

		var ofs = zDom.cmOffset(target ? target: zEvt.target(evt));
		var x = zEvt.x(evt) - ofs[0];
		var y = zEvt.y(evt) - ofs[1];
		return [x, y, extra];
	},
	/** Returns the X coordinate of the mouse pointer. */
	x: function (evt) {
		if (!evt) evt = window.evt;
		return evt.pageX || (evt.clientX +
			(document.documentElement.scrollLeft || document.body.scrollLeft));
  	},
	/** Returns the Y coordinate of the mouse pointer. */
	y: function(evt) {
		if (!evt) evt = window.evt;
		return evt.pageY || (evt.clientY +
			(document.documentElement.scrollTop || document.body.scrollTop));
	},

	//Key Info//
	/** Returns the char code. */
	charCode: function(evt) {
		if (!evt) evt = window.evt;
		return evt.charCode || evt.keyCode;
	},
	/** Returns the key code. */
	keyCode: function(evt) {
		if (!evt) evt = window.evt;
		var k = evt.keyCode || evt.charCode;
		return zk.safari ? (this.safariKeys[k] || k) : k;
	},

	/** Listens a browser event.
	 */
	listen: function (el, evtnm, fn) {
		if (el.addEventListener)
			el.addEventListener(evtnm, fn, false);
		else /*if (el.attachEvent)*/
			el.attachEvent('on' + evtnm, fn);

		//Bug 1811352
		if ("submit" == evtnm && zDom.tag(el) == "FORM") {
			if (!el._$submfns) el._$submfns = [];
			el._$submfns.push(fn);
		}
	},
	/** Un-listens a browser event.
	 */
	unlisten: function (el, evtnm, fn) {
		if (el.removeEventListener)
			el.removeEventListener(evtnm, fn, false);
		else if (el.detachEvent) {
			try {
				el.detachEvent('on' + evtnm, fn);
			} catch (e) {
			}
		}

		//Bug 1811352
		if ("submit" == evtnm && zDom.tag(el) == "FORM" && el._$submfns)
			el._$submfns.$remove(fn);
	},

	/** Enables ESC (default behavior). */
	enableESC: function () {
		if (zDom._noESC) {
			zEvt.unlisten(document, "keydown", zDom._noESC);
			delete zDom._noESC;
		}
		if (zDom._onErrChange) {
			window.onerror = zDom._oldOnErr;
			if (zDom._oldOnErr) delete zDom._oldOnErr;
			delete zDom._onErrChange;
		}
	},
	/** Disables ESC (so loading won't be aborted). */
	disableESC: function () {
		if (!zDom._noESC) {
			zDom._noESC = function (evt) {
				if (!evt) evt = window.event;
				if (evt.keyCode == 27) {
					zEvt.stop(evt);
					return false;//eat
				}
				return true;
			};
			zEvt.listen(document, "keydown", zDom._noESC);

			//FUTURE: onerror not working in Safari and Opera
			//if error occurs, loading will be never ended, so try to ignore
			//we cannot use zEvt.listen. reason: no way to get back msg...(FF)
			zDom._oldOnErr = window.onerror;
			zDom._onErrChange = true;
			window.onerror =
	function (msg, url, lineno) {
		//We display errors only for local class web resource
		//It is annoying to show error if google analytics's js not found
		var au = zAu.comURI();
		if (au && url.indexOf(location.host) >= 0) {
			var v = au.lastIndexOf(';');
			v = v >= 0 ? au.substring(0, v): au;
			if (url.indexOf(v + "/web/") >= 0) {
				msg = mesg.FAILED_TO_LOAD + url + "\n" + mesg.FAILED_TO_LOAD_DETAIL
					+ "\n" + mesg.CAUSE + msg+" (line "+lineno + ")";
				if (zk.error) zk.error(msg);
				else alert(msg);
				return true;
			}
		}
	};
		}
	}
};

/** A widget event, fired by {@link zk.Widget#fire}.
 * It is an application-level event that is used by application to
 * hook the listeners to.
 * On the other hand, a DOM event ({@link zEvt}) is the low-level event
 * listened by the implementation of a widget.
 */
zk.Event = zk.$extends(zk.Object, {
	/** The target widget. */
	//target: null,
	/** The event name. */
	//name: null,
	/** The extra data, which could be anything. */
	//data: null,
	/** Options.
	 * <dl>
	 * <dt>implicit</dt>
	 * <dd>Whether this event is an implicit event, i.e., whether it is implicit
	 * to users (so no progressing bar).</dd>
	 * <dt>ignorable</dt>
	 * <dd>Whether this event is ignorable, i.e., whether to ignore any error
	 * of sending this event back the server.
	 * An ignorable event is also an imiplicit event.</dd>
	 * <dt>ctl</dt>
	 * <dd>Whether it is a control, such as onClick, rather than
	 * a notification for status change.</dd>
	 * </dl>
	 */
	/** Whether to stop the event propogation.
	 * Note: it won't be sent to the server if stop is true.
	 */
	//stop: false,

	$init: function (target, name, data, opts) {
		this.target = target;
		this.name = name;
		this.data = typeof data == 'string' ? [data]: data ? data: null;
		this.opts = opts;
	}
});

/** An utility to manage a collection of watches.
 * A watch is any JavaScript object used to 'watch' an action, such as onSize,
 * The watch must implement a method having
 * the same as the action name.
 * For example, zAu.watch("onSend", o) where
 * o must have a method called onSend. Then, when the onSend action occurs,
 * o.onSend() will be invoked.
 *
 * <p>Note: the watches are shared by the whole client engine, so be careful
 * to avoid the conflict of action names. Here is a list of all action
 * names.
 * <dl>
 * <dt>onSend(implicit)</dt>
 * <dd>It is called before sending the AU request to the server.
 * The implicit argument indicates whether all AU requests being
 * sent are implicit.</dd>
 * </dl>
 */
zWatch = {
	/** Adds a watch.
	 * @param name the action name. Currently, it supports only onSend,
	 * which is called before sending the AU request(s).
	 * @return true if added successfully.
	 */
	watch: function (name, watch) {
		var wts = this._wts[name];
		if (!wts) wts = this._wts[name] = [];
		wts.$add(watch);
	},
	/** Removes a watch.
	 * @return whether the watch has been removed successfully.
	 * It returns false if the watch was not added before.
	 */
	unwatch: function (name, watch) {
		var wts = this._wts[name];
		return wts && wts.$remove(watch);
	},
	/** Remove all watches of the specified name.
	 */
	unwatchAll: function (name) {
		delete this._wts[name];
	},
	/** Calls all watches of the specified name.
	 * @param timeout when to call the watch. If positive or zero,
	 * setTimeout is used. Otherwise, it is called
	 */
	fire: function (name, timeout, vararg) {
		var wts = this._wts[name],
			len = wts ? wts.length: 0;
		if (len) {
			var args = [], o;
			for (var j = 2, l = arguments.length; j < l;)
				args.push(arguments[j++]);

			wts = wts.$clone(); //make a copy since unwatch might be called
			if (timeout >= 0) {
				setTimeout(
				function () {
					while (o = wts.shift())
						o[name].apply(o, args);
				}, timeout);
				return;
			}

			while (o = wts.shift())
				o[name].apply(o, args);
		}
	},
	/** Calls all descendant watches of the specified name.
	 * By descendant we mean the watch is the same or an descendant of
	 * the specified origin.
	 * <p>Note: it assumes the watch's parent can be retrieved by either
	 * the method called <code>getParent</code>, or the
	 * property called <code>parent</code>.
	 * <p>In other words, if the specified origin is not the ancestor
	 * of a watch, the watch won't be called.
	 */
	fireDown: function (name, timeout, origin, vararg) {
		var wts = this._wts[name],
			len = wts ? wts.length: 0;
		if (len) {
			var args = [];
			for (var j = 3, l = arguments.length; j < l;)
				args.push(arguments[j++]);

			var found = [], o;
			for (var j = 0; j < len;) {
				o = wts[j++];
				if (zUtl.isAncestor(origin, o))
					found.push(o);
			}

			if (timeout >= 0) {
				setTimeout(
				function () {
					while (o = found.shift())
						o[name].apply(o, args);
				}, timeout);
				return;
			}

			while (o = found.shift())
				o[name].apply(o, args);
		}
	},

	_wts: {}
};
