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
        import="blackboard.data.content.Content,
                blackboard.persist.content.ContentDbLoader,
                blackboard.persist.BbPersistenceManager,
                blackboard.persist.Id,
                blackboard.platform.persistence.PersistenceServiceFactory,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.lti.Tool,
                org.oscelot.blackboard.lti.Constants,
                org.oscelot.blackboard.lti.Utils"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<%
  Utils.checkForModule(request);
  B2Context b2Context = new B2Context(request);
  String courseId = b2Context.getRequestParameter("course_id", "");
  String contentId = b2Context.getRequestParameter("content_id", "");
  String tabId = b2Context.getRequestParameter(Constants.TAB_PARAMETER_NAME, "");
  String toolId = b2Context.getRequestParameter(Constants.TOOL_ID,
     b2Context.getSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID, ""));
  String sourcePage = b2Context.getRequestParameter(Constants.PAGE_PARAMETER_NAME, "");
  boolean isError = b2Context.getRequestParameter("error", "").length() > 0;
  Tool tool = new Tool(b2Context, toolId);
  boolean openInParent = ((tool.getOpenIn().equals(Constants.DATA_IFRAME) && (tabId.length() <= 0)) ||
                         (tool.getOpenIn().equals(Constants.DATA_FRAME) &&
                          B2Context.getIsVersion(9, 1, 201404)) ||
                         (tool.getOpenIn().equals(Constants.DATA_FRAME_NO_BREADCRUMBS) &&
                          B2Context.getIsVersion(9, 1, 201404))) && !isError;
  boolean openInWindow = tool.getOpenIn().equals(Constants.DATA_WINDOW) && !isError;

  String ltiLog = b2Context.getRequestParameter(Constants.LTI_LOG, "");
  if (ltiLog.length() > 0) {
    System.err.println("LTI log: " + ltiLog);
  }
  ltiLog = b2Context.getRequestParameter(Constants.LTI_ERROR_LOG, "");
  if (ltiLog.length() > 0) {
    System.err.println("LTI error log: " + ltiLog);
  }
  String ltiMessage = b2Context.getRequestParameter(Constants.LTI_MESSAGE, "");
  String ltiError = b2Context.getRequestParameter(Constants.LTI_ERROR_MESSAGE, "");

  String url = "";
  if (!openInWindow) {
    if (contentId.length() > 0) {
      BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
      ContentDbLoader courseDocumentLoader = (ContentDbLoader)bbPm.getLoader(ContentDbLoader.TYPE);
      Id id = bbPm.generateId(Content.DATA_TYPE, contentId);
      Content content = courseDocumentLoader.loadById(id);
      if (!content.getIsFolder()) {
        id = content.getParentId();
        contentId = id.toExternalString();
      }
      String navItem = "cp_content_quickdisplay";
      if (B2Context.getEditMode()) {
        navItem = "cp_content_quickedit";
      }
      url = b2Context.getNavigationItem(navItem).getHref();
      url = url.replace("@X@course.pk_string@X@", courseId);
      url = url.replace("@X@content.pk_string@X@", contentId);
    } else if (tabId.length() > 0) {
      url = "/webapps/portal/execute/tabs/tabAction?tab_tab_group_id=" + tabId;
    } else if (sourcePage.length() <= 0) {
      url = b2Context.getNavigationItem("course_tools_area").getHref();
      url = url.replace("@X@course.pk_string@X@", courseId);
    } else {
      if (sourcePage.equals(Constants.COURSE_TOOLS_PAGE)) {
        url = "course/";
      } else if (sourcePage.equals(Constants.ADMIN_PAGE)) {
        url = "system/";
      }
      String query = "&" + Utils.getQuery(request);
      query = query.replaceAll("&" + Constants.PAGE_PARAMETER_NAME + "=[^&]*", "");
      query = query.replaceAll("&" + Constants.PAGE_PARAMETER_NAME + "2=", "&" + Constants.PAGE_PARAMETER_NAME + "=");
      if (query.length() > 0) {
        query = query.substring(1);
      }
      url += "tools.jsp?" + query;
    }
    url = b2Context.setReceiptOptions(url, ltiMessage, ltiError);
  }

  if (!openInWindow && !openInParent) {
    response.sendRedirect(url);
    return;
  } else if (!openInParent && ((ltiMessage.length() > 0) || (ltiError.length() > 0))) {
    b2Context.setReceiptOptions(ltiMessage, ltiError);
  }

  pageContext.setAttribute("bundle", b2Context.getResourceStrings());

  if (openInParent) {
%>
<html>
<head>
<title>${bundle['page.course_tool.tools.title']}</title>
<script language="javascript" type="text/javascript">
function doOnLoad() {
  document.forms[0].submit();
}
window.onload=doOnLoad;
</script>
</head>
<body>
<p>
${bundle['page.course.tool.redirect.label']}
</p>
<form action="<%=url%>" method="post" target="_parent" />
</body>
</html>
<%
  } else {
%>
<bbNG:genericPage title="${bundle['page.course_tool.tools.title']}">
  <bbNG:pageHeader>
    <bbNG:pageTitleBar iconUrl="images/lti.gif" showTitleBar="true" title="${bundle['plugin.name']}"/>
  </bbNG:pageHeader>
  <bbNG:form>
    <bbNG:button label="${bundle['page.close.window']}" onClick="window.close();" />
  </bbNG:form>
</bbNG:genericPage>
<%
  }
%>
