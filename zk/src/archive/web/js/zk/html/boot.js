/* boot.js

{{IS_NOTE
	Purpose:
		Bootstrap JavaScript
	Description:
		
	History:
		Sun Jan 29 11:43:45     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
//zk//
if (!window.zk) { //avoid eval twice
zk = {};
zk.build = "3V"; //increase this if we want the browser to reload JavaScript

/** Browser info. */
zk.agent = navigator.userAgent.toLowerCase();
zk.safari = zk.agent.indexOf("safari") != -1;
zk.opera = zk.agent.indexOf("opera") != -1;
zk.ie = zk.agent.indexOf("msie") != -1 && !zk.opera;
zk.ie7 = zk.agent.indexOf("msie 7") != -1;
zk.gecko = zk.agent.indexOf("gecko/") != -1 && !zk.safari && !zk.opera;
zk.windows = zk.agent.indexOf("windows") != -1;
zk.macintosh = zk.agent.indexOf("macintosh") != -1;

/** Listen an event.
 * Why not to use prototype's Event.observe? Performance.
 */
zk.listen = function (el, evtnm, fn) {
	if (el.addEventListener)
		el.addEventListener(evtnm, fn, false);
	else /*if (el.attachEvent)*/
		el.attachEvent('on' + evtnm, fn);
};
/** Un-listen an event.
 */
zk.unlisten = function (el, evtnm, fn) {
	if (el.removeEventListener)
		el.removeEventListener(evtnm, fn, false);
	else if (el.detachEvent) {
		try {
			el.detachEvent('on' + evtnm, fn);
		} catch (e) {
		}
	}
};
/** disable ESC to prevent user from pressing ESC to stop loading */
zk.disableESC = function () {
	if (!zk._noESC) {
		zk._noESC = function (evt) {
			if (!evt) evt = window.event;
			if (evt.keyCode == 27) {
				if (evt.preventDefault) {
					evt.preventDefault();
					evt.stopPropagation();
				} else {
					evt.returnValue = false;
					evt.cancelBubble = true;
				}
				return false;//eat
			}
			return true;
		};
		zk.listen(document, "keydown", zk._noESC);

		//FUTURE: onerror not working in Safari and Opera
		//if error occurs, loading will be never ended, so try to ignore
		//we cannot use zk.listen. reason: no way to get back msg...(FF)
		zk._oldOnErr = window.onerror;
		zk._onErrChanged = true;
		window.onerror =
	function (msg, url, lineno) {
		//We display errors only for local class web resource
		//It is annoying to show error if google analytics's js not found
		if (url.indexOf(location.host) >= 0) {
			var v = zk_action.lastIndexOf(';');
			var v = v >= 0 ? zk_action.substring(0, v): zk_action;
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
};
zk.disableESC(); //disable it as soon as possible
/** Enables ESC to back to the normal mode. */
zk.enableESC = function () {
	if (zk._noESC) {
		zk.unlisten(document, "keydown", zk._noESC);
		delete zk._noESC;
	}
	if (zk._onErrChanged) {
		window.onerror = zk._oldOnErr;
		if (zk._oldOnErr) delete zk._oldOnErr;
		delete zk._onErrChanged;
	}
};

//////////////////////////////////////
zk.mods = {}; //ZkFns depends on it

/** Note: it is easy to cause problem with EMBED, if we use prototype's $() since
 * it tried to extend the element.
 */
function $e(id) {
    return typeof id == 'string' ? document.getElementById(id): id;
};
/** A control might be enclosed by other tag while event is sent from
 * the control directly, so... */
function $uuid(n) {
	if (typeof n != 'string') {
		while (n) {
			if (n.id) {
				n = n.id;
				break;
			}
			if (zk.gecko) { 
				var p = $e(getZKAttr(n, "vparent"));
				if (p) {
					n = p;
					continue;
				}
			}
			n = n.parentNode;
		}
	}
	if (!n) return "";
	var j = n.lastIndexOf('!');
	return j > 0 ? n.substring(0, j): n;
}
/** Returns the real element (ends with !real).
 * If a component's attributes are located in the inner tag, i.e.,
 * you have to surround it with span or other tag, you have to place
 * uuid!real on the inner tag
 *
 * Note: !chdextr is put by the parent as the exterior of its children,
 * while !real is by the component itself
 */
function $real(cmp) {
	var id = $uuid(cmp);
	if (id) {
		var n = $e(id + "!real");
		if (n) return n;
		n = $e(id);
		if (n) return n;
	}
	return cmp;
}
/** Returns the enclosing element (not ends with !real).
 * If not found, cmp is returned.
 */
function $outer(cmp) {
	var id = $uuid(cmp);
	if (id) {
		var n = $e(id);
		if (n) return n;
	}
	return cmp;
}
/** Returns the type of a node without module. */
function $type(n) {
	var type = getZKAttr(n, "type");
	if (type) {
		var j = type.lastIndexOf('.');
		return j >= 0 ? type.substring(j + 1): type;
	}
	return null;
};
/** Returns the peer (xxx!real => xxx, xxx => xxx!real), or null if n/a.
 */
/*function $peer(id) {
	return id ? $e(
		id.endsWith("!real") ? id.substring(0, id.length-5): id+"!real"): null;
}*/
/** Returns the exterior of the specified component (ends with !chdextr).
 * Some components, hbox nad vbox, need to add exterior to child compoents,
 * and the exterior is named with "uuid!chdextr".
 */
function $childExterior(cmp) {
	var n = $e(cmp.id + "!chdextr");
	return n ? n: cmp;
}

/** Returns the nearest parent element, including el itself, with the specified type.
 */
function $parentByType(el, type) {
	while (el) {
		if ($type(el) == type)
			return el;
		if (zk.gecko) { 
			var p = $e(getZKAttr(el, "vparent"));
			if (p) {
				el = p;
				continue;
			}
		}
		el = el.parentNode;
	}
	return null;
};
/** Returns the tag name in the upper case. */
function $tag(el) {
	return el && el.tagName ? el.tagName.toUpperCase(): "";
};
/** Returns the nearest parent element, including el itself, with the specified type.
 */
function $parentByTag(el, tagName) {
	while (el) {
		if ($tag(el) == tagName)
			return el;
		if (zk.gecko) { 
			var p = $e(getZKAttr(el, "vparent"));
			if (p) {
				el = p;
				continue;
			}
		}
		el = el.parentNode;
	}
	return null;
};

/** Returns the ZK attribute of the specified name.
 * Note: the name space of ZK attributes is "http://www.zkoss.org/2005/zk"
 */
function getZKAttr(el, nm) {
	//20061120:
	//1) getAttributeNS doesn't work properly to retrieve attribute back
	//2) setAttribute("z:nm", val) doesn't work in Safari
	try {
		return el && el.getAttribute ? el.getAttribute("z." + nm): null;
	} catch (e) {
		return null; //IE6: failed if el is TABLE and attribute not there
	}
};
/** Sets the ZK attribute of the specified name with the specified value.
 */
function setZKAttr(el, nm, val) {
	if (el && el.setAttribute) el.setAttribute("z." + nm, val);
};
function rmZKAttr(el, nm) {
	if (el && el.removeAttribute) el.removeAttribute("z." + nm);
	else setZKAttr(el, nm, "");
};

/** Returns the version of the specified module name.
 */
zk.getBuild = function (nm) {
	return zk.mods[nm] || zk.build;
};

/** Adds a function for module initialization.
 * Note: JS are loaded concurrently, so module initializations
 * must take place after all modules are loaded.
 */
zk.addModuleInit = function (fn) {
	zk._initmods.push(fn);
};
/** Adds a component that must be initialized after
 * all modules are loaded and initialized.
 */
zk.addInitCmp = function (cmp) {
	zk._initcmps.push(cmp);
};
/** Adds a function that will be invoked after the document is loaded.
 */
zk.addInit = function (fn) {
	zk._initfns.push(fn);
};

/** Loads the specified module (JS). If a feature is called "a.b.c", then
 * zk_action + "/web/js" + "a/b/c.js" is loaded.
 *
 * <p>To load a JS file that other modules don't depend on, use zk.loadJS.
 *
 * @param nm the module name if no / is specified, or filename if / is
 * specified, or URL if :// is specified.
 * @param initfn the function that will be added to zk.addModuleInit
 * @param ckfn used ONLY if URL (i.e., xxx://) is used as nm,
 * and the file being loaded doesn't invoke zk.ald().
 * @param modver the version of the module, or null to use zk.getBuild(nm)
 */
zk.load = function (nm, initfn, ckfn, modver) {
	if (!nm) {
		zk.error("Module name must be specified");
		return;
	}

	if (!zk._modules[nm]) {
		zk._modules[nm] = true;
		if (initfn) zk.addModuleInit(initfn);
		zk._load(nm, modver);
		if (ckfn) zk._ckfns.push(ckfn);
	}
};
/** Loads the required module for the specified component.
 * Note: it DOES NOT check any of its children.
 * @return true if z.type is defined.
 */
zk.loadByType = function (n) {
	var type = getZKAttr(n, "type");
	if (type) {
		var j = type.lastIndexOf('.');
		if (j > 0) zk.load(type.substring(0, j));
		return true;
	}
	return false;
}

/** Loads the javascript. It invokes _bld before loading.
 *
 * <p>The JavaScript file being loaded must<br/>
 * 1) call zk.ald() after loaded<br/>
 * 2) pass ckfn to test whether it is loaded.
 */
zk._load = function (nm, modver) {
	zk._bld();

	var e = document.createElement("script");
	e.type = "text/javascript" ;

	var zcb;
	if (zk.gecko) {
		e.onload = zk.ald;
		zcb = "";
	} else {
		zcb = "/_zcbzk.ald";
			//Note: we use /_zcb to enforce callback of zk.ald
	}

	var uri = nm;
	if (uri.indexOf("://") > 0) {
		e.src = uri;
	} else if (uri.indexOf('/') >= 0) {
		if (uri.charAt(0) != '/') uri = '/' + uri;
		e.charset = "UTF-8";
		e.src = zk.getUpdateURI("/web" + zcb + uri, false, modver);
	} else { //module name
		uri = uri.replace(/\./g, '/') + ".js";
		if (uri.charAt(0) != '/') uri = '/' + uri;
		e.charset = "UTF-8";
		if (!modver) modver = zk.getBuild(nm);
		e.src = zk.getUpdateURI("/web" + zcb + "/js" + uri, false, modver);
	}
	document.getElementsByTagName("HEAD")[0].appendChild(e);
};
/** before load. */
zk._bld = function () {
	if (zk.loading ++) {
		zk._updCnt();
	} else {
		zk.disableESC();

		zk._ckload = setInterval(function () {
			for (var j = 0; j < zk._ckfns.length; ++j)
				if (zk._ckfns[j]()) {
					zk._ckfns.splice(j--, 1);
					zk.ald();
				} else return; //wait a while
		}, 10);

		setTimeout(function () {
			if (zk.loading) {
				var n = $e("zk_loadprog");
				if (!n) zk._newProgDlg("zk_loadprog",
					'Loading (<span id="zk_loadcnt">'+zk.loading+'</span>)', 20, 20);
			}
		}, 1500);
	}
};
/** after load. */
zk.ald = function () {
	if (--zk.loading) {
		try {
			zk._updCnt();
		} catch (ex) {
			zk.error("Failed to count. "+ex.message);
		}
	} else {
		try {
			zk.enableESC();

			if (zk._ckload) {
				clearInterval(zk._ckload);
				delete zk._ckload;
			}

			var n = $e("zk_loadprog");
			if (n) n.parentNode.removeChild(n);
		} catch (ex) {
			zk.error("Failed to stop counting. "+ex.message);
		}
		
		if (zk._ready) zk._evalInit(); //zk._loadAndInit mihgt not finish
	}
};
zk._updCnt = function () {
	var n = $e("zk_loadcnt");
	if (n) n.innerHTML = "" + zk.loading;
};

/** Initializes the dom tree.
 */
zk.initAt = function (node) {
	if (!node) return;

	var stk = new Array();
	stk.push(node);
	zk._loadAndInit({stk: stk, nosibling: true});
};

/** Loads all required module and initializes components. */
zk._loadAndInit = function (inf) {
	zk._ready = false;

	//The algorithm here is to mimic deep-first tree traversal
	//We cannot use recursive algorithm because it might take too much time
	//to execute and browser will alert users for aborting!
	for (var j = 0; inf.stk.length;) {
		if (++j > 3000) {
			setTimeout(function() {zk._loadAndInit(inf);}, 0);
			return; //let browser breath
		}

		var n = inf.stk.pop();

	//FF remembers the previous value that user entered when reload
	//We have to reset them because the server doesn't know any of them
		if (zk.gecko) {
			switch ($tag(n)) {
			case "INPUT":
				if (n.type == "checkbox" || n.type == "radio") {
					if (n.checked != n.defaultChecked)
						n.checked = n.defaultChecked;
					break;
				}
				if (n.type != "text" && n.type != "password") break;
				//fall thru
			case "TEXTAREA":
				if (n.value != n.defaultValue
				&& n.defaultValue != "zk_wrong!~-.zk_pha!6")
					n.value = n.defaultValue;
				break;
			case "OPTION":
				if (n.selected != n.defaultSelected)
					n.selected = n.defaultSelected;
				break;
			}
		} else if (zk.ie && $tag(n) == 'A' && n.href.indexOf("javascript:") >= 0) {
			//Fix bug 1635685 and 1612312
			zk.listen(n, "click", zk._fixcc);
		}

		var v = getZKAttr(n, "dtid");
		if (v) zkau.addDesktop(v); //desktop's ID found

		if (zk.loadByType(n) || getZKAttr(n, "drag")
		|| getZKAttr(n, "drop") || getZKAttr(n, "zid"))
			zk._initcmps.push(n);

		//if nosibling, don't process its sibling (only process children)
		if (inf.nosibling) inf.nosibling = false;
		else if (n.nextSibling) inf.stk.push(n.nextSibling);
		if (n.firstChild) inf.stk.push(n.firstChild);
	}

	zk._evalInit();
	zk._ready = true;
};
/** Fix bug 1635685 and 1612312 (IE/IE7 only):
 * IE invokes onbeforeunload if <a href="javascript;"> is clicked (sometimes)
 * It actually ignores zkau.confirmClose temporary.
 */
if (zk.ie) {
	zk._fixcc = function () {
		var msg = zkau.confirmClose;
		if (msg) {
			zkau.confirmClose = null; 
			setTimeout(function () {zkau.confirmClose = msg;}, 0); //restore
		}
	};
}

/** Initial components and init functions. */
zk._evalInit = function () {
	while (!zk.loading && zk._initmods.length)
		(zk._initmods.shift())();

	//Note: if loading, zk._doLoad will execute zk._evalInit after finish
	for (var j = 0; zk._initcmps.length && !zk.loading;) {
		var n = zk._initcmps.pop(); //reverse-order

		var m = zk.eval(n, "init");
		if (m) n = m; //it might be transformed

		if (getZKAttr(n, "zid")) zkau.initzid(n);
		if (getZKAttr(n, "drag")) zkau.initdrag(n);
		if (getZKAttr(n, "drop")) zkau.initdrop(n);

		var type = $type(n);
		if (type) {
			var o = window["zk" + type];
			if (o) {
				if (o["onVisi"]) zk._visicmps[n.id] = true;
				if (o["onHide"]) zk._hidecmps[n.id] = true;
				if (o["onSize"]) zk._sizecmps[n.id] = true;
			}
		}

		if (++j > 3000 || zk.loading) {
			if (!zk.loading)
				setTimeout(zk._evalInit, 0); //let browser breath
			return;
		}
	}

	while (!zk.loading && zk._initfns.length)
		(zk._initfns.shift())();
};
/** Evaluate a method of the specified component.
 *
 * It assumes fn is a method name of a object called "zk" + type
 * (in window).
 * Nothing happens if no such object or no such method.
 *
 * @param n the component
 * @param fn the method name, e.g., "init"
 * @param type the component type. If omitted, $type(n)
 * is assumed.
 * @param a0 the first of extra arguments; null to omitted
 * @return the result
 */
zk.eval = function (n, fn, type, a0, a1, a2, a3, a4, a5, a6, a7) {
	if (!type) type = $type(n);
	if (type) {
		var o = window["zk" + type];
		if (o) {
			var f = o[fn];
			if (f) {
				try {
					return f(n,a0,a1,a2,a3,a4,a5,a6,a7);
				} catch (ex) {
					zk.error("Failed to invoke zk"+type+"."+fn+"\n"+ex.message);
				}
			}
		}
	}
	return false;
};

/** Check z.type and invoke zkxxx.cleanup if declared.
 */
zk.cleanupAt = function (n) {
	if (getZKAttr(n, "zid")) zkau.cleanzid(n);
	if (getZKAttr(n, "zidsp")) zkau.cleanzidsp(n);
	if (getZKAttr(n, "drag")) zkau.cleandrag(n);
	if (getZKAttr(n, "drop")) zkau.cleandrop(n);

	var type = $type(n);
	if (type) {
		zk.eval(n, "cleanup", type);
		zkau.cleanupMeta(n); //note: it is called only if type is defined
		delete zk._visicmps[n.id];
		delete zk._hidecmps[n.id];
		delete zk._sizecmps[n.id];
	}

	for (n = n.firstChild; n; n = n.nextSibling)
		zk.cleanupAt(n); //recursive for child component
};

/** To notify a component that it becomes visible because one its ancestors
 * becomes visible. All descendants of n is invoked if onVisi is declared.
 */
zk.onVisiAt = function (n) {
	for (nid in zk._visicmps) {
		var elm = $e(nid);
		for (var e = elm; e; e = e.parentNode) {
			if (e == n) { //elm is a child of n
				zk.eval(elm, "onVisi");
				break;
			}
			if (e.style && e.style.display == "none")
				break;
		}
	}
};
/** To notify a component that it becomes invisible because one its ancestors
 * becomes invisible. All descendants of n is invoked if onHide is declared.
 */
zk.onHideAt = function (n) {
	//Bug 1526542: we have to blur if we want to hide a focused control in gecko and IE
	var f = zkau.currentFocus;
	if (f && zk.isAncestor(n, f)) {
		zkau.currentFocus = null;
		try {f.blur();} catch (e) {}
	}

	for (nid in zk._hidecmps) {
		var elm = $e(nid);
		for (var e = elm; e; e = e.parentNode) {
			if (e == n) { //elm is a child of n
				zk.eval(elm, "onHide");
				break;
			}
			if (e.style && e.style.display == "none") //yes, ignore hidden ones
				break;
		}
	}
}
/** To notify a component that its size is changed.
 *  All descendants of n is invoked if onSize is declared.
 * Note: since onSize usually triggers another onSize, it handles them
 * asynchronously.
 */
zk.onSizeAt = function (n) {
	for (nid in zk._sizecmps) {
		var elm = $e(nid);
		for (var e = elm; e; e = e.parentNode) {
			if (e == n) { //elm is a child of n
				if (!zk._tmOnSizeAt)
					zk._tmOnSizeAt = setTimeout(zk._onSizeAt, 550);
				zk._toOnSize[nid] = true;
				break;
			}
			if (e.style && e.style.display == "none")
				break;
		}
	}
};
zk._onSizeAt = function () {
	delete zk._tmOnSizeAt;
	var once;
	for (nid in zk._toOnSize) {
		if (once) {
			if (!zk._tmOnSizeAt)
				zk._tmOnSizeAt = setTimeout(zk._onSizeAt, 0);
			break; //only once
		}
		once = true;

		delete zk._toOnSize[nid];

		var elm = $e(nid);
		if (elm) zk.eval(elm, "onSize");
	}
};

//extra//
/** Loads the specified style sheet (CSS).
 * @param uri Example, "/a/b.css". It will be prefixed with zk_action + "/web",
 * unless http:// or https:// is specified
 */
zk.loadCSS = function (uri) {
	var e = document.createElement("LINK");
	e.rel = "stylesheet";
	e.type = "text/css";
	if (uri.indexOf("://") < 0) {
		if (uri.charAt(0) != '/') uri = '/' + uri;
		uri = zk.getUpdateURI("/web" + uri);
	}
	e.href = uri;
	document.getElementsByTagName("HEAD")[0].appendChild(e);
};
/** Loads the specified JavaScript file directly.
 * @param uri Example, "/a/b.css". It will be prefixed with zk_action + "/web",
 * unless http:// or https:// is specified
 * @param fn the function to execute after loading. It is optional.
 * Not function under safari
 */
zk.loadJS = function (uri, fn) {
	var e = document.createElement("script");
	e.type	= "text/javascript" ;
	e.charset = "UTF-8";
	if (fn)
		e.onload = e.onreadystatechange = function() {
			if (!e.readyState || e.readyState == 'loaded') fn();
		};

	if (uri.indexOf("://") < 0) {
		if (uri.charAt(0) != '/') uri = '/' + uri;
		uri = zk.getUpdateURI("/web" + uri);
	}
	e.src = uri;
	document.getElementsByTagName("HEAD")[0].appendChild(e);
};

/** Returns the proper URI.
 * @param ignoreSessId whether not to append session ID.
 * @param modver the module version to insert into uri, or null to use zk.build.
 * Note: modver is used if uri starts with /uri
 */
zk.getUpdateURI = function (uri, ignoreSessId, modver) {
	if (!uri) return zk_action;

	if (uri.charAt(0) != '/') uri = '/' + uri;
	if (uri.length >= 5 && uri.substring(0, 5) == "/web/")
		uri = "/web/_zver" + modver + uri.substring(4);

	var j = zk_action.lastIndexOf(';'), k = zk_action.lastIndexOf('?');
	if (j < 0 && k < 0) return zk_action + uri;

	if (k >= 0 && (j < 0 || k < j)) j = k;
	uri = zk_action.substring(0, j) + uri;
	return ignoreSessId ? uri: uri + zk_action.substring(j);
};

//-- progress --//
/** Turn on the progressing dialog after the specified timeout. */
zk.progress = function (timeout) {
	zk.progressing = true;
	if (timeout > 0) setTimeout(zk._progress, timeout);
	else zk._progress();
};
zk.progressDone = function() {
	zk.progressing = zk.progressPrompted = false;
	var n = $e("zk_prog");
	if (n) n.parentNode.removeChild(n);
};
/** Generates the progressing dialog. */
zk._progress = function () {
	if (zk.progressing) {
		var n = $e("zk_prog");
		if (!n) {
			var msg;
			try {msg = mesg.PLEASE_WAIT;} catch (e) {msg = "Processing...";}
				//when the first boot, mesg is not ready

			n = zk._newProgDlg("zk_prog", msg, 0, zk.innerY());
			if (n) {
				var left = zk.innerWidth() - n.offsetWidth - 20;
				if (left < 0) left = 0;
				n.style.left = left + "px";
			}
			zk.progressPrompted = true;
		}
	}
};
zk._newProgDlg = function (id, msg, x, y) {
	var n = document.createElement("DIV");
	document.body.appendChild(n);

	var html = '<div id="'+id+'" style="left:'+x+'px;top:'+y+'px;'
	+'position:absolute;z-index:79000;background-color:#FFF0C8;'
	+'white-space:nowrap;border:1px solid #77a;padding:6px;">'
	+'<img alt="." src="'+zk.getUpdateURI('/web/zk/img/progress.gif')+'"/> '
	+msg+'</div>';

	zk._setOuterHTML(n, html);
	return $e(id);
};

//-- utilities --//
zk.https = function () {
	var p = location.protocol;
	return p && "https:" == p.toLowerCase();
};
/** Returns the x coordination of the visible part. */
zk.innerX = function () {
    return window.pageXOffset
		|| document.documentElement.scrollLeft
		|| document.body.scrollLeft || 0;
};
/** Returns the y coordination of the visible part. */
zk.innerY = function () {
    return window.pageYOffset
		|| document.documentElement.scrollTop
		|| document.body.scrollTop || 0;
};

zk.innerWidth = function () {
	return typeof window.innerWidth == "number" ? window.innerWidth:
		document.compatMode == "CSS1Compat" ?
			document.documentElement.clientWidth: document.body.clientWidth;
};
zk.innerHeight = function () {
	return typeof window.innerHeight == "number" ? window.innerHeight:
		document.compatMode == "CSS1Compat" ?
			document.documentElement.clientHeight: document.body.clientHeight;
};

zk._setOuterHTML = function (n, html) {
	if (n.outerHTML) n.outerHTML = html;
	else { //non-IE
		var range = document.createRange();
		range.setStartBefore(n);
		var df = range.createContextualFragment(html);
		n.parentNode.replaceChild(df, n);
	}
};

/** Pause milliseconds. */
zk.pause = function (millis) {
	if (millis) {
		var d = new Date(), n;
		do {
			n = new Date();
		} while (n - d < millis);
	}
};

//-- HTML/XML --//
zk.encodeXML = function (txt, multiline) {
	var out = "";
	if (txt)
		for (var j = 0; j < txt.length; ++j) {
			var cc = txt.charAt(j);
			switch (cc) {
			case '<': out += "&lt;"; break;
			case '>': out += "&gt;"; break;
			case '&': out += "&amp;"; break;
			case '\n':
				if (multiline) {
					out += "<br/>";
					break;
				}
			default:
				out += cc;
			}
		}
	return out
};

//-- debug --//
/** Generates a message for debugging. */
zk.message = function (msg) {
	zk._msg = zk._msg ? zk._msg + msg: msg;
	zk._msg +=  '\n';
	setTimeout(zk._domsg, 600);
		//for better performance and less side effect, execute later
};
zk._domsg = function () {
	if (zk._msg) {
		var console = $e("zk_msg");
		if (!console) {
			console = document.createElement("DIV");
			document.body.appendChild(console);
			var html =
 '<div style="border:1px solid #77c">'
+'<table cellpadding="0" cellspacing="0" width="100%"><tr>'
+'<td width="20pt"><button onclick="zk._msgclose(this)">close</button><br/>'
+'<button onclick="$e(\'zk_msg\').value = \'\'">clear</button></td>'
+'<td><textarea id="zk_msg" style="width:100%" rows="3"></textarea></td></tr></table></div>';
			zk._setOuterHTML(console, html);
			console = $e("zk_msg");
		}
		console.value = console.value + zk._msg + '\n';
		zk._msg = null;
	}
};
zk._msgclose = function (n) {
	while ((n = n.parentNode) != null)
		if ($tag(n) == "DIV") {
			n.parentNode.removeChild(n);
			return;
		}
};
//FUTURE: developer could control whether to turn on/off
zk.debug = zk.message;

/** Error message must be a popup. */
zk.error = function (msg) {
	if (!zk._errcnt) zk._errcnt = 1;
	var id = "zk_err_" + zk._errcnt++;
	var box = document.createElement("DIV");
	document.body.appendChild(box);
	var html =
 '<div style="position:absolute;z-index:99000;padding:3px;left:'
+(zk.innerX()+50)+'px;top:'+(zk.innerY()+20)
+'px;width:550px;border:1px solid #963;background-color:#fc9" id="'
+id+'"><table cellpadding="2" cellspacing="2" width="100%"><tr valign="top">'
+'<td width="20pt"><button onclick="zk._msgclose(this)">close</button></td>'
+'<td style="border:1px inset">'+zk.encodeXML(msg, true) //Bug 1463668: security
+'</td></tr></table></div>';
	zk._setOuterHTML(box, html);
	box = $e(id); //we have to retrieve back

	try {
		new Draggable(box, {
			handle: box, zindex: box.style.zIndex,
			starteffect: zk.voidf, starteffect: zk.voidf, endeffect: zk.voidf});
	} catch (e) {
	}
};

//-- bootstrapping --//
zk.loading = 0;
zk._modules = {}; //Map(String nm, boolean loaded)
zk._initfns = new Array(); //used by addInit
zk._initmods = new Array(); //used by addModuleInit
zk._initcmps = new Array(); //comps to init
zk._ckfns = new Array(); //functions called to check whether a module is loaded (zk._load)
zk._visicmps = {}; //a set of component's ID that requires zkType.onVisi
zk._hidecmps = {}; //a set of component's ID that requires zkType.onHide
zk._sizecmps = {}; //a set of component's ID that requires zkType.onSize
zk._toOnSize = {}; //a set of component's waiting to execute onSize

function myload() {
	var f = zk._onload;
	if (f) {
		zk._onload = null; //called only once
		f();
	}
}
zk._onload = function () {
	//It is possible to move javascript defined in zul's language.xml
	//However, IE has bug to order JavaScript properly if zk._load is used
	zk.progress(600);
	zk.addInit(zk.progressDone);
	zk.initAt(document.body);
};

//Source: http://dean.edwards.name/weblog/2006/06/again/
if (zk.ie && !zk.https()) {
	//IE consider the following <script> insecure, so skip is https
	document.write('<script id="_zie_load" defer src="javascript:void(0)"><\/script>');
	var e = $e("_zie_load");
	e.onreadystatechange = function() {
		if ("complete" == this.readyState) //don't check loaded!
	        myload(); // call the onload handler
	};
	e.onreadystatechange();
} else if (zk.safari) {
    var timer = setInterval(function() {
		if (/loaded|complete/.test(document.readyState)) {
			clearInterval(timer);
			delete timer;
			myload();
		}
	}, 10);
} else {
	//Bug 1619959: FF not always fire DOMContentLoaded (such as in 2nd iframe),
	//so we have to use onload in addition to register DOMContentLoaded
	if (zk.gecko)
		zk.listen(document, "DOMContentLoaded", myload)

	zk._oldOnload = window.onload;
	window.onload = function () {
		myload();
		if (zk._oldOnload)
			zk._oldOnload.apply(window, arguments);
	}
}

} //if (!window.zk)
