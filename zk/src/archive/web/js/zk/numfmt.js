/* numfmt.js

	Purpose:
		
	Description:
		
	History:
		Fri Jan 16 19:13:43     2009, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zNumFormat = {
	format: function (fmt, val) {
		if (!val) return '';
		if (!fmt) return '' + val;
		
		//caculate number of fixed decimals
		var pureFmtStr = fmt.replace(/[^#.]/g, ''),
			ind = pureFmtStr.indexOf('.'),
			fixed = ind > 0 ? pureFmtStr.length - ind - 1 : 0,
			valueStr = val + "";
			
		valueStr = (valueStr.replace(/[^0123456789.]/g, '') * 1).toFixed(fixed);
		
		var indFmt = fmt.indexOf('.'), indVal = valueStr.indexOf('.'), pre = suf = '';
		
		//pre part
		if (indVal == -1) 
			indVal = valueStr.length;
		for (var i = indFmt - 1, j = indVal - 1; i >= 0 && j >= 0;) {
			if (fmt.charAt(i) == '#') {
				pre = valueStr.charAt(j) + pre;
				i--;
				j--;
			} else {
				pre = fmt.charAt(i) + pre;
				i--;
			}
		}
		if (j >= 0) 
			pre = valueStr.substr(0, j + 1) + pre;
		
		//sufpart
		for (var i = indFmt + 1, j = indVal + 1, fl = fmt.length, vl = valueStr.length; i < fl && j < vl; i++) {
			if (fmt.charAt(i) == '#') {
				suf += valueStr.charAt(j);
				j++;
			} else
				suf += fmt.charAt(i);
		}
		if (j < valueStr.length) 
			suf = valueStr.substr(j, valueStr.length);
		
		//combine
		if (!pre) 
			pre = "0";
		return suf ? pre + "." + suf : pre;
	},
	unformat: function (fmt, val) {
		if (!val) return {raw: val, divscale: 0};

		var divscale = 0, //the second element
			minus, sb, cc, ignore;
		for (var j = 0, len = val.length; j < len; ++j) {
			cc = val.charAt(j);
			ignore = true;

			//We handle percent and (nnn) specially
			if (cc == zk.PERCENT) divscale += 2;
			else if (cc == zk.PER_MILL) divscale += 3;
			else if (cc == '(') minus = true;
			else if (cc != '+') ignore = false;

			//We don't add if cc shall be ignored (not alphanum but in fmt)
			if (!ignore)
				ignore = (cc < '0' || cc > '9')
				&& cc != zk.DECIMAL && cc != zk.MINUS && cc != '+'
				&& (zUtl.isChar(cc,{whitespace:1}) || cc == zk.GROUPING || cc == ')'
					|| (fmt && fmt.indexOf(cc) >= 0));
			if (ignore) {
				if (!sb) sb = val.substring(0, j);
			} else {
				var c2 = cc == zk.MINUS ? '-':
					cc == zk.DECIMAL ? '.':  cc;
				if (cc != c2 && !sb)
					sb = val.substring(0, j);
				if (sb) sb += c2;
			}
		}
		if (minus) {
			if (!sb) sb = val;
			if (sb.length)
				if (sb.charAt(0) == '-') sb = sb.substring(1); //-- => +
				else sb = '-' + sb;
		}
		return {raw: sb || val, divscale: divscale};
	}
};