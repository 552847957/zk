<%@ page contentType="text/css;charset=UTF-8" %>
<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

h1 {
	font-family: Tahoma, Arial, Helvetica, sans-serif;
	font-size: x-large; font-weight: bold; color: #250070;
	letter-spacing: -1px; margin-top: 3pt;
}
h2 {
	font-family: Tahoma, Arial, Helvetica, sans-serif;
	font-size: large; font-weight: bold; color: #200066;
}
h3 {
	font-family: Tahoma, Arial, Helvetica, sans-serif;
	font-size: medium; font-weight: bold; color: #100050;
}
h4 {
	font-family: Verdana, Tahoma, Arial, Helvetica, sans-serif;
	font-size: small; font-weight: bold; color: #107080;
}
h5 {
	font-family: Tahoma, Arial, Helvetica, sans-serif;
	font-size: small; font-weight: bold; color: #603080;
}
h6 {
	font-family: Tahoma, Arial, Helvetica, sans-serif;
	font-size: small; font-weight: normal; color: #404040;
}

h1 em	{color: #dd0000}

dt {
	margin: 0.5em 0 0.3em 0;
	font-weight: bold;
}
dd {
	margin: 0 0 0 0.8em;
}

li	{margin-top: 2pt}
ul li	{list-style: url(${c:encodeURL('~./img/bullet1.gif')}) disc}
ul ul li	{list-style: url(${c:encodeURL('~./img/bullet2.gif')}) circle}
ul ul ul li	{list-style: url(${c:encodeURL('~./img/bullet3.gif')}) square}

code {
	font-family: "Lucida Console", "Courier New", Courier, mono;
	font-size: x-small;  font-weight: normal;
}
dfn {
	font-family: "Lucida Console", "Courier New", Courier, mono;
	font-size: x-small; font-style: normal;
}

<%-- The hyperlink's style class. --%>
.link {cursor:hand; cursor:pointer;}
