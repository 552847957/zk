/* datefmt.js

	Purpose:

	Description:

	History:
		Fri Jan 16 19:13:43	 2009, Created by Flyworld

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under LGPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zk.fmt.Date = {
	checkDate : function (ary, txt) {
		if (txt.length)
			for (var j = ary.length; j--;) {
				var k = txt.indexOf(ary[j]);
				if (k >= 0)
					txt = txt.substring(0, k) + txt.substring(k + ary[j].length);
			}
		return txt;
	},
	digitFixed : function (val, digits) {
		var s = "" + val;
		for (var j = digits - s.length; --j >= 0;)
			s = "0" + s;
		return s;
	},
	_parseToken: function (token, ts, i, len) {
		if (len < 2) len = 2;
		if (token && token.length > len) {
			ts[i] = token.substring(len);
			return token.substring(0, len);
		}
		return token;
	},
	parseDate : function (txt, fmt, strict, refval) {
		if (!fmt) fmt = "yyyy/MM/dd";
		refval = refval || zUtl.today(true);
		var y = refval.getFullYear(),
			m = refval.getMonth(),
			d = refval.getDate(), dFound,
			hr = refval.getHours(),
			min = refval.getMinutes(),
			sec = refval.getSeconds(),
			msec = refval.getMilliseconds(),
			aa = fmt.indexOf('a'),
			hh = fmt.indexOf('h'),
			KK = fmt.indexOf('K'),
			hasAM = aa > -1,
			hasHour1 = hasAM ? hh > -1 || KK > -1 : false,
			isAM;

		var ts = [], mindex = fmt.indexOf("MMM"), eindex = fmt.indexOf("EE"),
			fmtlen = fmt.length, ary = [],
			//mmindex = mindex + 3,
			isNumber = !isNaN(txt),
			tlen = txt.replace(/[^.]/g, '').length,
			flen = fmt.replace(/[^.]/g, '').length;
		for (var i = 0, k = 0, j = txt.length; k < j; i++, k++) {
			var c = txt.charAt(k),
				f = fmtlen > i ? fmt.charAt(i) : "";
			if (c.match(/\d/)) {
				ary.push(c);
			} else if ((mindex >= 0 && mindex <= i /*&& mmindex >= i location French will lose last char */)
			|| (eindex >= 0 && eindex <= i) || (aa > -1 && aa <= i)) {
				if (c.match(/\w/)) {
					ary.push(c);
				} else {
					if (c.charCodeAt(0) < 128 && (c.charCodeAt(0) != 46 ||
								tlen == flen || f.charCodeAt(0) == 46)) {
						if (ary.length) {
							ts.push(ary.join(""));
							ary = [];
						}
					} else
						ary.push(c);
				}
			} else if (ary.length) {
				if (txt.charAt(k-1).match(/\d/))
					while (f == fmt.charAt(i-1) && f) {
						i++;
						f = fmt.charAt(i);
					}
				ts.push(ary.join(""));
				ary = [];
			} else if (c.match(/\w/))
				return; //failed
		}
		if (ary.length) ts.push(ary.join(""));
		if (!ts.length) return;
		for (var i = 0, j = 0, offs = 0, fl = fmt.length; j < fl; ++j) {
			var cc = fmt.charAt(j);
			if ((cc >= 'a' && cc <= 'z') || (cc >= 'A' && cc <= 'Z')) {
				var len = 1;
				for (var k = j; ++k < fl; ++len)
					if (fmt.charAt(k) != cc)
						break;

				var nosep; //no separator
				if (k < fl) {
					var c2 = fmt.charAt(k);
					nosep = c2 == 'y' || c2 == 'M' || c2 == 'd' || c2 == 'E';
				}
				var token = isNumber ? ts[0].substring(j - offs, k - offs) : ts[i++];
				switch (cc) {
				case 'y':
					if (nosep) {
						if (len <= 3) len = 2;
						if (token && token.length > len) {
							ts[--i] = token.substring(len);
							token = token.substring(0, len);
						}
					}
					y = zk.parseInt(token);
					if (isNaN(y)) return; //failed
					if (y < 100) y += y > 29 ? 1900 : 2000;
					break;
				case 'M':
					var mon = token ? token.toLowerCase() : '';
					if (token)
						for (var index = zk.SMON.length; --index >= 0;) {
							var smon = zk.SMON[index].toLowerCase();
							if (mon.startsWith(smon)) {
								token = zk.SMON[index];
								m = index;
								break;
							}
						}
					if (len == 3 && token) {
						if (nosep)
							token = this._parseToken(token, ts, --i, token.length);//token.length: the length of French month is 4
						break; // nothing to do.
					}else if (len <= 2) {
						if (nosep && token && token.length > 2) {//Bug 2560497 : if no seperator, token must be assigned.
							ts[--i] = token.substring(2);
							token = token.substring(0, 2);
						}
						m = zk.parseInt(token) - 1;
					} else {
						for (var l = 0;; ++l) {
							if (l == 12) return; //failed
							if (len == 3) {
								if (zk.SMON[l] == token) {
									m = l;
									break;
								}
							} else {
								if (token && zk.FMON[l].startsWith(token)) {
									m = l;
									break;
								}
							}
						}
					}
					if (isNaN(m) || m > 11 /*|| m < 0 accept 0 since it is a common-yet-acceptable error*/) //restrict since user might input year for month
						return;//failed
					break;
				case 'E':
					if (nosep)
						this._parseToken(token, ts, --i, len);
					break;
				case 'd':
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					d = zk.parseInt(token);
					dFound = true;
					if (isNaN(d) || d < 0 || d > 31) //restrict since user might input year for day (ok to allow 0 and 31 for easy entry)
						return; //failed
					break;
				case 'H':
					if (hasHour1)
						break;
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					hr = zk.parseInt(token);
					if (isNaN(hr)) return; //failed
					break;
				case 'h':
					if (!hasHour1)
						break;
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					hr = zk.parseInt(token);
					if (hr == 12)
						hr = 0;
					if (isNaN(hr)) return; //failed
					break;
				case 'K':
					if (!hasHour1)
						break;
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					hr = zk.parseInt(token);
					if (isNaN(hr)) return; //failed
					hr %= 12;
					break;
				case 'k':
					if (hasHour1)
						break;
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					hr = zk.parseInt(token);
					if (hr == 24)
						hr = 0;
					if (isNaN(hr)) return; //failed
					break;
				case 'm':
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					min = zk.parseInt(token);
					if (isNaN(min)) return; //failed
					break;
				case 's':
					if (nosep)
						token = this._parseToken(token, ts, --i, len);
					sec = zk.parseInt(token);
					if (isNaN(sec)) return; //failed
					break;
				case 'a':
					if (!hasHour1)
						break;
					if (!token) return; //failed
					isAM = token.startsWith(zk.APM[0]);
					break
				//default: ignored
				}
				j = k - 1;
			} else offs++;
		}

		if (hasHour1 && isAM === false)
			hr += 12;
		var dt = new Date(y, m, d, hr, min, sec, msec);
		if (!dFound && dt.getMonth() != m)
			dt = new Date(y, m + 1, 0, hr, min, sec, msec); //last date of m
		if (strict) {
			if (dt.getFullYear() != y || dt.getMonth() != m || dt.getDate() != d ||
				dt.getHours() != hr || dt.getMinutes() != min || dt.getSeconds() != sec) //ignore msec (safer though not accurate)
				return; //failed

			txt = txt.trim();
			txt = this.checkDate(zk.FDOW, txt);
			txt = this.checkDate(zk.SDOW, txt);
			txt = this.checkDate(zk.S2DOW, txt);
			txt = this.checkDate(zk.FMON, txt);
			txt = this.checkDate(zk.SMON, txt);
			txt = this.checkDate(zk.S2MON, txt);
			txt = this.checkDate(zk.APM, txt);
			for (var j = txt.length; j--;) {
				var cc = txt.charAt(j);
				if ((cc > '9' || cc < '0') && fmt.indexOf(cc) < 0)
					return; //failed
			}
		}
		return +dt == +refval ? refval: dt;
			//we have to use getTime() since dt == refVal always false
	},
	formatDate : function (val, fmt) {
		if (!fmt) fmt = "yyyy/MM/dd";

		var txt = "";
		for (var j = 0, fl = fmt.length; j < fl; ++j) {
			var cc = fmt.charAt(j);
			if ((cc >= 'a' && cc <= 'z') || (cc >= 'A' && cc <= 'Z')) {
				var len = 1;
				for (var k = j; ++k < fl; ++len)
					if (fmt.charAt(k) != cc)
						break;

				switch (cc) {
				case 'y':
					if (len <= 3) txt += this.digitFixed(val.getFullYear() % 100, 2);
					else txt += this.digitFixed(val.getFullYear(), len);
					break;
				case 'M':
					if (len <= 2) txt += this.digitFixed(val.getMonth()+1, len);
					else if (len == 3) txt += zk.SMON[val.getMonth()];
					else txt += zk.FMON[val.getMonth()];
					break;
				case 'd':
					txt += this.digitFixed(this.dayInMonth(val), len);
					break;
				case 'E':
					if (len <= 3) txt += zk.SDOW[(val.getDay() - zk.DOW_1ST + 7) % 7];
					else txt += zk.FDOW[(val.getDay() - zk.DOW_1ST) % 7];
					break;
				case 'D':
					txt += this.dayInYear(val);
					break;
				case 'w':
					txt += this.weekInYear(val);
					break;
				case 'W':
					txt += this.weekInMonth(val);
					break;
				case 'G':
					txt += zk.ERA;
					break;
				case 'F':
					txt += this.dayOfWeekInMonth(val);
					break;
				case 'H':
					if (len <= 2) txt += this.digitFixed(val.getHours(), len);
					break;
				case 'k':
					var h = val.getHours();
					if (h == 0)
						h = '24';
					if (len <= 2) txt += this.digitFixed(h, len);
					break;
				case 'K':
					if (len <= 2) txt += this.digitFixed(val.getHours() % 12, len);
					break;
				case 'h':
					var h = val.getHours();
					h %= 12;
					if (h == 0)
						h = '12';
					if (len <= 2) txt += this.digitFixed(h, len);
					break;
				case 'm':
					if (len <= 2) txt += this.digitFixed(val.getMinutes(), len);
					break;
				case 's':
					if (len <= 2) txt += this.digitFixed(val.getSeconds(), len);
					break;
				case 'Z':
					txt += -(val.getTimezoneOffset()/60);
					break;
				case 'a':
					txt += zk.APM[val.getHours() > 11 ? 1 : 0];
					break;
				default:
					txt += '1';
					//fake; SimpleDateFormat.parse might ignore it
					//However, it must be an error if we don't generate a digit
				}
				j = k - 1;
			} else {
				txt += cc;
			}
		}
		return txt;
	},
	/** Converts milli-second to day. */
	ms2day : function (t) {
		return Math.round(t / 86400000);
	},
	/** Day in year (starting at 1). */
	dayInYear : function (d, ref) {
		if (!ref) ref = new Date(d.getFullYear(), 0, 1);
		return this.digitFixed(1 + this._dayInYear(d, ref));
	},
	_dayInYear: function (d, ref) {
		return Math.round((new Date(d.getFullYear(), d.getMonth(), d.getDate())-ref)/864e5);
	},
	/** Day in month (starting at 1). */
	dayInMonth : function (d) {
		return d.getDate();
	},
	/** Week in year (starting at 1). */
	weekInYear : function (d, ref) {
		if (!ref) ref = new Date(d.getFullYear(), 0, 1);
		var wday = ref.getDay();
		if (wday == 7) wday = 0;
		return this.digitFixed(1 + Math.floor((this._dayInYear(d, ref) + wday) / 7));
	},
	/** Week in month (starting at 1). */
	weekInMonth : function (d) {
		return this.weekInYear(d, new Date(d.getFullYear(), d.getMonth(), 1));
	},
	/** Day of week in month. */
	dayOfWeekInMonth : function (d) {
		return this.digitFixed(1 + Math.floor(this._dayInYear(d, new Date(d.getFullYear(), d.getMonth(), 1)) / 7));
	}
};
/**
 * The <code>calendar</code> object provides a way
 * to convert between a specific instant in time for locale-sensitive
 * like buddhist's time.
 * <p>By default the year offset is specified from server if any.</p>
 * @since 5.0.1
 */
zk.fmt.Calendar = zk.$extends(zk.Object, {
	_offset: zk.YDELTA,
	$init: function (date) {
		this._date = date;
	},
	getTime: function () {
		return this._date;
	},
	setTime: function (date) {
		this._date = date;
	},
	setYearOffset: function (val) {
		this._offset = val;
	},
	getYearOffset: function () {
		return this._offset;
	},
	formatDate: function (val, fmt) {
		var d;
		if (this._offset) {
			d = new Date(val);
			d.setFullYear(d.getFullYear() + this._offset);
		}
		return zk.fmt.Date.formatDate(d || val, fmt);
	},
    toUTCDate: function () {
        var d;
        if ((d = this._date) && this._offset)
            (d = new Date(d))
                .setFullYear(d.getFullYear() - this._offset);
        return d;
    }, 
	parseDate: function (txt, fmt, strict, refval) {
		var d = zk.fmt.Date.parseDate(txt, fmt, strict, refval);
		if (this._offset && fmt) {
			var cnt = 0;
			for (var i = fmt.length; i--;)
				if (fmt.charAt(i) == 'y')
					cnt++;
			if (cnt > 3)
				d.setFullYear(d.getFullYear() - this._offset);
			else if (cnt) {
				var year = d.getFullYear();
				if (year < 2000)
					d.setFullYear(year + (Math.ceil(this._offset / 100) * 100 - this._offset));
				else
					d.setFullYear(year - (this._offset % 100));
			}
		}
		return d;
	},
	getYear: function () {
		return this._date.getFullYear() + this._offset;
	}
});