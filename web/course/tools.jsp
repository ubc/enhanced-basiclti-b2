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
      1.1.3  1-Jan-11  Changed to use standard image files
      1.2.0 17-Sep-11  Added support for outcomes, memberships and setting extension services
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
                       Added support for paging of tool list
                       Added option to allow instructors to create their own links
      2.0.1 20-May-12  Fixed page doctype
                       Added return to control panel tools page (including paging option)
      2.1.0 18-Jun-12  Added "Open in" to tool summary table
                       Updated mouseover titles to table entries
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
      3.0.0 30-Oct-13  Added remote configure option
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="blackboard.servlet.tags.ngui.ContextMenuTag,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.lti.Constants,
                org.oscelot.blackboard.lti.Utils,
                org.oscelot.blackboard.lti.ToolList"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:learningSystemPage title="${bundle['plugin.name']}">
<%
  B2Context b2Context = new B2Context(request);
  Utils.checkCourse(b2Context);
  ToolList toolList = new ToolList(b2Context);
  boolean allowLocal = b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_DELEGATE, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);

  String instructions = "page.course.tools.instructions";
  if (allowLocal) {
    instructions = "page.course.tools.local.instructions";
  }
  pageContext.setAttribute("itemSeparator", ContextMenuTag.SEPARATOR);
  pageContext.setAttribute("instructions", b2Context.getResourceString(instructions));
  pageContext.setAttribute("query", Utils.getQuery(request));
  pageContext.setAttribute("list", "&" + Constants.PAGE_PARAMETER_NAME + "=" + Constants.COURSE_TOOLS_PAGE);
  pageContext.setAttribute("courseId", b2Context.getRequestParameter("course_id", ""));
  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("imageFiles", Constants.IMAGE_FILE);
  pageContext.setAttribute("imageAlt", Constants.IMAGE_ALT_RESOURCE);
  pageContext.setAttribute("DoAvailable", Constants.ACTION_AVAILABLE);
  pageContext.setAttribute("DoUnavailable", Constants.ACTION_UNAVAILABLE);
  pageContext.setAttribute("DoDelete", Constants.ACTION_DELETE);
  pageContext.setAttribute("xmlTitle", b2Context.getResourceString("page.system.tools.action.xml"));
  String reorderingUrl = "reordertools?course_id=" + b2Context.getRequestParameter("course_id", "");
%>
  <bbNG:jsBlock>
  <script type="text/javascript">
  function doAction(value) {
    document.frmTools.action.value = value;
    document.frmTools.submit();
  }
  function doDelete() {
    if (confirm('${bundle['page.system.tools.action.confirm']}')) {
      doAction('delete');
    }
  }
  </script>
  </bbNG:jsBlock>
  <bbNG:pageHeader instructions="${instructions}">
    <bbNG:breadcrumbBar environment="CTRL_PANEL">
      <bbNG:breadcrumb title="${bundle['plugin.name']}" />
    </bbNG:breadcrumbBar>
    <bbNG:pageTitleBar iconUrl="../images/lti.gif" showTitleBar="true" title="${bundle['plugin.name']}" />
<%
  if (allowLocal) {
%>
    <bbNG:actionControlBar>
      <bbNG:actionButton title="${bundle['page.system.tools.button.add']}" url="tool.jsp?${query}" primary="true" />
    </bbNG:actionControlBar>
<%
  }
%>
  </bbNG:pageHeader>
  <bbNG:form name="frmTools" method="post" action="toolsaction?${query}">
    <input type="hidden" name="<%=Constants.ACTION%>" value="" />
    <bbNG:inventoryList collection="<%=toolList.getList()%>" objectVar="tool" className="org.oscelot.blackboard.lti.Tool"
       description="${bundle['page.system.tools.description']}" reorderable="true" reorderType="${bundle['page.system.tools.reordertype']}"
       reorderingUrl="<%=reorderingUrl%>"
       itemIdAccessor="getId" itemNameAccessor="getName" showAll="false" emptyMsg="${bundle['page.course.tools.empty']}">
      <bbNG:listActionBar>
        <bbNG:listActionMenu title="${bundle['page.course.tools.action.availability']}">
          <bbNG:listActionItem title="${bundle['page.course.tools.action.available']}" url="JavaScript: doAction('${DoAvailable}');" />
          <bbNG:listActionItem title="${bundle['page.course.tools.action.unavailable']}" url="JavaScript: doAction('${DoUnavailable}');" />
        </bbNG:listActionMenu>
      </bbNG:listActionBar>
<bbNG:jspBlock>
<%
    pageContext.setAttribute("id", Constants.TOOL_ID + "=" + tool.getId());
    boolean available = tool.getIsAvailable().equals(Constants.DATA_TRUE);
    if (available) {
      pageContext.setAttribute("actionTitle", b2Context.getResourceString("page.course.tools.action.unavailable"));
      pageContext.setAttribute("availableAction", Constants.ACTION_UNAVAILABLE);
    } else {
      pageContext.setAttribute("actionTitle", b2Context.getResourceString("page.course.tools.action.available"));
      pageContext.setAttribute("availableAction", Constants.ACTION_AVAILABLE);
    }
    pageContext.setAttribute("openinLabel", b2Context.getResourceString("page.system.launch.openin." + tool.getOpenIn()));
    pageContext.setAttribute("alt", Constants.COURSE_TOOL_PREFIX + Constants.TOOL_PARAMETER_PREFIX + "." + tool.getIsAvailable());
    pageContext.setAttribute("splashAlt", "enable." + tool.getSplash());
%>
</bbNG:jspBlock>
      <bbNG:listCheckboxElement name="<%=Constants.TOOL_ID%>" value="${tool.id}" />
      <bbNG:listElement isRowHeader="false" label="${bundle['page.course.tools.availability.label']}" name="isavailable">
        <img src="${imageFiles[tool.isAvailable]}" alt="${bundle[imageAlt[alt]]}" title="${bundle[imageAlt[alt]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="true" label="${bundle['page.system.tools.name.label']}" name="name">
        <span title="${tool.url}">${tool.name}</span>
<%
    String toolSettingPrefix = Constants.TOOL_PARAMETER_PREFIX + "." + tool.getId() + ".";
    boolean isLocal = b2Context.getSetting(toolSettingPrefix + Constants.TOOL_NAME).length() <= 0;
    String target = "_self";
    if (!tool.getSplash().equals(Constants.DATA_TRUE) && !tool.getUserHasChoice()) {
      target = tool.getWindowName();
    }
    pageContext.setAttribute("target", target);
    if (!isLocal) {
%>
        <bbNG:listContextMenu order="edit,${itemSeparator},availability,${itemSeparator},open">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.edit']}" url="edit.jsp?${id}&${query}" id="edit" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${availableAction}');" id="availability" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.open']}" url="../tool.jsp?${id}${list}&${query}" target="${target}" id="open" />
        </bbNG:listContextMenu>
<%
    } else if (allowLocal) {
      if (tool.getConfig().equals(Constants.DATA_TRUE)) {
%>
        <bbNG:listContextMenu order="register,data,launch,${itemSeparator},availability,${itemSeparator},xml,delete,${itemSeparator},open,${itemSeparator},config">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.register']}" url="tool.jsp?${id}&${query}" id="register" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.data']}" url="data.jsp?${id}&${query}" id="data" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.launch']}" url="launch.jsp?${id}&${query}" id="launch" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${availableAction}');" id="availability" />
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}&${query}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.delete']}" url="JavaScript: doDelete();" id="delete" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.open']}" url="../tool.jsp?${id}${list}&${query}" target="${target}" id="open" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.config']}" url="../config.jsp?${id}${list}&${query}" target="${target}" id="config" />
        </bbNG:listContextMenu>
<%
      } else {
%>
        <bbNG:listContextMenu order="register,data,launch,${itemSeparator},availability,${itemSeparator},xml,delete,${itemSeparator},open">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.register']}" url="tool.jsp?${id}&${query}" id="register" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.data']}" url="data.jsp?${id}&${query}" id="data" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.launch']}" url="launch.jsp?${id}&${query}" id="launch" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${availableAction}');" id="availability" />
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}&${query}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.delete']}" url="JavaScript: doDelete();" id="delete" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.open']}" url="../tool.jsp?${id}${list}&${query}" target="${target}" id="open" />
        </bbNG:listContextMenu>
<%
      }
    } else {
%>
        &nbsp;<img src="/images/ci/icons/disabled_li.gif" alt="${bundle['page.course.tools.availability.disabled']}" title="${bundle['page.course.tools.availability.disabled']}" />
        <bbNG:listContextMenu order="xml,delete">
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}&${query}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.delete']}" url="JavaScript: doDelete();" id="delete" />
        </bbNG:listContextMenu>
<%
    }
%>
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.contextid.label']}" name="contextid">
        <img src="${imageFiles[tool.contextId]}" alt="${bundle[imageAlt[tool.contextId]]}" title="${bundle[imageAlt[tool.contextId]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.contexttitle.label']}" name="contexttitle">
        <img src="${imageFiles[tool.contextTitle]}" alt="${bundle[imageAlt[tool.contextTitle]]}" title="${bundle[imageAlt[tool.contextTitle]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.userid.label']}" name="userid">
        <img alt="" src="${imageFiles[tool.sendUserId]}" alt="${bundle[imageAlt[tool.sendUserId]]}" title="${bundle[imageAlt[tool.sendUserId]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.username.label']}" name="username">
        <img src="${imageFiles[tool.sendUsername]}" alt="${bundle[imageAlt[tool.sendUsername]]}" title="${bundle[imageAlt[tool.sendUsername]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.email.label']}" name="email">
        <img src="${imageFiles[tool.sendEmail]}" alt="${bundle[imageAlt[tool.sendEmail]]}" title="${bundle[imageAlt[tool.sendEmail]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.roles.label']}" name="roles">
        <img src="${imageFiles[tool.roles]}" alt="${bundle[imageAlt[tool.roles]]}" title="${bundle[imageAlt[tool.roles]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.splash.label']}" name="splash">
        <img src="${imageFiles[tool.splash]}" alt="${bundle[imageAlt[splashAlt]]}" title="${bundle[imageAlt[splashAlt]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.openin.label']}" name="openin">
        <span title="${openinLabel}">${tool.openIn}</span>
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.ext.label']}" name="ext">
        ${tool.doSendExtensions}
      </bbNG:listElement>
    </bbNG:inventoryList>
  </bbNG:form>
</bbNG:learningSystemPage>
