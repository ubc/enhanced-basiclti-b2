<%--
    basiclti - Building Block to provide support for Basic LTI
    Copyright (C) 2013  Stephen P Vickers

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

    Version history:
      1.0.0  9-Feb-10  First public release
      1.1.0  2-Aug-10  Renamed class domain to org.oscelot
                       Updated for alternative schema name in Learn 9.1
      1.1.1  7-Aug-10
      1.1.2  9-Oct-10  Split connection to tool code according to where it is to be opened
      1.1.3  1-Jan-11
      1.2.0 17-Sep-11
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
      2.0.1 20-May-12  Fixed page doctype
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12  Added support for launching from a module outside a course
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
      3.0.0 30-Oct-13
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.Map,
                org.oscelot.blackboard.lti.Utils"
        errorPage="error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:learningSystemPage title="${bundle['page.course_tool.tool.title']}" onLoad="doOnLoad()">
<%@include file="lti_props.jsp" %>
<%
  if (params != null) {
    String target = "_self";
    String full = b2Context.getRequestParameter("full", "");
    if (full.equals(Constants.DATA_TRUE)) {
      target = "_parent";
    }
%>
<bbNG:jsBlock>
<script language="javascript" type="text/javascript">
function doOnLoad() {
  document.forms[0].submit();
}
</script>
</bbNG:jsBlock>
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
</bbNG:learningSystemPage>
