/* au.js

	Purpose:
		ZK Client Engine
	Description:
	
	History:
		Mon Sep 29 17:17:37     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
*/
zAu = { //static methods
	//General//
	/** Returns the URI to communicate with the server.
	 * @param uri the URI. If null, the base URI is returned.
	 * @param dt the desktop or its ID. If null, the first desktop is used.
	 * @param ignoreSessId whether to handle the session ID in the base URI
	 */
	comURI: function (uri, dt, ignoreSessId) {
		var au = zk.Desktop.$(dt).updateURI;
		if (!uri) return au;

		if (uri.charAt(0) != '/') uri = '/' + uri;

		var j = au.lastIndexOf(';'), k = au.lastIndexOf('?');
		if (j < 0 && k < 0) return au + uri;

		if (k >= 0 && (j < 0 || k < j)) j = k;
		var prefix = au.substring(0, j);

		if (ignoreSessId)
			return prefix + uri;

		var suffix = au.substring(j);
		var l = uri.indexOf('?');
		return l >= 0 ?
			k >= 0 ?
			  prefix + uri.substring(0, l) + suffix + '&' + uri.substring(l+1):
			  prefix + uri.substring(0, l) + suffix + uri.substring(l):
			prefix + uri + suffix;
	},

	//Error Handling//
	/** Confirms the user how to handle an error.
	 * Default: it shows up a message asking the user whether to retry.
	 */
	confirmRetry: function (msgCode, msg2) {
		var msg = mesg[msgCode];
		return zDom.confirm((msg?msg:msgCode)+'\n'+mesg.TRY_AGAIN+(msg2?"\n\n("+msg2+")":""));
	},
	/** Handles the error caused by processing the response.
	 * Overrides this method if you prefer to show it differently.
	 *
	 * @param msgCode the error message code.
	 * It is either an index of mesg (e.g., "FAILED_TO_PROCESS"),
	 * or an error message
	 * @param msg2 the additional message (optional)
	 * @param cmd the command (optional)
	 * @param ex the exception (optional)
	 */
	showError: function (msgCode, msg2, cmd, ex) {
		var msg = mesg[msgCode];
		zk.error((msg?msg:msgCode)+'\n'+(msg2?msg2:"")+(cmd?cmd:"")+(ex?"\n"+ex.message:""));
	},
	/** Sets the URI for an error code.
	 * If the length of  arguments is 1, then the argument must be
	 * the error code, and this method returns its URI (or null
	 * if not available).
	 * <p>If the length is larger than 1, they must be paired and
	 * the first element of the pair must be the package name,
	 * and the second is the version.
	 */
	errorURI: function (code, uri) {
		var args = arguments, len = args.length;
		if (len == 1)
			return zAu._eru['e' + code];

		if (len > 2) {
			for (var j = 0; j < len; j += 2)
				zAu.errorURI(args[j], args[j + 1]);
			return;
		}

		zAu._eru['e' + code] = uri;
	},
	_eru: {},

	////Ajax Send////
	/** Returns whether any AU request is in processing.
	 * <p>Note: zk.processing (which is a variable) represents both AU
	 * and other processing, while zAu.processing() (which is a function)
	 * represents AU only.
	 */
	processing: function () {
		return zAu._cmdsQue.length || zAu._areq || zAu._preqInf
			|| zAu._doingCmds;
	},
	/** Checks whether to turn off the progress prompt.
	 * @return true if the processing is done
	 */
	_ckProcessng: function () {
		if (zAu.processing())
			return false;
		zk.endProcessing();
		return true;
	},

	/** Sends an AU request to the server
	 * <p>Watches:
	 * <dl>
	 * <dt>onSend(implicit)</dt>
	 * <dd>It is called before sending the AU request to the server.
	 * The implicit argument indicates whether all AU requests being
	 * sent are implicit.</dd>
	 * <dt>onResponse()</dt>
	 * <dd>It is called after the response of the AU request
	 * has been sent back from the server, and processed.</dd>
	 * </dl>
	 *
	 * @param timout milliseconds.
	 * If negative, it won't be sent until next non-negative event
	 */
	send: function (aureq, timeout) {
		if (timeout < 0) {
			var opts = aureq.opts;
			if (!opts) opts = aureq.opts = {};
			opts.implicit = true;
		}

		var t = aureq.target;
		if (t) {
			zAu._send(t.type == '#d' ? t: t.desktop, aureq, timeout);
		} else {
			var dts = zk.Desktop.all;
			for (var dtid in dts)
				zAu._send(dts[dtid], aureq, timeout);
		}
	},
	/** Sends a request before any pending events.
	 * @param timout milliseconds.
	 * If undefined or negative, it won't be sent until next non-negative event
	 * Note: Unlike zAu.send, it considered undefined as not sending now
	 * (reason: backward compatible)
	 */
	sendAhead: function (aureq, timeout) {
		var t = aureq.target;
		if (t) {
			var dt = t.type == '#d' ? t: t.desktop;
			dt._aureqs.unshift(aureq);
			zAu._send2(dt, timeout);
		} else {
			var dts = zk.Desktop.all;
			for (var dtid in dts) {
				var dt = dts[dtid];
				dt._aureqs.unshift(aureq);
				zAu._send2(dt, timeout); //Spec: don't convert unefined to 0 for timeout
			}
			return;
		}
	},

	////Ajax receive////
	/** Parses a XML response and pushes the parsed commands to the queue.
	 * @return false if no command found at all
	 */
	pushXmlResp: function (dt, req) {
		var xml = req.responseXML;
		if (!xml) {
			if (zk.pfmeter) zAu.pfdone(dt, zAu._pfGetIds(req));
			return false; //invalid
		}

		var cmds = [],
			rs = xml.getElementsByTagName("r"),
			rid = xml.getElementsByTagName("rid");
		if (zk.pfmeter) {
			cmds.dt = dt;
			cmds.pfIds = zAu._pfGetIds(req);
		}

		if (rid && rid.length) {
			rid = zk.parseInt(zUtl.getElementValue(rid[0])); //response ID
			if (!isNaN(rid)) cmds.rid = rid;
		}

		for (var j = 0, rl = rs ? rs.length: 0; j < rl; ++j) {
			var cmd = rs[j].getElementsByTagName("c")[0],
				data = rs[j].getElementsByTagName("d");

			if (!cmd) {
				zk.error(mesg.ILLEGAL_RESPONSE+"Command required");
				continue;
			}

			cmds.push(cmd = {cmd: zUtl.getElementValue(cmd)});
			cmd.data = [];
			for (var k = data ? data.length: 0; --k >= 0;) {
				var d = zUtl.getElementValue(data[k]);
				switch (d.charAt(0)) {
				case 'c': case 's': d = d.substring(1); break;
				case 'n': d = null; break;
				case '1': d = true; break;
				case '0': d = false; break;
				case 'i': case 'l': case 'b':
					d = parseInt(d.substring(1)); break;
				case 'd': case 'f':
					d = parseFloat(d.substring(1)); break;
				}
				cmd.data[k] = d;
			}
		}

		zAu._cmdsQue.push(cmds);
		return true;
	},
	/** Process the response response commands.
	 */
	doCmds: function () {
		//avoid reentry since it calls loadAndInit, and loadAndInit call this
		if (zAu._doingCmds) {
			setTimeout(zAu.doCmds, 10);
		} else {
			zAu._doingCmds = true;
			try {
				zAu._doCmds0();
			} finally {
				zAu._doingCmds = false;

				if (zAu._ckProcessng())
					zAu.doneTime = zUtl.now();
			}
		}
	},
	/** Process a command.
	 */
	process: function (cmd, data) {
		//I. process commands that data[0] is not UUID
		var fn = zAu.cmd0[cmd];
		if (fn) {
			fn.apply(zAu, data);
			return;
		}

		//I. process commands that require uuid
		if (!data || !data.length) {
			zAu.showError("ILLEGAL_RESPONSE", "uuid is required for ", cmd);
			return;
		}

		fn = zAu.cmd1[cmd];
		if (fn) {
			data.splice(1, 0, zk.Widget.$(data[0])); //insert wgt
			fn.apply(zAu, data);
			return;
		}

		zAu.showError("ILLEGAL_RESPONSE", "Unknown command: ", cmd);
	},

	//ajax internal//
	_cmdsQue: [], //response commands in XML
	_seqId: 1, //1-999

	/** IE6 sometimes remains readyState==1 (reason unknown), so resend. */
	_areqTmout: function () {
		//Note: we don't resend if readyState >= 3, since the server is already
		//processing it
		var req = zAu._areq, reqInf = zAu._areqInf;
		if (req && req.readyState < 3) {
			zAu._areq = zAu._areqInf = null;
			try {
				if(typeof req.abort == "function") req.abort();
			} catch (e2) {
			}
			if (reqInf.tmout < 60000) reqInf.tmout += 3000;
				//sever might be busy, so prolong next timeout
			zAu._areqResend(reqInf);
		}
	},
	_areqResend: function (reqInf, timeout) {
		if (zAu._seqId == reqInf.sid) {//skip if the response was recived
			zAu._preqInf = reqInf; //store as a pending request info
			setTimeout(zAu._areqResend2, timeout ? timeout: 0);
		}
	},
	_areqResend2: function () {
		var reqInf = zAu._preqInf;
		if (reqInf) {
			zAu._preqInf = null;
			if (zAu._seqId == reqInf.sid)
				zAu._sendNow2(reqInf);
		}
	},
	/** Called when the response is received from _areq.
	 */
	_onRespReady: function () {
		try {
			var req = zAu._areq, reqInf = zAu._areqInf;
			if (req && req.readyState == 4) {
				zAu._areq = zAu._areqInf = null;
				if (reqInf.tfn) clearTimeout(reqInf.tfn); //stop timer

				if (zk.pfmeter) zAu.pfrecv(reqInf.dt, zAu._pfGetIds(req));

				if (zAu._revertpending) zAu._revertpending();
					//revert any pending when the first response is received

				var sid = req.getResponseHeader("ZK-SID");
				if (req.status == 200) { //correct
					if (sid && sid != zAu._seqId) {
						zAu._errcode = "ZK-SID " + (sid ? "mismatch": "required");
						return;
					} //if sid null, always process (usually for error msg)

					if (zAu.pushXmlResp(reqInf.dt, req)) { //valid response
						//advance SID to avoid receive the same response twice
						if (sid && ++zAu._seqId > 999) zAu._seqId = 1;
						zAu._areqTry = 0;
						zAu._preqInf = null;
					}
				} else if (!sid || sid == zAu._seqId) { //ignore only if out-of-seq (note: 467 w/o sid)
					zAu._errcode = req.status;
					var eru = zk.eru['e' + req.status];
					if (typeof eru == "string") {
						zUtl.go(eru);
					} else {
					//handle MSIE's buggy HTTP status codes
					//http://msdn2.microsoft.com/en-us/library/aa385465(VS.85).aspx
						switch (req.status) { //auto-retry for certain case
						default:
							if (!zAu._areqTry) break;
							//fall thru
						case 12002: //server timeout
						case 12030: //http://danweber.blogspot.com/2007/04/ie6-and-error-code-12030.html
						case 12031:
						case 12152: // Connection closed by server.
						case 12159:
						case 13030:
						case 503: //service unavailable
							if (!zAu._areqTry) zAu._areqTry = 3; //two more try
							if (--zAu._areqTry) {
								zAu._areqResend(reqInf, 200);
								return;
							}
						}

						if (!zAu._ignorable && !zAu._unloading) {
							var msg = req.statusText;
							if (zAu.confirmRetry("FAILED_TO_RESPONSE", req.status+(msg?": "+msg:""))) {
								zAu._areqTry = 2; //one more try
								zAu._areqResend(reqInf);
								return;
							}
						}

						zAu._cleanupOnFatal(zAu._ignorable);
					}
				}
			}
		} catch (e) {
			if (!window.zAu)
				return; //the doc has been unloaded

			zAu._areq = zAu._areqInf = null;
			try {
				if(req && typeof req.abort == "function") req.abort();
			} catch (e2) {
			}

			//NOTE: if connection is off and req.status is accessed,
			//Mozilla throws exception while IE returns a value
			if (!zAu._ignorable && !zAu._unloading) {
				var msg = e.message;
				zAu._errcode = "[Receive] " + msg;
				//if (e.fileName) zAu._errcode += ", "+e.fileName;
				//if (e.lineNumber) zAu._errcode += ", "+e.lineNumber;
				if (zAu.confirmRetry("FAILED_TO_RESPONSE", (msg&&msg.indexOf("NOT_AVAILABLE")<0?msg:""))) {
					zAu._areqResend(reqInf);
					return;
				}
			}
			zAu._cleanupOnFatal(zAu._ignorable);
		}

		//handle pending ajax send
		if (zAu._sendPending && !zAu._areq && !zAu._preqInf) {
			zAu._sendPending = false;
			var dts = zk.Desktop.all
			for (var dtid in dts)
				zAu._send2(dts[dtid], 0);
		}

		zAu.doCmds();
		zAu._ckProcessng();
	},

	_send: function (dt, aureq, timeout) {
		var opts = aureq.opts;
		if (opts && opts.ctl) {
			//Don't send the same request if it is in processing
			if (zAu._areqInf && zAu._areqInf.ctli == aureq.uuid
			&& zAu._areqInf.ctlc == aureq.cmd)
				return;

			var t = zUtl.now();
			if (zAu._ctli == aureq.uuid && zAu._ctlc == aureq.cmd //Bug 1797140
			&& t - zAu._ctlt < 390)
				return; //to prevent key stroke are pressed twice (quickly)

			//Note: it is still possible to queue two ctl with same uuid and cmd,
			//if the first one was not sent yet and the second one is generated
			//after 390ms.
			//However, it is rare so no handle it

			zAu._ctlt = t;
			zAu._ctli = aureq.uuid;
			zAu._ctlc = aureq.cmd;
		}

		dt._aureqs.push(aureq);

		//Note: we don't send immediately (Bug 1593674)
		//Note: Unlike sendAhead and _send2, if timeout is undefined,
		//it is considered as 0.
		zAu._send2(dt, timeout ? timeout: 0);
	},
	/** @param timeout if undefined or negative, it won't be sent. */
	_send2: function (dt, timeout) {
		if (dt && timeout >= 0)
			setTimeout(function(){zAu._sendNow(dt);}, timeout);
	},
	_sendNow: function (dt) {
		var es = dt._aureqs;
		if (es.length == 0)
			return; //nothing to do

		if (zk.loading) {
			zkPkg.afterLoad(function(){zAu._sendNow(dt);});
			return; //wait
		}

		if (zAu._areq || zAu._preqInf) { //send ajax request one by one
			zAu._sendPending = true;
			return;
		}

		//notify watches (fckez uses it to ensure its value is sent back correctly
		try {
			zWatch.fire('onSend', -1, implicit);
		} catch (e) {
			zk.error(e.message);
		}

		//bug 1721809: we cannot filter out ctl even if zAu.processing

		//decide implicit and ignorable
		var implicit = true, ignorable = true, ctli, ctlc;
		for (var j = es.length; --j >= 0;) {
			var aureq = es[j], opts = aureq.opts;
			if (implicit && (!opts || !opts.ignorable)) { //ignorable implies implicit
				ignorable = false;
				if (!opts || !opts.implicit)
					implicit = false;
			}
			if (opts && opts.ctl && !ctli) {
				ctli = aureq.target.uuid;
				ctlc = aureq.name;
			}
		}
		zAu._ignorable = ignorable;

		//Consider XML (Pros: ?, Cons: larger packet)
		var content = "";
		for (var j = 0, el = es.length; el; ++j, --el) {
			var aureq = es.shift(),
				t = aureq.target;
			content += "&cmd."+j+"="+aureq.name+"&uuid."+j+"="+(t.uuid?t.uuid:'');
			if (aureq.data)
				for (var k = 0, dl = aureq.data.length; k < dl; ++k) {
					var data = aureq.data[k];
					content += "&data."+j+"="
						+ (data != null ? encodeURIComponent(data): '_z~nil');
				}
		}

		if (content)
			zAu._sendNow2({
				sid: zAu._seqId, uri: zAu.comURI(null, dt),
				dt: dt, content: "dtid=" + dt.id + content,
				ctli: ctli, ctlc: ctlc, implicit: implicit,
				ignorable: ignorable, tmout: 0
			});
	},
	_sendNow2: function(reqInf) {
		var req = zUtl.newAjax(),
			uri = zAu._useQS(reqInf) ? reqInf.uri + '?' + reqInf.content: null;
		zAu.sentTime = zUtl.now(); //used by server-push (zkex)
		try {
			req.onreadystatechange = zAu._onRespReady;
			req.open("POST", uri ? uri: reqInf.uri, true);
			req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			req.setRequestHeader("ZK-SID", reqInf.sid);
			if (zAu._errcode) {
				req.setRequestHeader("ZK-Error-Report", zAu._errcode);
				delete zAu._errcode;
			}

			if (zk.pfmeter) zAu._pfsend(reqInf.dt, req);

			zAu._areq = req;
			zAu._areqInf = reqInf;
			if (zk.resendDelay > 0)
				zAu._areqInf.tfn = setTimeout(zAu._areqTmout, zk.resendDelay + reqInf.tmout);

			if (uri) req.send(null);
			else req.send(reqInf.content);

			if (!reqInf.implicit) zk.startProcessing(zk.procDelay); //wait a moment to avoid annoying
		} catch (e) {
			//handle error
			try {
				if(typeof req.abort == "function") req.abort();
			} catch (e2) {
			}

			if (!reqInf.ignorable && !zAu._unloading) {
				var msg = e.message;
				zAu._errcode = "[Send] " + msg;
				if (zAu.confirmRetry("FAILED_TO_SEND", msg)) {
					zAu._areqResend(reqInf);
					return;
				}
			}
			zAu._cleanupOnFatal(reqInf.ignorable);
		}
	},
	//IE: use query string if possible to avoid IE incomplete-request problem
	_useQS: zk.ie ? function (reqInf) {
		var s = reqInf.content, j = s.length, prev, cc;
		if (j + reqInf.uri.length < 2000) {
			while (--j >= 0) {
				cc = s.charAt(j);
				if (cc == '%' && prev >= '8') //%8x, %9x...
					return false;
				prev = cc;
			}
			return true;
		}
		return false;
	}: zk.$void,

	_doCmds0: function () {
		var ex, j = 0, que = zAu._cmdsQue, rid = zAu._resId;
		for (; j < que.length; ++j) {
			if (zk.loading) {
				zk.addInit(zAu.doCmds); //wait until the loading is done
				return;
			}

			var cmds = que[j];
			if (rid == cmds.rid || !rid || !cmds.rid //match
			|| zAu._dtids.length > 1) { //ignore multi-desktops (risky but...)
				que.splice(j, 1);

				var oldrid = rid;
				if (cmds.rid) {
					if ((rid = cmds.rid + 1) >= 1000)
						rid = 1; //1~999
					zAu._resId = rid;
				}

				try {
					if (zAu._doCmds1(cmds)) { //done
						j = -1; //start over
						if (zk.pfmeter) zAu.pfdone(cmds.dt, cmds.pfIds);
					} else { //not done yet (=zk.loading)
						zAu._resId = oldrid; //restore
						que.splice(j, 0, cmds); //put it back
						zk.addInit(zAu.doCmds);
						return;
					}
				} catch (e) {
					if (!ex) ex = e;
					j = -1; //start over
				}
			}
		}

		if (que.length) { //sequence is wrong => enforce to run if timeout
			setTimeout(function () {
				if (que.length && rid == zAu._resId) {
					var r = que[0].rid;
					for (j = 1; j < que.length; ++j) { //find min
						var r2 = que[j].rid,
							v = r2 - r;
						if (v > 500 || (v < 0 && v > -500)) r = r2;
					}
					zAu._resId = r;
					zAu.doCmds();
				}
			}, 3600);
		}

		if (ex) throw ex;
	},
	_doCmds1: function (cmds) {
		var processed;
		try {
			while (cmds && cmds.length) {
				if (zk.loading)
					return false;

				processed = true;
				var cmd = cmds.shift();
				try {
					zAu.process(cmd.cmd, cmd.data);
				} catch (e) {
					zAu.showError("FAILED_TO_PROCESS", null, cmd.cmd, e);
					throw e;
				}
			}
		} finally {
			if (processed && (!cmds || !cmds.length))
				zWatch.fire('onResponse', 0); //use setTimeout
		}
		return true;
	},

	/** Cleans up if we detect obsolete or other severe errors. */
	_cleanupOnFatal: function (ignorable) {
		for (var uuid in zAu._metas) {
			var meta = zAu._metas[uuid];
			if (meta && meta.cleanupOnFatal)
				meta.cleanupOnFatal(ignorable);
		}
	}
};

//Commands//
zAu.cmd0 = { //no uuid at all
	bookmark: function (bk) {
		zHistory.bookmark(bk);
	},
	obsolete: function (dt0, dt1) { //desktop timeout
		zAu._cleanupOnFatal();
		zk.error(dt1);
	},
	alert: function (msg) {
		zDom.alert(msg);
	},
	redirect: function (url, target) {
		try {
			zUtl.go(url, false, target);
		} catch (ex) {
			if (!zAu.confirmClose) throw ex;
		}
	},
	title: function (dt0) {
		document.title = dt0;
	},
	script: function (dt0) {
		eval(dt0);
	},
	echo: function (dtid) {
		zAu.send({dtid: dtid, cmd: "dummy", ignorable: true});
	},
	clientInfo: function (dtid) {
		zAu._cInfoReg = true;
		zAu.send({dtid: dtid, cmd: "onClientInfo", data: [
			new Date().getTimezoneOffset(),
			screen.width, screen.height, screen.colorDepth,
			zk.innerWidth(), zk.innerHeight(), zk.innerX(), zk.innerY()
		]});
	},
	download: function (url) {
		if (url) {
			var ifr = zDom.$('zk_download');
			if (ifr) {
				ifr.src = url; //It is OK to reuse the same iframe
			} else {
				var html = '<iframe src="'+url+'" id="zk_download" name="zk_download" style="display:none;width:0;height:0;border:0"></iframe>';
				zk.insertHTMLBeforeEnd(document.body, html);
			}
		}
	},
	print: function () {
		window.print();
	},
	scrollBy: function (x, y) {
		window.scrollBy(x, y);
	},
	scrollTo: function (x, y) {
		window.scrollTo(x, y);
	},
	resizeBy: function (x, y) {
		window.resizeBy(x, y);
	},
	resizeTo: function (x, y) {
		window.resizeTo(x, y);
	},
	moveBy: function (x, y) {
		window.moveBy(x, y);
	},
	moveTo: function (x, y) {
		window.moveTo(x, y);
	},
	cfmClose: function (msg) {
		zAu.confirmClose = msg;
	},
	showBusy: function (msg, open) {
		//close first (since users might want close and show diff message)
		var n = zDom.$("zk_showBusy");
		if (n) {
			n.parentNode.removeChild(n);
			zk.restoreDisabled();
		}

		if (open == "true") {
			n = zDom.$("zk_loadprog");
			if (n) n.parentNode.removeChild(n);
			n = zDom.$("zk_prog");
			if (n) n.parentNode.removeChild(n);
			n = zDom.$("zk_showBusy");
			if (!n) {
				msg = msg == "" ? mesg.PLEASE_WAIT : msg;
				zUtl.progressbox("zk_showBusy", msg, true);
				zk.disableAll();
			}
		}
	}
};
zAu.cmd1 = {
	wrongValue: function (uuid, cmp, dt1) {
		for (var uuids = uuid.split(","), i = 0, j = uuids.length; i < j; i++) {
			cmp = zDom.$(uuids[i]);
			if (cmp) {
				cmp = $real(cmp); //refer to INPUT (e.g., datebox)
				//we have to update default value so validation will be done again
				var old = cmp.value;
				cmp.defaultValue = old + "_err"; //enforce to validate
				if (old != cmp.value) cmp.value = old; //Bug 1490079 (FF only)
				if (zAu.valid) zAu.valid.errbox(cmp.id, arguments[i+2], true);
				else zDom.alert(arguments[i+2]);
			} else if (!uuids[i]) { //keep silent if component (of uuid) not exist (being detaced)
				zDom.alert(arguments[i+2]);
			}
		}
	},
	setAttr: function (uuid, wgt, nm, val) {
		for (var len = arguments.length, j = 3; j < len;)
			zk.set(wgt, nm, arguments[j++]);
	},
	outer: function (uuid, wgt, code) {
		var cf = zk.currentFocus, cfid;
		if (cf && zUtl.isAncestor(wgt, cf, true)) {
			cfid = cf.node.id;
			zk.currentFocus = null;
		} else
			cf = null;

		zAu.stub = function (newwgt) {
			var p = newwgt.parent = wgt.parent,
				s = newwgt.previousSibling = wgt.previousSibling;
			if (s) s.nextSibling = newwgt;
			else if (p) p.firstChild = newwgt;

			s = newwgt.nextSibling = wgt.nextSibling;
			if (s) s.previousSibling = newwgt;
			else if (p) p.lastChild = newwgt;

			newwgt.replaceHTML(wgt.uuid, wgt.desktop);

			//TODO: if (zAu.valid) zAu.valid.fixerrboxes();
			if (cf && !zk.currentFocus && cfid) zUtl.focus(cfid);
		};
		try {
			eval(code);
		} finally {
			zAu.stub = null;
		}
	},
	addAft: function (uuid, wgt, code) {
		//Bug 1939059: This is a dirty fix. Refer to AuInsertBefore
		//Format: comp-uuid:pg-uuid (if native root)
		if (!wgt) {
			var j = uuid.indexOf(':');
			if (j >= 0) { //native root
				wgt = zk.Widget.$(uuid.substring(0, j)); //try comp (though not possible)
				if (!wgt) {
					uuid = uuid.substring(j + 1); //try page
					wgt = zk.Widget.$(uuid);
					if (!wgt) wgt = document.body;
					zAu.cmd1.addChd(uuid, wgt, code);
					return;
				}
			}
		}

		zAu.stub = function (child) {
			wgt.parent.insertBefore(child, wgt.nextSibling);
		};
		try {
			eval(code);
		} finally {
			zAu.stub = null;
		}
	},
	addBfr: function (uuid, wgt, code) {
		zAu.stub = function (child) {
			wgt.parent.insertBefore(child, wgt);
		};
		try {
			eval(code);
		} finally {
			zAu.stub = null;
		}
	},
	addChd: function (uuid, wgt, code) {
		zAu.stub = function (child) {
			wgt.appendChild(child);
		};
		try {
			eval(code);
		} finally {
			zAu.stub = null;
		}
	},
	rm: function (uuid, wgt) {
		//NOTE: it is possible the server asking removing a non-exist cmp
		//so keep silent if not found
		if (wgt) {
			var p = wgt.parent;
			if (p) p.removeChild(wgt);
			else {
				p = wgt.node;
				wgt.unbind_();
				zDom.remove(p);
			}
		}
		//TODO if (zAu.valid) zAu.valid.fixerrboxes();
	},
	focus: function (uuid, cmp) {
		if (!zk.eval(cmp, "focus")) {
			//Bug 1936366: endModal uses timer, so canFocus might be false
			//when this method is called
			setTimeout(function (){
				if (!zAu.canFocus(cmp, true)) return;

				zAu.autoZIndex(cmp); //some, say, window, not listen to onfocus
				cmp = $real(cmp); //focus goes to inner tag
				zk.asyncFocus(cmp.id, 35);
				}, 30); //wnd.js uses 20
		}
	},
	closeErrbox: function (uuid, cmp) {
		if (zAu.valid) {
			var uuids = uuid.trim().split(',');
			for (var i = uuids.length; --i >= 0;)
				zAu.valid.closeErrbox(uuids[i], false, true);
		}
	},
	submit: function (uuid, cmp) {
		setTimeout(function (){if (cmp && cmp.submit) cmp.submit();}, 50);
	},
	invoke: function (uuid, cmp, func, arg0, arg1, arg2) {
		zk.eval(cmp, func, null, arg0, arg1, arg2);
	},
	popup: function (uuid, cmp, mode, x, y) {
		var type = $type(cmp);
		if (type) {
			if (mode == "0") { //close
				zAu.closeFloatsOf(cmp);
			} else {
				var ref;
				if (mode == "1") { //ref
					ref = zDom.$(x);
					if (ref) {
						var ofs = zPos.cumulativeOffset(zDom.$(x));
						x = ofs[0];
						y = ofs[1] + zk.offsetHeight(ref);
					}
				}
				cmp.style.position = "absolute";
				zk.setVParent(cmp); //FF: Bug 1486840, IE: Bug 1766244
				zAu._autopos(cmp, $int(x), $int(y));
				zk.eval(cmp, "context", type, ref);
			}
		}
	},
	echo2: function (uuid, cmp, evtnm, data) {
		zAu.send(
			{uuid: uuid, cmd: "echo",
				data: data != null ? [evtnm, data]: [evtnm], ignorable: true});
	}
};
