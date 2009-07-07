/* Button.js

	Purpose:
		
	Description:
		
	History:
		Sat Nov  8 22:58:16     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.wgt.Button = zk.$extends(zul.LabelImageWidget, {
	_orient: "horizontal",
	_dir: "normal",
	_tabindex: -1,

	$define: {
		href: null,
		target: null,
		dir: _zkf = function () {
			this.updateDomContent_();
		},
		orient: _zkf,
		disabled: function (v) {
			if (this.desktop)
				if (this._mold != 'trendy') this.getNode().disabled = v;
				else this.rerender(); //bind and unbind required
		},
		tabindex: function (v) {
			var n = this.getNode();
			if (n) (this.getSubnode('btn') || n).tabIndex = v >= 0 ? v: '';
		},
		autodisable: null
	},

	//super//
	focus: function (timeout) {
		if (this.desktop && this.isVisible() && this.canActivate({checkOnly:true})) {
			zk(this.getSubnode('btn')||this.getNode()).focus(timeout);
			return true;
		}
		return false;
	},

	domContent_: function () {
		var label = zUtl.encodeXML(this.getLabel()),
			img = this.getImage();
		if (!img) return label;

		img = '<img src="' + img + '" align="absmiddle" />';
		var space = "vertical" == this.getOrient() ? '<br/>': ' ';
		return this.getDir() == 'reverse' ?
			label + space + img: img + space + label;
	},
	domClass_: function (no) {
		var scls = this.$supers('domClass_', arguments);
		if (this._disabled && (!no || !no.zclass)) {
			var s = this.getZclass();
			if (s) scls += (scls ? ' ': '') + s + '-disd';
		}
		return scls;
	},

	getZclass: function () {
		var zcls = this._zclass;
		return zcls != null ? zcls: this._mold != 'trendy' ? "z-button-os": "z-button";
	},
	bind_: function () {
		this.$supers('bind_', arguments);

		var n;
		if (this._mold != 'trendy') {
			n = this.getNode();
		} else {
			if (this._disabled) return;

			zk(this.getSubnode('box')).disableSelection();

			n = this.getSubnode('btn');
		}

		this.domListen_(n, "onFocus", "doFocus_");
		this.domListen_(n, "onBlur", "doBlur_");
	},
	unbind_: function () {
		var n = this._mold != 'trendy' ? this.getNode(): this.getSubnode('btn');
		if (n) {
			this.domUnlisten_(n, "onFocus", "doFocus_");
			this.domUnlisten_(n, "onBlur", "doBlur_");
		}

		this.$supers('unbind_', arguments);
	},

	doFocus_: function (evt) {
		if (this._mold == 'trendy')
			jq(this.getSubnode('box')).addClass(this.getZclass() + "-focus");
		this.$supers('doFocus_', arguments);
	},
	doBlur_: function (evt) {
		if (this._mold == 'trendy')
			jq(this.getSubnode('box')).removeClass(this.getZclass() + "-focus");
		this.$supers('doBlur_', arguments);
	},
	doClick_: function (evt) {
		if (!this._disabled) {
			var ads = this._autodisable, aded;
			if (ads) {
				ads = ads.split(',');
				for (var j = ads.length; --j >= 0;) {
					var ad = ads[j].trim();
					if (ad) {
						var perm;
						if (perm = ad.charAt(0) == '+')
							ad = ad.substring(1);
						ad = "self" == ad ? this: this.$f(ad);
						if (ad) {
							ad.setDisabled(true);
							if (this.inServer)
								if (perm)
									ad.smartUpdate('disabled', true);
								else if (!aded) aded = [ad];
								else aded.push(ad);
						}
					}
				}
			}
			if (aded) {
				aded = new zul.wgt.ADBS(aded);
				if (this.isListen('onClick', {asapOnly:true}))
					zWatch.listen({onResponse: aded});
				else
					setTimeout(function () {aded.onResponse();}, 800);
			}

			this.fireX(evt);

			if (!evt.stopped) {
				var href = this._href;
				if (href)
					zUtl.go(href, false, this._target, "target");
			}
		}
		//Unlike DOM, we don't proprogate to parent (so no calling $supers)
	},
	doMouseOver_: function () {
		if (!this._disabled)
			jq(this.getSubnode('box')).addClass(this.getZclass() + "-over");
		this.$supers('doMouseOver_', arguments);
	},
	doMouseOut_: function (evt) {
		if (!this._disabled && this != zul.wgt.Button._curdn
		&& !(zk.ie && jq.isAncestor(this.getSubnode('box'), evt.domEvent.relatedTarget || evt.domEvent.toElement)))
			jq(this.getSubnode('box')).removeClass(this.getZclass() + "-over");
		this.$supers('doMouseOut_', arguments);
	},
	doMouseDown_: function () {
		if (!this._disabled) {
			var zcls = this.getZclass();
			jq(this.getSubnode('box')).addClass(zcls + "-clk")
				.addClass(zcls + "-over")
			zk(this.getSubnode('btn')).focus(30);
		}

		zk.mouseCapture = this; //capture mouse up
		this.$supers('doMouseDown_', arguments);
	},
	doMouseUp_: function () {
		if (!this._disabled) {
			var zcls = this.getZclass();
			jq(this.getSubnode('box')).removeClass(zcls + "-clk")
				.removeClass(zcls + "-over");
		}
		this.$supers('doMouseUp_', arguments);
	}
});
//handle autodisabled buttons
zul.wgt.ADBS = zk.$extends(zk.Object, {
	$init: function (ads) {
		this._ads = ads;
	},
	onResponse: function () {
		for (var ads = this._ads, ad; ad = ads.shift();)
			ad.setDisabled(false);
		zWatch.unlisten({onResponse: this});
	}
});
