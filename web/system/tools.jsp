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
      2.0.1 20-May-12  Fixed page doctype
                       Added return to control panel tools page (including paging option)
      2.1.0 18-Jun-12  Added "Open in" to tool summary table
                       Updated mouseover titles to table entries
      2.2.0  2-Sep-12  Added option to add tools as named items on a content page menu
                       Added clear cache option
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
      3.0.0 30-Oct-13
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.List,
                java.util.ArrayList,
                blackboard.servlet.tags.ngui.ContextMenuTag,
                blackboard.persist.Id,
                blackboard.persist.KeyNotFoundException,
                blackboard.persist.PersistenceException,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.lti.Constants,
                org.oscelot.blackboard.lti.Utils,
                org.oscelot.blackboard.lti.Tool,
                org.oscelot.blackboard.lti.ToolList,
                org.oscelot.blackboard.utils.StringCache,
                org.oscelot.blackboard.utils.StringCacheFile"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:genericPage title="${bundle['page.system.tools.title']}" onLoad="doOnLoad()">
<%
  B2Context b2Context = new B2Context(request);
  String subTitle = "";
  ToolList toolList = new ToolList(b2Context);
  List<Tool> tools = toolList.getList();
  String handle = "admin_main";
  if (request.getParameter(Constants.PAGE_PARAMETER_NAME) != null) {
    handle = "admin_plugin_manage";
  }
  String cancelUrl = b2Context.getNavigationItem(handle).getHref();
  boolean isv90 = !B2Context.getIsVersion(9, 1, 0);
  boolean enabledMashup = b2Context.getSetting(Constants.MASHUP_PARAMETER, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  if (!isv90) {
    Utils.checkVTBEMashup(b2Context, enabledMashup);  // Check mashup option is in place
  }

  StringCache stringCache = StringCacheFile.getInstance(
     b2Context.getSetting(Constants.CACHE_AGE_PARAMETER),
     b2Context.getSetting(Constants.CACHE_CAPACITY_PARAMETER));
  boolean hasCache = (stringCache.getCapacity() > 0);
  pageContext.setAttribute("size", stringCache.getSize());

  pageContext.setAttribute("itemSeparator", ContextMenuTag.SEPARATOR);
  pageContext.setAttribute("path", b2Context.getPath());
  pageContext.setAttribute("query", Utils.getQuery(request));
  pageContext.setAttribute("list", "&" + Constants.PAGE_PARAMETER_NAME + "=" + Constants.ADMIN_PAGE);
  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("imageFiles", Constants.IMAGE_FILE);
  pageContext.setAttribute("imageAlt", Constants.IMAGE_ALT_RESOURCE);
  pageContext.setAttribute("cancelUrl", cancelUrl);
  pageContext.setAttribute("DoEnable", Constants.ACTION_ENABLE);
  pageContext.setAttribute("DoDisable", Constants.ACTION_DISABLE);
  pageContext.setAttribute("DoDelete", Constants.ACTION_DELETE);
  pageContext.setAttribute("NoMenu", Constants.ACTION_NOMENU);
  pageContext.setAttribute("DoTool", Constants.ACTION_TOOL);
  pageContext.setAttribute("DoNotool", Constants.ACTION_NOTOOL);
  if (!isv90) {
    pageContext.setAttribute("CreateItemMenu", Constants.MENU_CREATE_ITEM);
    pageContext.setAttribute("CreateMediaMenu", Constants.MENU_CREATE_MEDIA);
    pageContext.setAttribute("CreateOtherMenu", Constants.MENU_CREATE_OTHER);
    pageContext.setAttribute("NewPageMenu", Constants.MENU_NEW_PAGE);
    pageContext.setAttribute("MashupMenu", Constants.MENU_MASHUP);
    pageContext.setAttribute("AssessmentsMenu", Constants.MENU_EVALUATE);
    pageContext.setAttribute("ToolsMenu", Constants.MENU_COLLABORATE);
    pageContext.setAttribute("PublisherMenu", Constants.MENU_TEXTBOOK);
  }
  pageContext.setAttribute("xmlTitle", b2Context.getResourceString("page.system.tools.action.xml"));
  pageContext.setAttribute("subTitle", subTitle);
  String reorderingUrl = "reordertools";
%>
  <bbNG:jsBlock>
<script type="text/javascript">
function doOnLoad() {
}
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
  <bbNG:pageHeader instructions="${bundle['page.system.tools.instructions']}">
    <bbNG:breadcrumbBar environment="SYS_ADMIN" navItem="admin_plugin_manage">
      <bbNG:breadcrumb href="tools.jsp" title="${bundle['plugin.name']}" />
    </bbNG:breadcrumbBar>
    <bbNG:pageTitleBar iconUrl="../images/lti.gif" showTitleBar="true" title="${bundle['plugin.name']}${subTitle}" />
    <bbNG:actionControlBar>
      <bbNG:actionButton title="${bundle['page.system.tools.button.settings']}" url="settings.jsp?${query}" primary="true" />
      <bbNG:actionMenu title="${bundle['page.system.tools.button.default']}">
        <bbNG:actionMenuItem title="${bundle['page.system.tools.button.data']}" href="data.jsp?${query}" />
        <bbNG:actionMenuItem title="${bundle['page.system.tools.button.launch']}" href="launch.jsp?${query}" />
      </bbNG:actionMenu>
      <bbNG:actionButton title="${bundle['page.system.tools.button.add']}" url="tool.jsp?${query}" primary="true" />
      <bbNG:actionButton title="${bundle['page.system.tools.button.domains']}" url="domains.jsp?${query}" primary="false" />
      <bbNG:actionButton title="${bundle['page.system.tools.button.services']}" url="services.jsp?${query}" primary="false" />
<%
  if (hasCache) {
%>
      <bbNG:actionButton title="${bundle['page.system.tools.button.cache']} (${size})" url="cache.jsp" primary="false" />
<%
  }
%>
    </bbNG:actionControlBar>
  </bbNG:pageHeader>
  <bbNG:form name="frmTools" method="post" action="toolsaction?${query}">
    <input type="hidden" name="<%=Constants.ACTION%>" value="" />
    <bbNG:inventoryList collection="<%=tools%>" objectVar="tool" className="org.oscelot.blackboard.lti.Tool"
       description="${bundle['page.system.tools.description']}" reorderable="true" reorderType="${bundle['page.system.tools.reordertype']}"
       reorderingUrl="<%=reorderingUrl%>"
       itemIdAccessor="getId" itemNameAccessor="getName" showAll="false" emptyMsg="${bundle['page.system.tools.empty']}">
<%
  if (isv90) {
%>
      <bbNG:listActionBar>
        <bbNG:listActionMenu title="${bundle['page.system.tools.action.status']}">
          <bbNG:listActionItem title="${bundle['page.system.tools.action.enable']}" url="JavaScript: doAction('${DoEnable}');" />
          <bbNG:listActionItem title="${bundle['page.system.tools.action.disable']}" url="JavaScript: doAction('${DoDisable}');" />
        </bbNG:listActionMenu>
        <bbNG:listActionMenu title="${bundle['page.system.tools.action.coursetool']}">
          <bbNG:listActionItem title="${bundle['page.system.tools.action.tool']}" url="JavaScript: doAction('${DoTool}');" />
          <bbNG:listActionItem title="${bundle['page.system.tools.action.notool']}" url="JavaScript: doAction('${DoNotool}');" />
        </bbNG:listActionMenu>
        <bbNG:listActionItem title="${bundle['page.system.tools.action.delete']}" url="JavaScript: doDelete('${DoDelete}');" />
      </bbNG:listActionBar>
<%
  }
%>
      <bbNG:listActionBar>
        <bbNG:listActionMenu title="${bundle['page.system.tools.action.status']}">
          <bbNG:listActionItem title="${bundle['page.system.tools.action.enable']}" url="JavaScript: doAction('${DoEnable}');" />
          <bbNG:listActionItem title="${bundle['page.system.tools.action.disable']}" url="JavaScript: doAction('${DoDisable}');" />
        </bbNG:listActionMenu>
        <bbNG:listActionMenu title="${bundle['page.system.tools.action.menuitem']}">
          <bbNG:listActionItem title="${bundle['page.system.tools.action.nomenu']}" url="JavaScript: doAction('${NoMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.createItem.label']}" url="JavaScript: doAction('${CreateItemMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.createMedia.label']}" url="JavaScript: doAction('${CreateMediaMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.createOther.label']}" url="JavaScript: doAction('${CreateOtherMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.newPage.label']}" url="JavaScript: doAction('${NewPageMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.mashup.label']}" url="JavaScript: doAction('${MashupMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.evaluate.label']}" url="JavaScript: doAction('${AssessmentsMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.collaborate.label']}" url="JavaScript: doAction('${ToolsMenu}');" />
          <bbNG:listActionItem title="${bundle['menu.textbook.label']}" url="JavaScript: doAction('${PublisherMenu}');" />
        </bbNG:listActionMenu>
        <bbNG:listActionMenu title="${bundle['page.system.tools.action.coursetool']}">
          <bbNG:listActionItem title="${bundle['page.system.tools.action.tool']}" url="JavaScript: doAction('${DoTool}');" />
          <bbNG:listActionItem title="${bundle['page.system.tools.action.notool']}" url="JavaScript: doAction('${DoNotool}');" />
        </bbNG:listActionMenu>
        <bbNG:listActionItem title="${bundle['page.system.tools.action.delete']}" url="JavaScript: doDelete('${DoDelete}');" />
      </bbNG:listActionBar>
<bbNG:jspBlock>
<%
  pageContext.setAttribute("id", Constants.TOOL_ID + "=" + tool.getId());
  pageContext.setAttribute("alt", Constants.TOOL_PARAMETER_PREFIX + "." + tool.getIsEnabled());
  String deleteOption = b2Context.getResourceString("page.system.tools.action.delete");
  pageContext.setAttribute("deleteOption", deleteOption);
%>
</bbNG:jspBlock>
      <bbNG:listCheckboxElement name="<%=Constants.TOOL_ID%>" value="${tool.id}" />
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.enabled.label']}" name="isenabled">
        <img src="${imageFiles[tool.isEnabled]}" alt="${bundle[imageAlt[alt]]}" title="${bundle[imageAlt[alt]]}" />
      </bbNG:listElement>
<%
  boolean enabled = tool.getIsEnabled().equals(Constants.DATA_TRUE);
  if (enabled) {
    pageContext.setAttribute("actionTitle", b2Context.getResourceString("page.system.tools.action.disable"));
    pageContext.setAttribute("statusAction", Constants.ACTION_DISABLE);
    if (tool.getHasCourseTool().equals(Constants.DATA_TRUE)) {
      pageContext.setAttribute("toolTitle", b2Context.getResourceString("page.system.tools.action.notool"));
      pageContext.setAttribute("toolAction", Constants.ACTION_NOTOOL);
    } else {
      pageContext.setAttribute("toolTitle", b2Context.getResourceString("page.system.tools.action.tool"));
      pageContext.setAttribute("toolAction", Constants.ACTION_TOOL);
    }
  } else {
    pageContext.setAttribute("actionTitle", b2Context.getResourceString("page.system.tools.action.enable"));
    pageContext.setAttribute("statusAction", Constants.ACTION_ENABLE);
  }
  pageContext.setAttribute("openinLabel", b2Context.getResourceString("page.system.launch.openin." + tool.getOpenIn()));

  if (!isv90) {
%>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.menuitem.label']}" name="menuitem">
<%
    if (tool.getHasMenuItem()) {
%>
        ${tool.menuItem.menuLabel}
<%
    } else {
%>
        <img src="${imageFiles["false"]}" alt="${bundle[imageAlt["false"]]}" title="${bundle[imageAlt["false"]]}" />
<%
    }
%>
      </bbNG:listElement>
<%
  }
%>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.coursetool.label']}" name="coursetool">
        <img src="${imageFiles[tool.hasCourseTool]}" alt="${bundle[imageAlt[tool.hasCourseTool]]}" title="${bundle[imageAlt[tool.hasCourseTool]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="true" label="${bundle['page.system.tools.name.label']}" name="name">
        <span title="${tool.url}">${tool.name}</span>
<%
  if (enabled) {
    if (tool.getConfig().equals(Constants.DATA_TRUE)) {
%>
        <bbNG:listContextMenu order="edit,data,launch,${itemSeparator},status,tool,xml,${itemSeparator},delete,${itemSeparator},configure">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.register']}" url="tool.jsp?${id}&${query}" id="edit" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.data']}" url="data.jsp?${id}&${query}" id="data" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.launch']}" url="launch.jsp?${id}&${query}" id="launch" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${statusAction}');" id="status" />
          <bbNG:contextMenuItem title="${toolTitle}" url="JavaScript: doAction('${toolAction}');" id="tool" />
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${deleteOption}" url="JavaScript: doDelete();" id="delete" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.config']}" url="../config.jsp?${id}${list}&${query}" target="${target}" id="config" />
        </bbNG:listContextMenu>
<%
    } else {
%>
        <bbNG:listContextMenu order="edit,data,launch,${itemSeparator},status,tool,xml,${itemSeparator},delete">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.register']}" url="tool.jsp?${id}&${query}" id="edit" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.data']}" url="data.jsp?${id}&${query}" id="data" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.launch']}" url="launch.jsp?${id}&${query}" id="launch" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${statusAction}');" id="status" />
          <bbNG:contextMenuItem title="${toolTitle}" url="JavaScript: doAction('${toolAction}');" id="tool" />
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${deleteOption}" url="JavaScript: doDelete();" id="delete" />
        </bbNG:listContextMenu>
<%
    }
  } else if (tool.getConfig().equals(Constants.DATA_TRUE)) {
%>
        <bbNG:listContextMenu order="edit,data,launch,${itemSeparator},status,xml,${itemSeparator},delete,${itemSeparator},configure">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.register']}" url="tool.jsp?${id}&${query}" id="edit" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.data']}" url="data.jsp?${id}&${query}" id="data" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.launch']}" url="launch.jsp?${id}&${query}" id="launch" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${statusAction}');" id="status" />
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${deleteOption}" url="JavaScript: doDelete();" id="delete" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.config']}" url="../config.jsp?${id}${list}&${query}" target="${target}" id="config" />
        </bbNG:listContextMenu>
<%
  } else {
%>
        <bbNG:listContextMenu order="edit,data,launch,${itemSeparator},status,xml,${itemSeparator},delete">
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.register']}" url="tool.jsp?${id}&${query}" id="edit" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.data']}" url="data.jsp?${id}&${query}" id="data" />
          <bbNG:contextMenuItem title="${bundle['page.system.tools.action.launch']}" url="launch.jsp?${id}&${query}" id="launch" />
          <bbNG:contextMenuItem title="${actionTitle}" url="JavaScript: doAction('${statusAction}');" id="status" />
          <bbNG:contextMenuItem title="${xmlTitle}" url="../toolxml?${id}" target="_blank" id="xml" />
          <bbNG:contextMenuItem title="${deleteOption}" url="JavaScript: doDelete();" id="delete" />
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
        <img alt="" src="${imageFiles[tool.userId]}" alt="${bundle[imageAlt[tool.userId]]}" title="${bundle[imageAlt[tool.userId]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.username.label']}" name="username">
        <img src="${imageFiles[tool.username]}" alt="${bundle[imageAlt[tool.username]]}" title="${bundle[imageAlt[tool.username]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.email.label']}" name="email">
        <img src="${imageFiles[tool.email]}" alt="${bundle[imageAlt[tool.email]]}" title="${bundle[imageAlt[tool.email]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.roles.label']}" name="roles">
        <img src="${imageFiles[tool.roles]}" alt="${bundle[imageAlt[tool.roles]]}" title="${bundle[imageAlt[tool.roles]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.splash.label']}" name="splash">
        <img src="${imageFiles[tool.splash]}" alt="${bundle[imageAlt[tool.splash]]}" title="${bundle[imageAlt[tool.splash]]}" />
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.openin.label']}" name="openin">
        <span title="${openinLabel}">${tool.openIn}</span>
      </bbNG:listElement>
      <bbNG:listElement isRowHeader="false" label="${bundle['page.system.tools.ext.label']}" name="ext">
        ${tool.sendExtensions}
      </bbNG:listElement>
    </bbNG:inventoryList>
  </bbNG:form>
  <br />
  <bbNG:okButton url="${cancelUrl}" />
</bbNG:genericPage>
