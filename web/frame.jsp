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
        import="java.util.Map,
                org.oscelot.blackboard.lti.Utils"
        errorPage="error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<%@include file="lti_props.jsp" %>
<%
  if (params != null) {
    String target = "_self";
    String full = b2Context.getRequestParameter("full", "");
    if (full.equals(Constants.DATA_TRUE)) {
      target = "_parent";
    }
%>
<html>
<head>
<script language="javascript" type="text/javascript">
function doOnLoad() {
  document.forms[0].submit();
}
window.onload=doOnLoad;
</script>
</head>
<body>
<p>${bundle['page.course_tool.tool.redirect.label']}</p>
<form action="<%=toolURL%>" method="post" target="<%=target%>">
<%
    for (Map.Entry<String,String> entry : params) {
      String name = Utils.htmlSpecialChars(entry.getKey());
      String value = Utils.htmlSpecialChars(entry.getValue());
%>
  <input type="hidden" name="<%=name%>" value="<%=value%>">
<%
    }
%>
</form>
<%
  } else {
    b2Context.setReceipt(b2Context.getResourceString("page.receipt.error"), false);
  }
%>
</body>
</html>