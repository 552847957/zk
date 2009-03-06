/* effect.js

	Purpose:
		
	Description:
		
	History:
		Mon Nov 10 14:45:53     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

script.aculo.us effects.js v1.7.0
Copyright (c) 2005, 2006 Thomas Fuchs (http://script.aculo.us, http://mir.aculo.us)

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zEffect = {
	fade: function(element, opts) {
		element = zDom.$(element);
		var oldOpacity = element.style.opacity || '';
		opts = zk.$default(opts, {to: 0.0});
		if (!opts.from) opts.from = zDom.getOpacity(element) || 1.0;
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) { 
				if(effect.opts.to==0) {
					var e = effect.node;
					zDom.hide(e);
					zDom.setStyle(e, {opacity: oldOpacity}); 
				}
			};
		return new zk.eff.Opacity(element,opts);
	},
	appear: function(element, opts) {
		element = zDom.$(element);
		opts = zk.$default(opts, {to: 1.0});
		if (!opts.from)
			opts.from = zDom.getStyle(element, 'display') == 'none' ? 0.0 : zDom.getOpacity(element) || 0.0;
		
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) { 
				zDom.rerender(effect.node);
				// force Safari to render floated elements properly
			};
		if (!opts.beforeSetup)
			opts.beforeSetup = function(effect) {
				var e = effect.node;
				zDom.setOpacity(e, effect.opts.from);
				zDom.show(e);
			};
		return new zk.eff.Opacity(element,opts);
	},

	puff: function(element, opts) {
		element = zDom.$(element);
		var oldStyle = { 
			opacity: element.style.opacity || '', 
			position: zDom.getStyle(element, 'position'),
			top:  element.style.top,
			left: element.style.left,
			width: element.style.width,
			height: element.style.height
		};
		opts = zk.$default(opts, {duration: 1.0});
		if (!opts.beforeSetupInternal)
			opts.beforeSetupInternal = function(effect) {
				zDom.absolutize(effect._effects[0].node)
			};
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				var e = effect._effects[0].node;
				zDom.hide(e);
				zDom.setStyle(e, oldStyle);
			};
		return new zk.eff.Parallel(
			[ new zk.eff.Scale(element, 200, 
				{sync: true, scaleFromCenter: true, scaleContent: true, restoreAfterFinish: true}),
				new zk.eff.Opacity(element, {sync: true, to: 0.0}) ],
			opts);
	},

	blindUp: function(element, opts) {
		element = zDom.$(element);
		zDom.makeClipping(element);
		opts = zk.$default(opts,
			{scaleContent: false, scaleX: false, restoreAfterFinish: true});
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				var e = effect.node;
				zDom.hide(e);
				zDom.undoClipping(e);
			};
		return new zk.eff.Scale(element, 0, opts);
	},
	blindDown: function(element, opts) {
		element = zDom.$(element);
		var elementDimensions = zDom.getDimension(element);
		opts = zk.$default(opts,{
			scaleContent: false,
			scaleX: false,
			scaleFrom: 0,
			scaleMode: {originalHeight: elementDimensions.height, originalWidth: elementDimensions.width},
			restoreAfterFinish: true});
		if (!opts.afterSetup)
			opts.afterSetup = function(effect) {
				var e = effect.node;
				zDom.makeClipping(e);
				zDom.setStyle(e, {height: '0px'});
				zDom.show(e); 
			};
		if (!opts.afterFinishInternal)
			afterFinishInternal = function(effect) {
				zDom.undoClipping(effect.node);
			};
		return new zk.eff.Scale(element, 100, opts);
	},

	switchOff: function(element, opts) {
		element = zDom.$(element);
		var oldOpacity = element.style.opacity || '';
		opts = zk.$default(opts, {
			duration: 0.4, from: 0, transition: zEffect._Tranx.flicker});
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				new zk.eff.Scale(effect.node, 1, { 
					duration: 0.3, scaleFromCenter: true,
					scaleX: false, scaleContent: false,
					restoreAfterFinish: true,
					beforeSetup: function(effect) {
						var e = effect.node;
						zDom.makePositioned(e);
						zDom.makeClipping(e);
					},
					afterFinishInternal: function(effect) {
						var e = effect.node;
						zDom.hide(e);
						zDom.undoClipping(e);
						zDom.undoPositioned(e);
						zDom.setStyle(e, {opacity: oldOpacity});
					}
				});
			}
		return appear(element, opts);
	},

	dropOut: function(element, opts) {
		element = zDom.$(element);
		var oldStyle = {
			top: zDom.getStyle(element, 'top'),
			left: zDom.getStyle(element, 'left'),
			opacity: element.style.opacity || ''};
		opts = zk.$default(opts, {duration: 0.5});
		if (!opts.beforeSetup)
			opts.beforeSetup = function(effect) {
				zDom.makePositioned(effect._effects[0].node);
			};
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				var e = effect._effects[0].node;
				zDom.hide(e);
				zDom.undoPositioned(e);
				zDom.setStyle(e, oldStyle);
			};
		return new zk.eff.Parallel(
			[new zk.eff.Move(element, {x: 0, y: 100, sync: true}), 
			new zk.eff.Opacity(element, { sync: true, to: 0.0})], opts);
	},

	slideOut: function(element, opts) {
		var anchor = opts ? opts.anchor || 't': 't';
		element = zDom.$(element);

		var movement, st = element.style;
		switch (anchor) {
		case 't':
			movement = {x: 0, y: -zk.parseInt(st.height), sync: true};
			break;
		case 'b':
			movement = {x: 0, y: zk.parseInt(st.height), sync: true};
			break;
		case 'l':
			movement = {x: -zk.parseInt(st.width), y: 0, sync: true};
			break;
		case 'r':
			movement = {x: zk.parseInt(st.width), y: 0, sync: true};
			break;
		}

		var oldStyle = {
			top: zDom.getStyle(element, 'top'),
			left: zDom.getStyle(element, 'left'),
			opacity: element.style.opacity || ''};
		opts = zk.$default(opts, {duration: 0.5});
		if (!opts.beforeSetup)
			opts.beforeSetup = function(effect) {
				zDom.makePositioned(effect._effects[0].node); 
			};
		if (!opts.beforeFinishInternal)
			opts.beforeFinishInternal = function (effect) {
				zDom.hide(effect._effects[0].node);
			};
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				var e = effect._effects[0].node;
				zDom.undoPositioned(e)
				zDom.setStyle(e, oldStyle);
			}; 
		return new zk.eff.Parallel(
			[new zk.eff.Move(element, movement)], opts);
	},
	slideIn: function(element, opts) {
		var anchor = opts ? opts.anchor || 't': 't';
		element = zDom.$(element);
		var oldStyle = {
			top: zDom.getStyle(element, 'top'),
			left: zDom.getStyle(element, 'left'),
			opacity: element.style.opacity || ''};

		var movement, st = element.style;
		switch (anchor) {
		case 't':
			var t = zk.parseInt(st.top), h = zk.parseInt(st.height);
			st.top = t - h + "px";
			movement = {x: 0, y: h, sync: true};
			break;
		case 'b':
			var t = zk.parseInt(st.top), h = zk.parseInt(st.height);
			st.top = t + h + "px";
			movement = {x: 0, y: -h, sync: true};
			break;
		case 'l':
			var l = zk.parseInt(st.left), w = zk.parseInt(st.width);
			st.left = l - w + "px";
			movement = {x: w, y: 0, sync: true};
			break;
		case 'r':
			var l = zk.parseInt(st.left), w = zk.parseInt(st.width);
			st.left = l + w + "px";
			movement = {x: -w, y: 0, sync: true};
			break; 
		}

		opts = zk.$default(opts, {duration: 0.5});
		if (!opts.beforeSetup)
			opts.beforeSetup = function(effect) {
				var e = effect._effects[0].node;
				zDom.show(e);
				zDom.makePositioned(e);
			};
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				var e = effect._effects[0].node;
				zDom.undoPositioned(e);
				zDom.setStyle(e, oldStyle);
			};
		return new zk.eff.Parallel(
			[new zk.eff.Move(element, movement)], opts);
	},

	slideDown: function(element, opts) {
		var anchor = opts ? opts.anchor || 't': 't';
		element = zDom.$(element);
		zDom.cleanWhitespace(element);

		// SlideDown need to have the content of the element wrapped in a container element with fixed height!
		var orig = {t: zDom.getStyle(element, 'top'), l: zDom.getStyle(element, 'left')},
			isVert = anchor == 't' || anchor == 'b',
			dims = zDom.getDimension(element);

		opts = zk.$default(opts, {
			scaleContent: false,
			scaleX: !isVert, scaleY: isVert,
			scaleFrom: zk.opera ? 0 : 1,
			scaleMode: {originalHeight: dims.height, originalWidth: dims.width},
			restoreAfterFinish: true
		});
		if (!opts.afterSetup)
			opts.afterSetup = function(effect) {
				var e = effect.node;
				zDom.makePositioned(e);
				switch (anchor) {
				case 't':
					zDom.makeClipping(e);
					zDom.setStyle(e, {height: '0px'});
					zDom.show(e);
					break;
				case 'b':
					orig.ot = dims.top + dims.height;
					zDom.makeClipping(e);
					zDom.setStyle(e, {height: '0px', top: orig.ot + 'px'});
					zDom.show(e);
					break;
				case 'l':
					zDom.makeClipping(e);
					zDom.setStyle(e, {width: '0px'});
					zDom.show(e);
					break;
				case 'r':
					orig.ol = dims.left + dims.width;
					zDom.makeClipping(e);
					zDom.setStyle(e, {width: '0px', left: orig.ol + 'px'});
					zDom.show(e);
					break;
				}
			};
		if (!opts.afterUpdateInternal)
			opts.afterUpdateInternal = function(effect){
				var e = effect.node;
				if (anchor == 'b')
					zDom.setStyle(e, {top: (orig.ot - zk.parseInt(effect.node.style.height)) + 'px'});
				else if (anchor == 'r')
					zDom.setStyle(e, {left: (orig.ol - zk.parseInt(effect.node.style.width)) + 'px'});
			};
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function(effect) {
				var e = effect.node;
				zDom.undoClipping(e);
				zDom.undoPositioned(e);
				zDom.setStyle(e, {top: orig.t, left: orig.l});
			};
		return new zk.eff.Scale(element, 100, opts);
	},
	slideUp: function(element, opts) {
		var anchor = opts ? opts.anchor || 't': 't';
		element = zDom.$(element);
		zDom.cleanWhitespace(element);

		var orig = {t: zDom.getStyle(element, 'top'), l: zDom.getStyle(element, 'left')},
			isVert = anchor == 't' || anchor == 'b';

		opts = zk.$default(opts, {
			scaleContent: false, 
			scaleX: !isVert, scaleY: isVert,
			scaleMode: 'box', scaleFrom: 100,
			restoreAfterFinish: true});
		if (!opts.beforeStartInternal)
			opts.beforeStartInternal = function(effect) {
				var e = effect.node;
				zDom.makePositioned(e);
				zDom.makeClipping(e);
				zDom.show(e);
				orig.ot = e.offsetTop;
				orig.oh = e.offsetHeight;
				orig.ol = e.offsetLeft;
				orig.ow = e.offsetWidth;
			};
		if (!opts.afterUpdateInternal)
			opts.afterUpdateInternal = function (effect) {
				var e = effect.node;
				if (anchor == 'b')
					zDom.setStyle(e, {top: (orig.ot + orig.oh - zk.parseInt(effect.node.style.height)) + 'px'});
				else if (anchor == 'r')
					zDom.setStyle(e, {left: (orig.ol + orig.ow - zk.parseInt(effect.node.style.width)) + 'px'});
			};
		if (!opts.beforeFinishInternal)
			opts.beforeFinishInternal = function (effect) {
				zDom.hide(effect.node);
			};
		if (!opts.afterFinishInternal)
			opts.afterFinishInternal = function (effect) {
				var e = effect.node;
				zDom.undoClipping(e);
				zDom.undoPositioned(e);
				zDom.setStyle(e, {top: orig.t, left: orig.l});
			};
		return new zk.eff.Scale(element, zk.opera ? 0 : 1, opts);
	}
};

zk.eff = {}
zk.eff.Base_ = zk.$extends(zk.Object, {
	position: null,
	start: function(opts) {
		this.opts = zk.$default(opts, zEffect._defOpts);
		this.name = this.opts.name || "Base";
		this.currentFrame = 0;
		this.state = 'idle';
		this.startOn = this.opts.delay*1000;
		this.finishOn = this.startOn + (this.opts.duration*1000);
		this.event('beforeStart');

		if(!this.opts.sync) zEffect._Queue.add(this);
	},
	loop: function(timePos) {
		if(timePos >= this.startOn) {
			if(timePos >= this.finishOn) {
				this.render(1.0);
				this.cancel();
				this.event('beforeFinish');
				if(this.finish) this.finish(); 
				this.event('afterFinish');
				return;  
			}
			var pos = (timePos - this.startOn) / (this.finishOn - this.startOn);
			var frame = Math.round(pos * this.opts.fps * this.opts.duration);
			if(frame > this.currentFrame) {
				this.render(pos);
				this.currentFrame = frame;
			}
		}
	},
	render: function(pos) {
		if(this.state == 'idle') {
			this.state = 'running';
			this.event('beforeSetup');
			if(this.setup) this.setup();
			this.event('afterSetup');
		}
		if(this.state == 'running') {
			if(this.opts.transition) pos = this.opts.transition(pos);
			pos *= (this.opts.to-this.opts.from);
			pos += this.opts.from;
			this.position = pos;
			this.event('beforeUpdate');
			if(this.update) this.update(pos);
			this.event('afterUpdate');
		}
	},
	cancel: function() {
		if(!this.opts.sync)
			zEffect._Queue.remove(this);
		this.state = 'finished';
	},
	event: function(eventName) {
		if(this.opts[eventName + 'Internal'])
			this.opts[eventName + 'Internal'](this);
		if(this.opts[eventName]) this.opts[eventName](this);
	}
});

zk.eff.Parallel = zk.$extends(zk.eff.Base_, {
	$init: function(effects, opts) {
		this._effects = effects || [];
		this.start(opts);
	},
	update: function(position) {
		for (var j = 0, effs = this._effects, len = effs.length; j < len;)
			effs[j++].render(position);
	},
	finish: function(position) {
		for (var j = 0, effs = this._effects, len = effs.length; j < len;) {
			var ef = effs[j++];
			ef.render(1.0);
			ef.cancel();
			ef.event('beforeFinish');
			if(ef.finish) ef.finish(position);
			ef.event('afterFinish');
		}
	}
});

zk.eff.Opacity = zk.$extends(zk.eff.Base_, {
	$init: function(element, opts) {
		var e = this.node = zDom.$(element);
		// make this work on IE on elements without 'layout'
		if(zk.ie && (!e.currentStyle.hasLayout))
			zDom.setStyle(e, {zoom: 1});
		opts = zk.$default(opts, {to: 1.0});
		if (!opts.from) opts.from = zDom.getOpacity(e) || 0.0,
		this.start(opts);
	},
	update: function(position) {
		zDom.setOpacity(this.node, position);
	}
});

zk.eff.Move = zk.$extends(zk.eff.Base_, {
	$init: function(element, opts) {
		this.node = zDom.$(element);
		opts = zk.$default(opts, {x: 0, y: 0, mode: 'relative'});
		this.start(opts);
	},
	setup: function() {
	// Bug in Opera: Opera returns the "real" position of a static element or
	// relative element that does not have top/left explicitly set.
	// ==> Always set top and left for position relative elements in your stylesheets 
	// (to 0 if you do not need them) 
		var e = this.node;
		zDom.makePositioned(e);
		this.originalLeft = parseFloat(zDom.getStyle(e, 'left') || '0');
		this.originalTop = parseFloat(zDom.getStyle(e, 'top')  || '0');
		if(this.opts.mode == 'absolute') {
			// absolute movement, so we need to calc deltaX and deltaY
			this.opts.x -= this.originalLeft;
			this.opts.y -= this.originalTop;
		}
	},
	update: function(position) {
		zDom.setStyle(this.node, {
			left: Math.round(this.opts.x  * position + this.originalLeft) + 'px',
			top:  Math.round(this.opts.y  * position + this.originalTop)  + 'px'
		});
	}
});

zk.eff.Scale = zk.$extends(zk.eff.Base_, {
	$init: function(element, percent, opts) {
		this.node = zDom.$(element);
		opts = zk.$default(opts, {
			scaleX: true,
			scaleY: true,
			scaleContent: true,
			scaleFromCenter: false,
			scaleMode: 'box', // 'box' or 'contents' or {} with provided values
			scaleFrom: 100.0,
			scaleTo: percent
			});
		this.start(opts);
	},
	setup: function() {
		var el = this.node;
		this.restoreAfterFinish = this.opts.restoreAfterFinish || false;
		this.nodePositioning = zDom.getStyle(el, 'position');
		
		this.originalStyle = {};
		for (var j = 0, styles=['top','left','width','height','fontSize'],
		len = styles.length; j < len;) {
			var s = styles[j++]
			this.originalStyle[s] = el.style[s];
		}

		this.originalTop = el.offsetTop;
		this.originalLeft = el.offsetLeft;

		var fontSize = zDom.getStyle(el, 'font-size') || '100%';
		for (var j = 0, types=['em','px','%','pt'],
		len = types.length; j < len;) {
			var t = types[j++];
			if(fontSize.indexOf(t) > 0) {
				this.fontSize = parseFloat(fontSize);
				this.fontSizeType = t;
			}
		}

		this.factor = (this.opts.scaleTo - this.opts.scaleFrom)/100;

		this.dims = null;
		if(this.opts.scaleMode=='box')
			this.dims = [el.offsetHeight, el.offsetWidth];
		if(/^content/.test(this.opts.scaleMode))
			this.dims = [el.scrollHeight, el.scrollWidth];
		if(!this.dims)
			this.dims = [this.opts.scaleMode.originalHeight,
				this.opts.scaleMode.originalWidth];
	},
	update: function(position) {
		var currentScale = (this.opts.scaleFrom/100.0) + (this.factor * position);
		if(this.opts.scaleContent && this.fontSize)
			zDom.setStyle(this.node, {fontSize: this.fontSize * currentScale + this.fontSizeType});
		this.setDimensions(this.dims[0] * currentScale, this.dims[1] * currentScale);
	},
	finish: function(position) {
		if(this.restoreAfterFinish)
			zDom.setStyle(this.node, this.originalStyle);
	},
	setDimensions: function(height, width) {
		var d = {};
		if(this.opts.scaleX) d.width = Math.round(width) + 'px';
		if(this.opts.scaleY) d.height = Math.round(height) + 'px';
		if(this.opts.scaleFromCenter) {
			var topd = (height - this.dims[0])/2;
			var leftd = (width  - this.dims[1])/2;
			if(this.nodePositioning == 'absolute') {
				if(this.opts.scaleY) d.top = this.originalTop-topd + 'px';
				if(this.opts.scaleX) d.left = this.originalLeft-leftd + 'px';
			} else {
				if(this.opts.scaleY) d.top = -topd + 'px';
				if(this.opts.scaleX) d.left = -leftd + 'px';
			}
		}
		zDom.setStyle(this.node, d);
	}
});

zk.eff.ScrollTo = zk.$extends(zk.eff.Base_, {
	$init: function(element, opts) {
		this.node = zDom.$(element);
		this.start(opts || {});
	},
	setup: function() {
		var innerY = zDom.innerY(),
			offsets = zDom.cmOffset(this.node);
		if(this.opts.offset) offsets[1] += this.opts.offset;
		var max = window.innerHeight ? 
			window.height - window.innerHeight :
			document.body.scrollHeight - 
			(document.documentElement.clientHeight ? 
				document.documentElement.clientHeight : document.body.clientHeight);
		this.scrollStart = innerY;
		this.delta = (offsets[1] > max ? max : offsets[1]) - this.scrollStart;
	},
	update: function(position) {
		window.scrollTo(zDom.innerX(), this.scrollStart + (position*this.delta));
	}
});

zEffect._Tranx = {
	sinoidal: function(pos) {
		return (-Math.cos(pos*Math.PI)/2) + 0.5;
	},
	flicker: function(pos) {
		return ((-Math.cos(pos*Math.PI)/4) + 0.75) + Math.random()/4;
	},
	pulse: function(pos, pulses) { 
		pulses = pulses || 5; 
		return Math.round((pos % (1/pulses)) * pulses) == 0 ? 
			((pos * pulses * 2) - Math.floor(pos * pulses * 2)) : 
			1 - ((pos * pulses * 2) - Math.floor(pos * pulses * 2));
	},
	none: function(pos) {
		return 0;
	},
	full: function(pos) {
		return 1;
	}
};
zEffect._Queue = {
	_effects: [],
	_interval: null,

	add: function(effect) {
		var timestamp = zUtl.now(),
			position = typeof effect.opts.queue == 'string' ? 
				effect.opts.queue : effect.opts.queue.position,
			effque = zEffect._Queue,
			effs = effque._effects;

		switch(position) {
		case 'front':
			// move unstarted effects after this effect  
			for (var j = 0, len = effs.length; j < len;) {
				var ef = effs[j++];
				if (ef.state == 'idle') {
					e.startOn  += effect.finishOn;
					e.finishOn += effect.finishOn;
				}
			}
			break;
		case 'with-last':
			for (var j = 0, len = effs.length; j < len;) {
				var v = effs[j++].startOn;
				if (v > timestamp) timestamp = v;
			}
			break;
		case 'end':
			// start effect after last queued effect has finished
			for (var j = 0, len = effs.length; j < len;) {
				var v = effs[j++].finishOn;
				if (v > timestamp) timestamp = v;
			}
			break;
		}

		effect.startOn  += timestamp;
		effect.finishOn += timestamp;

		if(!effect.opts.queue.limit || (effs.length < effect.opts.queue.limit))
			effs.push(effect);

		if(!effque._interval) 
			effque._interval = setInterval(effque.loop, 15);
	},
	remove: function(effect) {
		var effque = zEffect._Queue, effs = effque._effects;
		effs.$remove(effect);
		if(!effs.length) {
			clearInterval(effque._interval);
			effque._interval = null;
		}
	},
	loop: function() {
		var timePos = zUtl.now(), effs = zEffect._Queue._effects;
		for(var i=0, len=effs.length; i<len; i++) 
			if(effs[i])
				effs[i].loop(timePos);
	}
};

zEffect._defOpts = {
  transition: zEffect._Tranx.sinoidal,
  duration:   1.0,   // seconds
  fps:        60.0,  // max. 60fps due to zEffect.Queue implementation
  sync:       false, // true for combining
  from:       0.0,
  to:         1.0,
  delay:      0.0,
  queue:      'parallel'
};

zk.eff.Shadow = zk.$extends(zk.Object, {
	_HTML: zk.ie6Only ? '" class="z-shadow"></div>':
		'" class="z-shadow"><div class="z-shadow-tl"><div class="z-shadow-tr"></div></div>'
		+'<div class="z-shadow-cl"><div class="z-shadow-cr"><div class="z-shadow-cm">&#160;</div></div></div>'
		+'<div class="z-shadow-bl"><div class="z-shadow-br"></div></div></div>',

	$init: function (element, opts) {
		opts = this.opts = zk.$default(opts, {
			left: 4, right: 4, top: 3, bottom: 3
		});
		if (zk.ie6Only) {
			opts.left -= 1;
			opts.right -= 8;
			opts.top -= 2;
			opts.bottom -= 6;
		}

		this.node = element;
		var sdwid = element.id + "$sdw";
		zDom.insertHTMLBefore(element, '<div id="'+sdwid+this._HTML);
		this.shadow = zDom.$(sdwid);
	},
	destroy: function () {
		zDom.remove(this.shadow);
		zDom.remove(this.stackup);
		this.node = this.shadow = this.stackup = null;
	},
	hide: function(){
		this.shadow.style.display = 'none';
		if (this.stackup) this.stackup.style.display = 'none';
	},
	sync: function () {
		var node = this.node, shadow = this.shadow;
		if (!node || !zDom.isVisible(node, true)) {
			this.hide();
			return false;
		}

		for (var c = shadow;;) {
			if (!(c = c.nextSibling) || c.tagName) {
				if (c != node)
					node.parentNode.insertBefore(shadow, node);
				break;
			}
		}
		shadow.style.zIndex = zk.parseInt(zDom.getStyle(node, "zIndex"));

		var opts = this.opts,
			l = node.offsetLeft, t = node.offsetTop,
			w = node.offsetWidth, h = node.offsetHeight,
			wd = w - opts.left + opts.right,
			hgh = h - opts.top + opts.bottom,
			st = shadow.style;
		st.left = (l + opts.left) + "px";
		st.top = (t + opts.top) + "px";
		st.width = zDom.getPositive(wd) + "px";
		st.display = "block";
		if (zk.ie6Only) st.height = zDom.getPositive(hgh) + "px";
		else {
			var cns = shadow.childNodes;
			cns[1].style.height = zDom.getPositive(hgh - cns[0].offsetHeight - cns[2].offsetHeight) + "px";
		}

		var stackup = this.stackup;
		if(opts.stackup && node) {
			if(!stackup)
				stackup = this.stackup =
					zDom.makeStackup(node, node.id + '$sdwstk', shadow);

			st = stackup.style;
			st.left = l +"px";
			st.top = t +"px";
			st.width = w +"px";
			st.height = h +"px";
			st.zIndex = zk.parseInt(zDom.getStyle(node, "zIndex"));
			st.display = "block";
		}
		return true;
	},
	getBottomElement: function () {
		return this.stackup || this.shadow;
	}
});

zk.eff.FullMask = zk.$extends(zk.Object, {
	$init: function (opts) {
		opts = opts || {};
		var mask = this.mask = opts.mask;
		if (this.mask) {
			if (opts.anchor)
				opts.anchor.parentNode.insertBefore(mask, opts.anchor);
			if (opts.id) mask.id = opts.id;
			if (opts.zIndex != null) mask.style.zIndex = opts.zIndex;
			if (opts.visible == false) mask.style.display = 'none';
		} else {
			var maskId = opts.id || 'z_mask',
				html = '<div id="' + maskId + '" class="z-modal-mask"';//FF: don't add tabIndex
			if (opts.zIndex != null || opts.visible == false) {
				html += ' style="';
				if (opts.zIndex != null) html += 'z-index:' + opts.zIndex;
				if (opts.visible == false) html += ';display:none';
				html +='"';
			}

			html += '></div>'
			if (opts.anchor)
				opts.anchor.insertAdjacentHTML('beforeBegin', html);
			else
				document.body.insertAdjacentHTML('beforeEnd', html);
			mask = this.mask = zDom.$(maskId);
		}
		if (opts.stackup)
			this.stackup = zDom.makeStackup(mask, mask.id + '$mkstk');

		this._syncPos();

		zEvt.listen(mask, "mousemove", zEvt.stop);
		zEvt.listen(mask, "click", zEvt.stop);
		zEvt.listen(window, "resize", this.proxy(this._syncPos, '_pxSyncPos'));
		zEvt.listen(window, "scroll", this._pxSyncPos);
	},
	destroy: function () {
		var mask = this.mask;
		zEvt.unlisten(mask, "mousemove", zEvt.stop);
		zEvt.unlisten(mask, "click", zEvt.stop);
		zEvt.unlisten(window, "resize", this._pxSyncPos);
		zEvt.unlisten(window, "scroll", this._pxSyncPos);
		zDom.remove(mask);
		zDom.remove(this.stackup);
		this.mask = this.stackup = null;
	},
	hide: function () {
		this.mask.style.display = 'none';
		if (this.stackup) this.stackup.style.display = 'none';
	},
	sync: function (el) {
		if (!zDom.isVisible(el, true)) {
			this.hide();
			return;
		}

		if (this.mask.nextSibling != el) {
			var p = el.parentNode;
			p.insertBefore(this.mask, el);
			if (this.stackup)
				p.insertBefore(this.stackup, this.mask);
		}

		var st = this.mask.style;
		st.display = 'block';
		st.zIndex = el.style.zIndex;
		if (this.stackup) {
			st = this.stackup.style;
			st.display = 'block';
			st.zIndex = el.style.zIndex;
		}
	},
	/** Position a mask to cover the whole browser window. */
	_syncPos: function () {
		var n = this.mask;
		var ofs = zDom.toStyleOffset(n, zDom.innerX(), zDom.innerY()),
			st = n.style;
		st.left = ofs[0] + "px";
		st.top = ofs[1] + "px";
		st.width = zDom.innerWidth() + "px";
		st.height = zDom.innerHeight() + "px";
		st.display = "block";

		n = this.stackup;
		if (n) {
			n = n.style;
			n.left = st.left;
			n.top = st.top;
			n.width = st.width;
			n.height = st.height;
		}
	}
});

zk.eff.Mask = zk.$extends(zk.Object, {
	$init: function(opts) {
		opts = opts || {};
		var anchor = (typeof opts.anchor == "string") ? zDom.$(opts.anchor) : opts.anchor;
		
		if (!anchor || !zDom.isRealVisible(anchor, true)) return; //nothing do to.
		
		var maskId = opts.id || 'z_applymask',
			progbox = zDom.$(maskId);
		
		if (progbox) return this;
		
		var msg = opts.msg || (window.mesg?mesg.LOADING:"Loading..."),
			n = document.createElement("DIV");
		
		document.body.appendChild(n);
		var xy = opts.offset || zDom.revisedOffset(anchor), 
			w = opts.width || zDom.offsetWidth(anchor),
			h = opts.height || zDom.offsetHeight(anchor),
			html = '<div id="'+maskId+'" style="visibility:hidden">' 
				+ '<div class="z-apply-mask" style="display:block;top:' + xy[1]
				+ 'px;left:' + xy[0] + 'px;width:' + w + 'px;height:' + h + 'px;"></div>'
				+ '<div id="'+maskId+'$z_loading" class="z-apply-loading"><div class="z-apply-loading-indicator">'
				+ '<span class="z-apply-loading-icon"></span> '
				+ msg+ '</div></div></div>';
		zDom.setOuterHTML(n, html);
		var loading = zDom.$(maskId+"$z_loading"),
			mask = this.mask = zDom.$(maskId);
		
		if (loading) {
			if (loading.offsetHeight > h) 
				loading.style.height = zDom.revisedHeight(loading, h) + "px";
			if (loading.offsetWidth > w)
				loading.style.width = zDom.revisedWidth(loading, w) + "px";
			loading.style.top = (xy[1] + ((h - loading.offsetHeight) /2)) + "px";
			loading.style.left = (xy[0] + ((w - loading.offsetWidth) /2)) + "px";
		}
		
		mask.style.visibility = "";
	},
	destroy: function () {
		var mask = this.mask;
		zDom.remove(mask);
		this.mask = null;
	}
});

zk.eff.Tooltip = zk.$extends(zk.Object, {
	beforeBegin: function (ref) {
		var overTip = this._tip == ref;
		if (overTip) this._clearClosing(); //not close tip if over tip
		return !overTip;//disable tip in tip
	},
	begin: function (tip, ref) {
		if (this._tip != tip) {
			this.close_();

			this._inf = {
				tip: tip, ref: ref,
				timer: setTimeout(this.proxy(this.open_, '_pxopen'), zk.tipDelay)
			};
		} else
			this._clearClosing();
	},
	end: function (ref) {
		if (this._ref == ref || this._tip == ref)
			this._tmClosing =
				setTimeout(this.proxy(this.close_, '_pxclose'), 100);
			//don't cloes immediate since user might move from ref to toolip
		else
			this._clearOpening();
	},
	open_: function () {
		var inf = this._inf;
		if (inf) {
			var tip = this._tip = inf.tip,
				ref = this._ref = inf.ref;
			this._inf = null;
			tip.open(ref, zk.currentPointer, null, {sendOnOpen:true});
		}
	},
	close_: function () {
		this._clearOpening();
		this._clearClosing();

		var tip = this._tip;
		if (tip) {
			this._tip = this._ref = null;
			tip.close({sendOnOpen:true});
		}
	},
	_clearOpening: function () {
		var inf = this._inf;
		if (inf) {
			this._inf = null;
			clearTimeout(inf.timer);
		}
	},
	_clearClosing: function () {
		var tmClosing = this._tmClosing;
		if (tmClosing) {
			this._tmClosing = null;
			clearTimeout(tmClosing);
		}
	}
});
zTooltip = new zk.eff.Tooltip();
