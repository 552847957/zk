<%@ page contentType="text/css;charset=UTF-8" %>
<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%-- Borderlayout --%>
.z-border-layout {
    width:100%; height:100%; overflow:hidden; background-color:#CDE6F5;	border:0 none;
	position: relative;	visibility: hidden;
}
.z-border-layout-tool {
	overflow: hidden; width: 15px; height: 15px; float: right; cursor: pointer;
	background: transparent url(${c:encodeURL('~./zul/img/panel/tool-btn.gif')}) no-repeat;
	margin-left: 2px;
}
<%-- LayoutRegion --%>
.z-west-nested, .z-center-nested, .z-east-nested, .z-north-nested, .z-south-nested {
	border:0 none;
}
.z-west, .z-center, .z-east, .z-north, .z-south {
	border:1px solid #B1CBD5; position:absolute; overflow:hidden; background-color:white;
}
.z-west-noborder, .z-center-noborder, .z-east-noborder,
	.z-north-noborder, .z-south {
	border:0 none;
}
.z-west {
    z-index: 12;
}
.z-center {
	z-index: 8;
}
.z-east {
    z-index: 10;
}
.z-north {
    z-index: 16;
}
.z-south {
	z-index: 14;
} 
.z-east-split, .z-west-split {
    position: absolute; height: 6px; width: 6px; line-height: 1px; font-size: 1px;
    z-index: 12; background-color: #C4DCFB;
    background-image:url("${c:encodeURL('~./zul/img/splt/splt-h.png')}");
    background-position: left; cursor: e-resize; cursor: col-resize;
}
.z-north-split, .z-south-split {
    position: absolute; height: 6px; width: 6px; line-height: 1px; font-size: 1px;
    z-index: 12; background-color: #C4DCFB;
    background-image:url("${c:encodeURL('~./zul/img/splt/splt-v.png')}");
    background-position: top; cursor: s-resize; cursor: row-resize;
}
.z-west-split {
    z-index: 11;
}
.z-east-split {
    z-index: 9;
}
.z-north-split {
    z-index: 15;
}
.z-south-split {
	z-index: 13;
}
.z-west-header, .z-center-header, .z-east-header,
	.z-north-header, .z-south-header {
	overflow: hidden; zoom: 1; color: #15428b; font: normal 11px tahoma, arial, verdana, sans-serif;
	padding: 5px 3px 4px 5px; border-bottom: 1px solid #B1CBD5; line-height: 15px; 
	background:transparent url(${c:encodeURL('~./zul/img/panel/panel-tb.png')}) repeat-x 0 -1px;
	white-space: nowrap;font-weight:bold;
}
.z-south-collapse {
	background-position: 0 -195px;
}
.z-south-collapse-over {
	background-position: -15px -195px;
}
.z-north-collapse {
	background-position: 0 -210px;
}
.z-north-collapse-over{
	background-position: -15px -210px;
}
.z-west-collapse {
	background-position: 0 -180px;
}
.z-west-collapse-over {
	background-position: -15px -180px;
}
.z-east-collapse {
	background-position: 0 -165px;
}
.z-east-collapse-over {
	background-position: -15px -165px;
}
.z-south-expand {
	background-position: 0 -210px;
}
.z-south-expand-over {
	background-position: -15px -210px;
}
.z-north-expand {
	background-position: 0 -195px;
}
.z-north-expand-over {
	background-position: -15px -195px;
}
.z-west-expand {
	background-position: 0 -165px;
}
.z-west-expand-over {
	background-position: -15px -165px;
}
.z-east-expand {
	background-position: 0 -180px;
}
.z-east-expand-over {
	background-position: -15px -180px;
}
.z-north-expand, .z-south-expand {
	float: right; margin: 3px;
}
.z-east-expand, .z-west-expand {
	float: none; margin: 3px auto;
}
.z-east-collapsed, .z-west-collapsed, .z-south-collapsed, .z-north-collapsed {
	position: absolute; background-color: #E1F1FB; width: 20px; height: 20px;
	border: 1px solid #98C0F4; z-index:20; overflow: hidden;
}
.z-east-collapsed-over, .z-west-collapsed-over, .z-south-collapsed-over, .z-north-collapsed-over {
	cursor: pointer; background-color: #E8FFFC;
}
.z-west-collapsed {
    z-index: 12;
}
.z-east-collapsed {
    z-index: 10;
}
.z-north-collapsed {
    z-index: 16;
}
.z-south-collapsed {
	z-index: 14;
} 
<%-- Column Layout--%>
.z-column-layout {
	visibility: hidden; overflow: hidden; zoom: 1;
}
.z-column-children {
    float: left; padding: 0; margin: 0; overflow: hidden; zoom: 1;
}
.z-column-children-cnt {
    overflow: hidden;
}
.z-column-children-body {
    overflow: hidden; zoom: 1;
}
.z-column-layout-inner {
    overflow: hidden;
}
<%-- Fisheyebar--%>
.z-fisheye-text {
	font-family: Arial, Helvetica, sans-serif; background-color: #eee; border: 2px solid #666;
	padding: 2px; text-align: center; position: absolute; display: none;
}
.z-fisheye-img {
	border: 0px; position: absolute; width: 100%; height: 100%;
}
.z-fisheye {
	position: absolute; z-index: 2;
}
.z-fisheyebar {
	visibility: hidden;
}
.z-fisheyebar-inner {
	position: relative;
}
<%-- Detail --%>
.z-detail {
	height: 100%; padding: 0!important; overflow: hidden;
	-o-text-overflow: ellipsis; text-overflow: ellipsis; white-space:nowrap;
}
tr.z-row td.z-detail-outer {
	background: #bae2f0 url(${c:encodeURL('~./zul/img/grid/detail-bg.gif')}) repeat-y left;
	//background: #EFF8FB url(${c:encodeURL('~./zul/img/grid/ms_hd.gif')}) repeat-y right;
	vertical-align: top; border-top: none; border-left: 1px solid white;
	border-right: 1px solid #CCC; border-bottom: 1px solid #DDD;
}
.z-detail-img {
	width: 100%; height: 18px; background-position: 4px 2px;
	background-repeat: no-repeat; background-color: transparent; 
}
.z-detail-expanded .z-detail-img {
	background-position: -21px 2px;
}