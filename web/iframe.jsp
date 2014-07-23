<%--
    basiclti - Building Block to provide support for Basic LTI
    Copyright (C) 2014  Stephen P Vickers

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Contact: stephen@spvsoftwareproducts.com
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.lti.Constants,
                org.oscelot.blackboard.lti.Tool"
        errorPage="error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:learningSystemPage  title="${bundle['page.course.tool.title']}" onLoad="doOnLoad()">
  <bbNG:pageHeader>
<%
  B2Context b2Context = new B2Context(request);
  String toolId = b2Context.getRequestParameter(Constants.TOOL_ID, b2Context.getSetting(false, true, "tool.id", ""));
  Tool tool = new Tool(b2Context, toolId);

  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("tool", tool);
  pageContext.setAttribute("query", request.getQueryString() + "&if=true");
  if (b2Context.getContext().hasContentContext()) {
%>
    <bbNG:breadcrumbBar />
<%
  } else {
    pageContext.setAttribute("courseId", b2Context.getRequestParameter("course_id", ""));
%>
    <bbNG:breadcrumbBar environment="COURSE">
      <bbNG:breadcrumb href="tools.jsp?course_id=${courseId}" title="${bundle['plugin.name']}" />
      <bbNG:breadcrumb title="${tool.name}" />
    </bbNG:breadcrumbBar>
<%
  }
%>
    <bbNG:pageTitleBar showTitleBar="false" title="${bundle['page.course.tool.title']}" />
  </bbNG:pageHeader>
  <bbNG:cssBlock>
<style type="text/css">
div#containerdiv, div#frame {
  padding: 0;
  margin: 0;
}
</style>
  </bbNG:cssBlock>
  <bbNG:jsBlock>
<script language="javascript" type="text/javascript">
//<![CDATA[
var resizeTimeoutId;
function getHeight() {
  var height = window.innerHeight;  // Firefox
  if (document.body.clientHeight)	{
    height = document.body.clientHeight;  // IE
  }
  var el = document.getElementById("frame");
  height = height - el.offsetTop - 85;
  return parseInt(height) + "px";
}
function doResize() {
  var el = document.getElementById("if");
  if (el) {
    var height = getHeight();
    if (height != el.height) {
      el.height = height;
    }
  }
}
function doOnResize() {
  window.clearTimeout(resizeTimeoutId);
  resizeTimeoutId = window.setTimeout('doResize();', 10);
}
function doOnLoad() {
  var el = document.getElementById("frame");
  el.innerHTML = '<iframe id="if" src="window.jsp?${query}" width="100%" height="' + getHeight() + '" frameborder="0" />';
  if (document.body.onresize) {
    document.body.onresize=doOnResize;
  } else {
    window.onresize=doOnResize;
  }
}
//]]>
</script>
  </bbNG:jsBlock>
<div id="frame"></div>
</bbNG:learningSystemPage>
