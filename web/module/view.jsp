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
      2.2.0  2-Sep-12  Added to release
      2.3.0  5-Nov-12  Added mapping for institution roles
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
      3.0.0 30-Oct-13
--%>
<%@page import="java.util.List,
                java.util.ArrayList,
                java.util.Date,
                java.util.Calendar,
                blackboard.portal.data.Module,
                blackboard.servlet.data.ngui.CollapsibleListItem,
                blackboard.platform.intl.BbLocale,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.lti.Tool,
                org.oscelot.blackboard.lti.DashboardFeed,
                org.oscelot.blackboard.lti.Constants,
                org.oscelot.blackboard.lti.Utils"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG" %>
<%
  B2Context b2Context = new B2Context(request);

  Module module = (Module)request.getAttribute("blackboard.portal.data.Module");
  String courseId = b2Context.getRequestParameter("course_id", "");

  String name = b2Context.getVendorId() + "-" + b2Context.getHandle() + "-" + module.getId().toExternalString();
  b2Context.getRequest().getSession().setAttribute(name + "-launch", b2Context.getRequest().getSession().getAttribute(name));

  String toolId = b2Context.getSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID,
      b2Context.getSetting(false, true, Constants.MODULE_TOOL_ID, ""));
  boolean allowLaunch = b2Context.getSetting(false, true, Constants.MODULE_LAUNCH, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  boolean showLaunch = allowLaunch && b2Context.getSetting(false, true, Constants.MODULE_LAUNCH_BUTTON, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  Tool tool = new Tool(b2Context, toolId);
  String launchUrl = "";
  if (allowLaunch) {
    launchUrl = b2Context.getPath() + "tool.jsp?" +
       Constants.TOOL_MODULE + "=" + module.getId().toExternalString() +
       "&amp;" + Constants.TAB_PARAMETER_NAME + "=" + b2Context.getRequestParameter(Constants.TAB_PARAMETER_NAME, "");
    if (courseId.length() > 0) {
      launchUrl += "&amp;course_id=" + courseId;
    }
  }
  String launchText = b2Context.getResourceString("page.module.view.launch") + " " + tool.getName();

  DashboardFeed feed = new DashboardFeed(b2Context, module, tool, launchUrl);

  List<CollapsibleListItem> listItems = feed.getItems();

  String target = "_self";
  if (!tool.getSplash().equals(Constants.DATA_TRUE) && !tool.getUserHasChoice()) {
    target = tool.getWindowName();
  }

  BbLocale locale = new BbLocale();
  String dateString = locale.formatDateTime(feed.getDate(), BbLocale.Date.MEDIUM, BbLocale.Time.SHORT);

  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("dateString", dateString);
  pageContext.setAttribute("launchUrl", launchUrl);
  pageContext.setAttribute("launchText", launchText);
  pageContext.setAttribute("iconUrl", feed.getIconUrl());
  pageContext.setAttribute("iconTitle", feed.getIconTitle());
  pageContext.setAttribute("content", feed.getContent());
  pageContext.setAttribute("target", target);
%>
<bbNG:includedPage>
  <bbNG:cssBlock>
    <style type="text/css">
      span.itemTitle a {
        display: inline;
      }
    </style>
  </bbNG:cssBlock>
<div class="eudModule">
  <div class="eudModule-inner">
    <div class="portletBlock" style="border-top-width: 0">
<%
  if (feed.getIconUrl() != null) {
%>
      <div style="text-align: center;">
<%
    if (allowLaunch) {
%>
        <a href="${launchUrl}" title="${launchText}"><img src="${iconUrl}" alt="${iconTitle}" /></a>
<%
    } else {
%>
        <img src="${iconUrl}" alt="${iconTitle}" />
<%
    }
%>
      </div>
<%
  }
  if (listItems.size() > 0) {
%>
    <bbNG:collapsibleList id="id_items" isDynamic="false" listItems="<%=listItems%>">
    </bbNG:collapsibleList>
<%
  } else {
%>
${content}
<%
  }
  if (showLaunch) {
%>
    <div class="blockGroups" style="text-align: center;">
      <bbNG:button url="${launchUrl}" label="${launchText}" target="${target}" />
    </div>
<%
  }
%>
    </div>
  </div>
</div>
<div class="portletInfoFooter">${bundle['page.module.view.date']}: ${dateString}</div>
</bbNG:includedPage>
