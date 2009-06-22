/* applet.js
 {{IS_NOTE
 Purpose:

 Description:

 History:
 Fri Sep 19 17:31:03 TST 2008, Created by davidchen
 }}IS_NOTE
 Copyright (C) 2008 Potix Corporation. All Rights Reserved.
 {{IS_RIGHT
 This program is distributed under GPL Version 3.0 in the hope that
 it will be useful, but WITHOUT ANY WARRANTY.
 }}IS_RIGHT
 */
zkApplet = {
	invoke: zk.ie ? function(id){
		var cmp = $e(id);
		if (cmp && arguments.length >= 2) {
			if(arguments.length < 4){
				var expr = "cmp." + arguments[1] + '(';
				for (var j = 2; j < arguments.length;) {
					if (j != 2) expr += ',';
					var s = arguments[j++];
					expr += '"' + (s ? s.replace('"', '\\"'): '') + '"';
				}
			}else{
				var expr = "cmp." + arguments[1] + '([';
				for (var j = 2; j < arguments.length;) {
					if (j != 2) expr += ',';
					var s = arguments[j++];
					expr += '"' + (s ? s.replace('"', '\\"'): '') + '"';
				}
			}
			try {
				if(arguments.length < 4)
					eval(expr + ');');
				else
					eval(expr + ']);');
			} catch (e) {
				zk.error("Failed to invoke applet's method: " + expr);
			}
		}
	}: function(id){
		var cmp = $e(id);
		if (cmp && arguments.length >= 2) {
			var fn = arguments[1],
				func = cmp[fn];
			if (!func) {
				zk.error("Method not found: "+fn);
				return;
			}
			try {
				var args = [],
					arrayArg = [];
				if(arguments.length < 4){
					for (var j = 2; j < arguments.length;)
						args.push(arguments[j++]);
				}else{
					for (var j = 2; j < arguments.length;)
						arrayArg.push(arguments[j++]);
					args.push(arrayArg);
				}
				func.apply(cmp, args);
			} catch (e) {
				zk.error("Failed to invoke applet's method: " + fn);
			}
		}
	},
	field: function(id, name, value) {
		var cmp = $e(id);
		if (cmp) {
			try {
				cmp[name] = value;
			} catch(e) {
				zk.error("Failed to set applet's field: " + name);
			}
		}
	}
};