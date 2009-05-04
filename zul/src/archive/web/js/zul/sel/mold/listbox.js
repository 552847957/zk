/* listbox.js

	Purpose:
		
	Description:
		
	History:
		Mon May  4 15:34:02     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
function (out) {
	var uuid = this.uuid,
		zcls = this.getZclass(),
		innerWidth = this.getInnerWidth(),
		wdAttr = innerWidth == '100%' ? ' width="100%"' : '',
		wdStyle =  innerWidth != '100%' ? 'width:' + innerWidth : '',
		inPaging = this.inPagingMold(), pgpos,
		tag = zk.ie || zk.gecko ? "a" : "button";

	out.push('<div', this.domAttrs_(), '>');

	if (inPaging && this.paging) {
		pgpos = this.getPagingPosition();
		if (pgpos == 'top' || pgpos == 'both') {
			out.push('<div id="', uuid, '$pgit" class="', zcls, '-pgi-t">');
			this.paging.redraw(out);
			out.push('</div>');
		}
	}

	if(this.listhead){
		out.push('<div id="', uuid, '$head" class="', zcls, '-header">',
			'<table', wdAttr, zUtl.cellps0,
			' style="table-layout:fixed;', wdStyle,'">');
		this.domFaker_(out, '$hdfaker', zcls);
		
		for (var hds = this.heads, j = 0, len = hds.length; j < len;)
			hds[j++].redraw(out);
	
		out.push('</table></div>');
	}
	out.push('<div id="', uuid, '$body" class="', zcls, '-body"');

	var hgh = this.getHeight();
	if (hgh) out.push(' style="overflow:hidden;height:', hgh, '"');
	else if (this.getRows() > 1) out.push(' style="overflow:hidden;height:"', this.getRows() * 15, 'px"');
	
	out.push('><table', wdAttr, zUtl.cellps0, ' id="', uuid, '$cave"');
	if (this.isFixedLayout())
		out.push(' style="table-layout:fixed;', wdStyle,'"');		
	out.push('>');
	
	if(this.listhead)
		this.domFaker_(out, '$bdfaker', zcls);


	for (var items = this.items, j = 0, len = items.length; j < len;)
		items[j++].redraw(out);

	out.push('</table><', tag, ' id="', uuid,
		'$a" tabindex="-1" onclick="return false;" href="javascript:;" style="position:absolute;left:0px;top:0px;padding:0 !important;margin:0 !important;border:0 !important;background:transparent !important;font-size:1px !important;width:1px !important;height:1px !important;-moz-outline:0 none;outline:0 none;-moz-user-select:text;-khtml-user-select:text;"></',
		tag, '>');
	out.push("</div>");

	if (this.listfoot) {
		out.push('<div id="', uuid, '$foot" class="', zcls, '-footer">',
			'<table', wdAttr, zUtl.cellps0, ' style="table-layout:fixed;', wdStyle,'">');
		if (this.listhead) 
			this.domFaker_(out, '$ftfaker', zcls);
			
		this.listfoot.redraw(out);
		out.push('</table></div>');
	}

	if (pgpos == 'bottom' || pgpos == 'both') {
		out.push('<div id="', uuid, '$pgib" class="', zcls, '-pgi-b">');
		this.paging.redraw(out);
		out.push('</div>');
	}
	out.push('</div>');
}