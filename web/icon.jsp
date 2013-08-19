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
      1.1.1  7-Aug-10
      1.1.2  9-Oct-10
      1.1.3  1-Jan-11
      1.2.0 17-Sep-11
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
      2.0.1 20-May-12
      2.1.0 18-Jun-12  Added support for icons on content pages and default domain icons
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
--%>
<%@page import="org.oscelot.blackboard.basiclti.Constants,
                org.oscelot.blackboard.basiclti.Tool,
                com.spvsoftwareproducts.blackboard.utils.B2Context"
        errorPage="error.jsp"%>
<%
  B2Context b2Context = new B2Context(request);

  String toolId = b2Context.getSetting(false, true,
     Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID,
     b2Context.getRequestParameter(Constants.TOOL_ID, ""));
  Tool tool = new Tool(b2Context, toolId);
  String icon = tool.getDisplayIcon();
  if (icon.length() <= 0) {
    tool = tool.getDomain();
    if (tool != null) {
      icon = tool.getDisplayIcon();
    }
  }

  if (icon.length() <= 0) {
    icon = "images/lti.gif";
  }

  response.sendRedirect(icon);
%>
