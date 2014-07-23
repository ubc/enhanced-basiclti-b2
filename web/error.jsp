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
        import="java.util.logging.Level,
                java.util.logging.Logger,
                com.spvsoftwareproducts.blackboard.utils.B2Context"
        isErrorPage="true" %>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<%
  B2Context b2Context = new B2Context(request);

  Logger.getLogger(Object.class.getName()).log(Level.SEVERE, b2Context.getResourceString("plugin.name"), exception);

  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
%>
<bbNG:genericPage>
  <bbNG:pageHeader>
    <bbNG:pageTitleBar iconUrl="images/lti.gif" showTitleBar="true" title="${bundle['plugin.name']}: ${bundle['page.error.title']}"/>
  </bbNG:pageHeader>
<bbNG:breadcrumbBar/>
<p>
${bundle['page.error.introduction']}
</p>
</bbNG:genericPage>