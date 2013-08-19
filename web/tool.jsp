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
      1.1.2  9-Oct-10  Added options for return messages and log requests
      1.1.3  1-Jan-11  Changed to use standard image files
      1.2.0 17-Sep-11
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
      2.0.1 20-May-12  Fixed page doctype
                       Added return to control panel tools page (including paging option)
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12  Changed disabled and noaccess messages to display as receipts rather than separate pages
      2.3.0  5-Nov-12  Added fix for changes to how frames are opened in SP10
                       Added "no breadcrumb" option
                       Added support for launching from a module outside a course
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13  Allow access to tools defined by URL even when define tools by instructor option is not enabled
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.net.URLEncoder,
                blackboard.portal.data.Module,
                blackboard.portal.persist.ModuleDbLoader,
                blackboard.persist.Id,
                blackboard.platform.persistence.PersistenceServiceFactory,
                blackboard.persist.BbPersistenceManager,
                blackboard.persist.content.ContentDbLoader,
                blackboard.data.content.Content,
                blackboard.persist.KeyNotFoundException,
                blackboard.persist.PersistenceException,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.basiclti.Constants,
                org.oscelot.blackboard.basiclti.Utils,
                org.oscelot.blackboard.basiclti.Tool"
        errorPage="error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:learningSystemPage title="${bundle['page.course_tool.splash.pagetitle']}">
<%
  String moduleId = Utils.checkForModule(request);
  B2Context b2Context = new B2Context(request);
  Utils.checkCourse(b2Context);
  String courseId = b2Context.getRequestParameter("course_id", "");
  String contentId = b2Context.getRequestParameter("content_id", "");
  String toolId = b2Context.getRequestParameter(Constants.TOOL_ID,
     b2Context.getSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID, ""));
  String sourcePage = b2Context.getRequestParameter(Constants.PAGE_PARAMETER_NAME, "");
  Tool tool = new Tool(b2Context, toolId);
  boolean allowLocal = b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_DELEGATE, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  String actionUrl = "";
  if (courseId.length() > 0) {
    actionUrl = "course_id=" + courseId + "&";
  }
  if (contentId.length() > 0) {
    actionUrl += "content_id=" + contentId + "&";
  }
  String idParam = Constants.TOOL_ID + "=" + toolId + "&";
  if (moduleId != null) {
    idParam = Constants.TOOL_MODULE + "=" + moduleId + "&" +
       Constants.TAB_PARAMETER_NAME + "=" + b2Context.getRequestParameter(Constants.TAB_PARAMETER_NAME, "") + "&n=" +
       b2Context.getRequestParameter("n", "");
  }
  if (!tool.getIsEnabled().equals(Constants.DATA_TRUE) || (!tool.getIsSystemTool() && !tool.getByUrl() && !allowLocal)) {
    response.sendRedirect("return.jsp?" + actionUrl + idParam + "&error=true&" +
       Constants.LTI_ERROR_MESSAGE + "=" + b2Context.getResourceString("page.course_tool.disabled.error"));
  } else if (tool.getDoSendRoles() && (courseId.length() > 0) && (b2Context.getContext().getCourseMembership() == null)) {
    response.sendRedirect("return.jsp?" + actionUrl + idParam + "&error=true&" +
       Constants.LTI_ERROR_MESSAGE + "=" + b2Context.getResourceString("page.course_tool.noaccess.error"));
  } else {
    if (sourcePage.equals(Constants.COURSE_TOOLS_PAGE) || sourcePage.equals(Constants.TOOLS_PAGE)) {
      actionUrl = Utils.getQuery(request) + "&";
    }
    boolean redirect = (b2Context.getRequestParameter(Constants.ACTION, "").length() > 0);
    if (!redirect && (tool.getSplash().equals(Constants.DATA_TRUE) || tool.getUserHasChoice()) && (moduleId != null)) {
      response.sendRedirect("return.jsp?" + actionUrl + idParam + "&error=true&" +
         Constants.LTI_ERROR_MESSAGE + "=" + b2Context.getResourceString("page.course_tool.splash.error"));
    } else if (!redirect && (tool.getSplash().equals(Constants.DATA_TRUE) || tool.getUserHasChoice())) {
      actionUrl = "tool.jsp?" + actionUrl + idParam + Constants.ACTION + "=redirect";
      pageContext.setAttribute("bundle", b2Context.getResourceStrings());
      pageContext.setAttribute("imageFiles", Constants.IMAGE_FILE);
      pageContext.setAttribute("imageAlt", Constants.IMAGE_ALT_RESOURCE);
      pageContext.setAttribute("actionUrl", actionUrl);
      pageContext.setAttribute("tool", tool);
      pageContext.setAttribute("courseId", courseId);
      String target = tool.getWindowName();
      pageContext.setAttribute("target", target);
      String ltiError = b2Context.getRequestParameter(Constants.LTI_ERROR_MESSAGE, "");
      if (ltiError.length() > 0) {
        b2Context.setReceipt(ltiError, false);
      }
%>
  <bbNG:pageHeader instructions="${bundle['page.settings.instructions']}">
<%
      if (!b2Context.getContext().hasContentContext()) {
%>
    <bbNG:breadcrumbBar environment="COURSE">
      <bbNG:breadcrumb href="tools.jsp?course_id=${courseId}" title="${bundle['plugin.name']}" />
      <bbNG:breadcrumb title="${bundle['page.course_tool.splash.title']} ${tool.name}" />
    </bbNG:breadcrumbBar>
<%
        pageContext.setAttribute("iconUrl", "icon.jsp?course_id=" + courseId + "&amp;" + Constants.TOOL_ID + "=" + tool.getId());
      } else {
%>
    <bbNG:breadcrumbBar />
<%
        pageContext.setAttribute("iconUrl", "icon.jsp?course_id=" + courseId + "&amp;content_id=" + contentId);
      }
%>
    <bbNG:pageTitleBar iconUrl="${iconUrl}" showTitleBar="true" title="${bundle['page.course_tool.splash.title']} ${tool.name}"/>
  </bbNG:pageHeader>
  <bbNG:form action="${actionUrl}" method="post" onsubmit="return validateForm();">
  <bbNG:dataCollection markUnsavedChanges="true" showSubmitButtons="true">
<%
      if (tool.getSplash().equals("true") && (tool.getSplashText().length() > 0)) {
%>
    <bbNG:step hideNumber="false" title="${bundle['page.course_tool.splash.step1.title']}">
      ${tool.splashText}
    </bbNG:step>
<%
      }
%>
    <bbNG:step hideNumber="false" title="${bundle['page.course_tool.splash.step2.title']}">
      <bbNG:dataElement isRequired="false" label="${bundle['page.course_tool.splash.step2.userid.label']}">
<%
      String userIdSetting = tool.getSendUserId();
      if (userIdSetting.equals(Constants.DATA_MANDATORY)) {
%>
        <img src="${imageFiles['true']}" alt="${bundle[imageAlt['true']]}" title="${bundle[imageAlt['true']]}" />
<%
      } else if (userIdSetting.equals(Constants.DATA_NOTUSED)) {
%>
        <img src="${imageFiles['false']}" alt="${bundle[imageAlt['false']]}" title="${bundle[imageAlt['false']]}" />
<%
      } else {
%>
        <bbNG:checkboxElement isSelected="${tool.userUserId}" name="<%=Constants.TOOL_USERID%>" value="true" helpText="${bundle['page.course_tool.splash.step2.userid.instructions']}" />
<%
      }
%>
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="false" label="${bundle['page.course_tool.splash.step2.username.label']}">
<%
      String usernameSetting = tool.getSendUsername();
      if (usernameSetting.equals(Constants.DATA_MANDATORY)) {
%>
        <img src="${imageFiles['true']}" alt="${bundle[imageAlt['true']]}" title="${bundle[imageAlt['true']]}" />
<%
      } else if (usernameSetting.equals(Constants.DATA_NOTUSED)) {
%>
        <img src="${imageFiles['false']}" alt="${bundle[imageAlt['false']]}" title="${bundle[imageAlt['false']]}" />
<%
      } else {
%>
        <bbNG:checkboxElement isSelected="${tool.userUsername}" name="<%=Constants.TOOL_USERNAME%>" value="true" helpText="${bundle['page.course_tool.splash.step2.username.instructions']}" />
<%
      }
%>
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="false" label="${bundle['page.course_tool.splash.step2.email.label']}">
<%
      String emailSetting = tool.getSendEmail();
      if (emailSetting.equals(Constants.DATA_MANDATORY)) {
%>
        <img src="${imageFiles['true']}" alt="${bundle[imageAlt['true']]}" title="${bundle[imageAlt['true']]}" />
<%
      } else if (emailSetting.equals(Constants.DATA_NOTUSED)) {
%>
        <img src="${imageFiles['false']}" alt="${bundle[imageAlt['false']]}" title="${bundle[imageAlt['false']]}" />
<%
      } else {
%>
        <bbNG:checkboxElement isSelected="${tool.userEmail}" name="<%=Constants.TOOL_EMAIL%>" value="true" helpText="${bundle['page.course_tool.splash.step2.email.instructions']}" />
<%
      }
%>
      </bbNG:dataElement>
    </bbNG:step>
    <bbNG:stepSubmit hideNumber="false" showCancelButton="true" />
  </bbNG:dataCollection>
  </bbNG:form>
<%
    } else {
      String settingPrefix = Constants.TOOL_PARAMETER_PREFIX + ".";
      if (toolId.length() > 0) {
        settingPrefix += toolId + ".";
      }
      boolean persist = false;
      if (tool.getUserId().equals(Constants.DATA_OPTIONAL) && (bbContext.hasContentContext() || tool.getSendUserId().equals(Constants.DATA_OPTIONAL))) {
        b2Context.setSetting(false, false, settingPrefix + Constants.TOOL_USERID, b2Context.getRequestParameter(Constants.TOOL_USERID, "false"));
        persist = true;
      }
      if (tool.getUsername().equals(Constants.DATA_OPTIONAL) && (bbContext.hasContentContext() || tool.getSendUsername().equals(Constants.DATA_OPTIONAL))) {
        b2Context.setSetting(false, false, settingPrefix + Constants.TOOL_USERNAME, b2Context.getRequestParameter(Constants.TOOL_USERNAME, "false"));
        persist = true;
      }
      if (tool.getEmail().equals(Constants.DATA_OPTIONAL) && (bbContext.hasContentContext() || tool.getSendEmail().equals(Constants.DATA_OPTIONAL))) {
        b2Context.setSetting(false, false, settingPrefix + Constants.TOOL_EMAIL, b2Context.getRequestParameter(Constants.TOOL_EMAIL, "false"));
        persist = true;
      }
      if (persist) {
        b2Context.persistSettings(false, false);
      }
      String url = null;
      boolean useWrapper = false;
      if (tool.getOpenIn().equals(Constants.DATA_IFRAME) && (moduleId == null)) {
        url = "iframe";
      } else if (tool.getOpenIn().equals(Constants.DATA_WINDOW)) {
        url = "new";
        if (!B2Context.getIsVersion(9, 1, 10)) {
          useWrapper = true;
        }
      } else {
        url = "frame";
        useWrapper = tool.getOpenIn().equals(Constants.DATA_FRAME);
        if (B2Context.getIsVersion(9, 1, 10) && !redirect) {
          if (!useWrapper && (moduleId == null)) {
            idParam += "&full=" + Constants.DATA_TRUE;
          }
          useWrapper = false;
        }
      }
      url += ".jsp?" + actionUrl + idParam;
      String title = tool.getName();
      if (useWrapper) {
        if (contentId.length() > 0) {
          BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
          ContentDbLoader contentLoader = (ContentDbLoader)bbPm.getLoader(ContentDbLoader.TYPE);
          Id id = bbPm.generateId(Content.DATA_TYPE, contentId);
          Content content = contentLoader.loadById(id);
          useWrapper = content.getRenderType().equals(Content.RenderType.DEFAULT);
          if (!useWrapper) {
            content.setRenderType(Content.RenderType.DEFAULT);
            content.persist();
          }
          title = content.getTitle();
        } else if (courseId.length() <= 0) {
          useWrapper = false;
        }
      }
      if (useWrapper) {
        url = b2Context.getPath() + URLEncoder.encode(url, "UTF-8");
        url = "/webapps/blackboard/content/contentWrapper.jsp?content_id=" + contentId +
              "&displayName=" + URLEncoder.encode(title, "UTF-8") + "&course_id=" + courseId +
              "&navItem=content&href=" + url;
      }
      response.sendRedirect(url);
    }
  }
%>
</bbNG:learningSystemPage>
