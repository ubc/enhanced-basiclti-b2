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
      1.1.2  9-Oct-10  Added OpenIn and WindowName settings
                       Corrected names of LTI roles
      1.1.3  1-Jan-11  Added User ID type option
      1.2.0 17-Sep-11  Added support for outcomes, memberships and setting extension services
      1.2.1 10-Oct-11  Added custom parameters option
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
      2.0.1 20-May-12  Fixed page doctype
      2.1.0 18-Jun-12  Added "By XML" option
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12  Added options for creating grade center columns
      2.3.1 17-Dec-12  Added grade column options
      2.3.2  3-Apr-13
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.Map,
                java.util.HashMap,
                java.util.Iterator,
                java.util.UUID,
                java.net.URL,
                java.net.MalformedURLException,
                blackboard.platform.security.CourseRole,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.basiclti.Utils,
                org.oscelot.blackboard.basiclti.Tool,
                org.oscelot.blackboard.basiclti.ToolList,
                org.oscelot.blackboard.basiclti.Constants"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:learningSystemPage title="${bundle['page.system.tool.title']}">
<%
  B2Context b2Context = new B2Context(request);
  String query = Utils.getQuery(request);
  String cancelUrl = "tools.jsp?" + query;
  String toolId = b2Context.getRequestParameter(Constants.TOOL_ID, "");
  String toolName = b2Context.getRequestParameter(Constants.TOOL_NAME, "");
  String toolUrl = b2Context.getRequestParameter(Constants.TOOL_URL, "");
  String xml = b2Context.getRequestParameter(Constants.TOOL_XML, "");
  boolean byXML = (xml.length() > 0);

  boolean ok = true;
  boolean submitForm = request.getMethod().equalsIgnoreCase("POST");
  boolean isNewTool = (toolId.length() <= 0);

  String messageResourceString = null;
  Map<String,String> params = new HashMap<String,String>();
  params.put(Constants.TOOL_ID, toolId);

  Map<String,String> settings = null;
  if (byXML) {
    boolean isSecure = b2Context.getServerUrl().startsWith("https://");
    settings = Utils.getToolFromXML(xml, isSecure, false, false, false);
    if (settings.containsKey(Constants.TOOL_NAME)) {
      if (toolName.length() <= 0) {
        toolName = settings.get(Constants.TOOL_NAME);
      }
      settings.remove(Constants.TOOL_NAME);
    }
    if (settings.containsKey(Constants.TOOL_URL)) {
      if (toolUrl.length() <= 0) {
        toolUrl = settings.get(Constants.TOOL_URL);
      }
      settings.remove(Constants.TOOL_URL);
    }
  }
  if (submitForm && isNewTool) {
//    toolId = Constants.COURSE_TOOL_PREFIX + Utils.getNewToolId(b2Context, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.COURSE_TOOL_PREFIX, toolName);
    toolId = Utils.getNewToolId(b2Context, toolName, false, false);
System.err.println("new tool: " + toolName + "/" + toolId);
  } else if (toolId.length() <= 0) {
    toolId = Constants.DEFAULT_TOOL_ID;
  }
  String toolSettingPrefix = Constants.TOOL_PARAMETER_PREFIX + "." + toolId + ".";
  if (submitForm) {
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_NAME, toolName);
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_URL, toolUrl);
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_GUID, b2Context.getRequestParameter(Constants.TOOL_GUID, ""));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_SECRET, b2Context.getRequestParameter(Constants.TOOL_SECRET, ""));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES, Constants.DATA_NOTUSED));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_COLUMN, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_COLUMN, Constants.DATA_FALSE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_FORMAT, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_POINTS, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_POINTS, Constants.DEFAULT_POINTS_POSSIBLE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_SCORABLE, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_SCORABLE, Constants.DATA_FALSE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_VISIBLE, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_VISIBLE, Constants.DATA_FALSE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS, b2Context.getRequestParameter(Constants.TOOL_EXT_MEMBERSHIPS, Constants.DATA_NOTUSED));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS_LIMIT, b2Context.getRequestParameter(Constants.TOOL_EXT_MEMBERSHIPS_LIMIT, Constants.DATA_FALSE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS_GROUPS, b2Context.getRequestParameter(Constants.TOOL_EXT_MEMBERSHIPS_GROUPS, Constants.DATA_FALSE));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES, b2Context.getRequestParameter(Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES, ""));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_SETTING, b2Context.getRequestParameter(Constants.TOOL_EXT_SETTING, Constants.DATA_NOTUSED));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_CSS, b2Context.getRequestParameter(Constants.TOOL_CSS, ""));
    b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_ICON, b2Context.getRequestParameter(Constants.TOOL_ICON, ""));
    if (b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_UUID, "").length() <= 0) {
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_UUID, UUID.randomUUID().toString());
    }
    if (isNewTool) {
      String defaultToolSettingPrefix = Constants.TOOL_PARAMETER_PREFIX + "." + Constants.DEFAULT_TOOL_ID + ".";
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_CONTEXT_ID,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_CONTEXT_ID, Constants.DATA_FALSE));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_CONTEXTIDTYPE,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_CONTEXTIDTYPE, Constants.DATA_BATCHUID));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_CONTEXT_SOURCEDID,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_CONTEXT_SOURCEDID, Constants.DATA_FALSE));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_CONTEXT_TITLE,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_CONTEXT_TITLE, Constants.DATA_FALSE));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_USERID,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_USERID, Constants.DATA_NOTUSED));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_USERIDTYPE,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_USERIDTYPE, Constants.DATA_BATCHUID));
     b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_USER_SOURCEDID,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_USER_SOURCEDID, Constants.DATA_FALSE));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_USERNAME,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_USERNAME, Constants.DATA_NOTUSED));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_EMAIL,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_EMAIL, Constants.DATA_NOTUSED));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_AVATAR,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_AVATAR, Constants.DATA_FALSE));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_ROLES,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_ROLES, Constants.DATA_FALSE));
      boolean systemRolesOnly = !b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_COURSE_ROLES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
      for (Iterator<CourseRole> iter = Utils.getCourseRoles(systemRolesOnly).iterator(); iter.hasNext();) {
        CourseRole role = iter.next();
        b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_ROLE + "." + role.getIdentifier(),
           b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_ROLE + "." + role.getIdentifier(), ""));
      }
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_ADMINISTRATOR,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_ADMINISTRATOR, Constants.DATA_FALSE));

      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_OPEN_IN,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_OPEN_IN, Constants.DATA_FRAME));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_WINDOW_NAME,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_WINDOW_NAME, ""));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_SPLASH,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_SPLASH, Constants.DATA_FALSE));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_SPLASHFORMAT,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_SPLASHFORMAT, "H"));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_SPLASHTEXT,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_SPLASHTEXT, ""));
      b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_CUSTOM,
         b2Context.getSetting(defaultToolSettingPrefix + Constants.TOOL_CUSTOM, ""));
    }
    if (byXML) {
      for (Iterator<Map.Entry<String,String>> iter = settings.entrySet().iterator(); iter.hasNext();) {
        Map.Entry<String,String> setting = iter.next();
        b2Context.setSetting(false, true, toolSettingPrefix + setting.getKey(), setting.getValue());
      }
      Tool domain = Utils.urlToDomain(b2Context, toolUrl);
      if (domain != null) {
        ok = (domain.getGUID().length() > 0) || (domain.getSecret().length() > 0);
      }
      if (!ok) {
        ok = (b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_GUID, "").length() > 0) &&
             (b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_SECRET, "").length() > 0);
      } else if (domain != null) {
        b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_GUID, null);
        b2Context.setSetting(false, true, toolSettingPrefix + Constants.TOOL_SECRET, null);
      }
      if (!ok) {
        messageResourceString = "page.system.tool.receipt.xml";
      }
    }
    ok = (toolName.length() > 0) && (toolUrl.length() > 0);
    if (ok && (toolUrl.length() > 0)) {
      try {
        URL url = new URL(toolUrl);
      } catch (MalformedURLException e) {
        ok = false;
        messageResourceString = "page.system.tool.receipt.invalidurl";
      }
    } else if (ok) {
      messageResourceString = "page.receipt.success";
    } else if (messageResourceString == null) {
      messageResourceString = "page.system.tool.receipt.incompletetool";
    }

    if (ok) {
      if (isNewTool) {
        ToolList toolList = new ToolList(b2Context);
        toolList.setTool(toolId);
      } else {
        b2Context.persistSettings(false, true);
      }
      cancelUrl = b2Context.setReceiptOptions(cancelUrl,
         b2Context.getResourceString(messageResourceString), null);
      response.sendRedirect(cancelUrl);
    }
  }

  if (messageResourceString != null) {
    b2Context.setReceipt(b2Context.getResourceString(messageResourceString), !submitForm);
  }

  boolean domainAuth = false;
  if (!isNewTool) {
    Tool tool = Utils.urlToDomain(b2Context, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_URL));
    if (tool != null) {
      domainAuth = (tool.getGUID().length() > 0) || (tool.getSecret().length() > 0);
    }
  }

  boolean hasLineItem = b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_LINEITEM, "").length() > 0;

  Map<String,String> resourceStrings = b2Context.getResourceStrings();
  pageContext.setAttribute("bundle", resourceStrings);
  if (!isNewTool) {
    pageContext.setAttribute("titleSuffix", ": " + b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_NAME));
  }
  pageContext.setAttribute("courseId", b2Context.getRequestParameter("course_id", ""));

  params.put(Constants.TOOL_NAME, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_NAME));
  params.put(Constants.TOOL_URL, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_URL));
  params.put(Constants.TOOL_GUID, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_GUID));
  params.put(Constants.TOOL_SECRET, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_SECRET));
  params.put(Constants.TOOL_EXT_OUTCOMES, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES, Constants.DATA_NOTUSED));
  params.put(Constants.TOOL_EXT_OUTCOMES_COLUMN, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_COLUMN, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_OUTCOMES_FORMAT, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE));
  params.put(Constants.TOOL_EXT_OUTCOMES_POINTS, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_POINTS, Constants.DEFAULT_POINTS_POSSIBLE));
  params.put(Constants.TOOL_EXT_OUTCOMES_SCORABLE, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_SCORABLE, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_OUTCOMES_VISIBLE, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_VISIBLE, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_MEMBERSHIPS, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS, Constants.DATA_NOTUSED));
  params.put(Constants.TOOL_EXT_MEMBERSHIPS_LIMIT, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS_LIMIT, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_MEMBERSHIPS_GROUPS, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS_GROUPS, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES));
  params.put(Constants.TOOL_EXT_SETTING, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_SETTING, Constants.DATA_NOTUSED));
  params.put(Constants.TOOL_CSS, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_CSS));
  params.put(Constants.TOOL_ICON, b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_ICON));

  boolean outcomesEnabled = b2Context.getSetting("ext_outcomes", Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  boolean membershipsEnabled = b2Context.getSetting("ext_memberships", Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  boolean settingEnabled = b2Context.getSetting("ext_setting", Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
  boolean scorable = params.get(Constants.TOOL_EXT_OUTCOMES_SCORABLE).equals(Constants.DATA_TRUE);
  boolean visible = params.get(Constants.TOOL_EXT_OUTCOMES_VISIBLE).equals(Constants.DATA_TRUE);
  params.put("ext_outcomes" + params.get(Constants.TOOL_EXT_OUTCOMES), "true");
  params.put("ext_outcomes_format" + params.get(Constants.TOOL_EXT_OUTCOMES_FORMAT), "true");
  params.put("ext_memberships" + params.get(Constants.TOOL_EXT_MEMBERSHIPS), "true");
  params.put("ext_setting" + params.get(Constants.TOOL_EXT_SETTING), "true");
  pageContext.setAttribute("query", query);
  pageContext.setAttribute("params", params);
  pageContext.setAttribute("cancelUrl", cancelUrl);
%>
  <bbNG:pageHeader instructions="${bundle['page.system.tool.instructions']}">
    <bbNG:breadcrumbBar environment="CTRL_PANEL">
      <bbNG:breadcrumb href="tools.jsp?${query}" title="${bundle['plugin.name']}" />
      <bbNG:breadcrumb title="${bundle['page.system.tool.title']}" />
    </bbNG:breadcrumbBar>
    <bbNG:pageTitleBar iconUrl="../images/lti.gif" showTitleBar="true" title="${bundle['page.system.tool.title']}${titleSuffix}"/>
  </bbNG:pageHeader>
<bbNG:form action="tool.jsp?${query}" name="toolForm" method="post" onsubmit="return validateForm();">
<%
  if (!isNewTool) {
%>
  <input type="hidden" name="<%=Constants.TOOL_ID%>" value="<%=params.get(Constants.TOOL_ID)%>" />
<%
  }
%>
  <bbNG:dataCollection markUnsavedChanges="true" showSubmitButtons="true">
    <bbNG:stepGroup active="true" title="${bundle['page.system.tool.tab.bysetting']}">
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.step1.title']}" instructions="${bundle['page.system.tool.step1.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step1.name.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_NAME%>" value="<%=params.get(Constants.TOOL_NAME)%>" size="50" helpText="${bundle['page.system.tool.step1.name.instructions']}" />
        </bbNG:dataElement>
      </bbNG:step>
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.step2.title']}" instructions="${bundle['page.system.tool.step2.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step2.url.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_URL%>" value="<%=params.get(Constants.TOOL_URL)%>" size="80" helpText="${bundle['page.system.tool.step2.url.instructions']}" />
        </bbNG:dataElement>
<%
  if (!domainAuth) {
%>
        <bbNG:dataElement isRequired="false" label="${bundle['page.system.tool.step2.guid.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_GUID%>" value="<%=params.get(Constants.TOOL_GUID)%>" size="50" helpText="${bundle['page.system.tool.step2.guid.instructions']}" />
        </bbNG:dataElement>
        <bbNG:dataElement isRequired="false" label="${bundle['page.system.tool.step2.secret.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_SECRET%>" value="<%=params.get(Constants.TOOL_SECRET)%>" size="50" helpText="${bundle['page.system.tool.step2.secret.instructions']}" />
        </bbNG:dataElement>
<%
  }
%>
      </bbNG:step>
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.step3.title']}" instructions="${bundle['page.system.tool.step3.instructions']}">
<%
  if (!outcomesEnabled && !membershipsEnabled && !settingEnabled) {
%>
<p>${bundle['page.system.tool.step3.none']}</p>
<%
  }
  if (outcomesEnabled) {
%>
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes.label']}">
          <bbNG:selectElement name="<%=Constants.TOOL_EXT_OUTCOMES%>" helpText="${bundle['page.system.tool.step3.outcomes.instructions']}">
            <bbNG:selectOptionElement isSelected="${params.ext_outcomesN}" value="<%=Constants.DATA_NOTUSED%>" optionLabel="${bundle['page.system.data.select.notused']}" />
            <bbNG:selectOptionElement isSelected="${params.ext_outcomesM}" value="<%=Constants.DATA_MANDATORY%>" optionLabel="${bundle['page.system.data.select.mandatory']}" />
          </bbNG:selectElement><br />
        </bbNG:dataElement>
<%
      if (hasLineItem) {
%>
        ${bundle['page.course.edit.step3.lineitem.label']}<br />
<%
      } // else if (params.get(Constants.TOOL_EXT_OUTCOMES).equals(Constants.DATA_MANDATORY)) {
%>
        <bbNG:dataElement isSubElement="true" subElementType="INDENTED_NESTED_LIST">
          <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_column.label']}">
            <bbNG:checkboxElement isSelected="${params.ext_outcomes_column}" name="<%=Constants.TOOL_EXT_OUTCOMES_COLUMN%>" value="true" helpText="${bundle['page.system.tool.step3.outcomes_column.instructions']}" />
          </bbNG:dataElement>
          <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_format.label']}">
            <bbNG:selectElement name="<%=Constants.TOOL_EXT_OUTCOMES_FORMAT%>" helpText="${bundle['page.system.tool.step3.outcomes_format.instructions']}">
              <bbNG:selectOptionElement isSelected="${params.ext_outcomes_formatP}" value="<%=Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE%>" optionLabel="${bundle['page.system.tool.step3.outcomes_format.percentage']}" />
              <bbNG:selectOptionElement isSelected="${params.ext_outcomes_formatS}" value="<%=Constants.EXT_OUTCOMES_COLUMN_SCORE%>" optionLabel="${bundle['page.system.tool.step3.outcomes_format.score']}" />
            </bbNG:selectElement>
          </bbNG:dataElement>
          <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_points.label']}">
            <bbNG:textElement type="unsigned_integer" name="<%=Constants.TOOL_EXT_OUTCOMES_POINTS%>" value="<%=params.get(Constants.TOOL_EXT_OUTCOMES_POINTS)%>" title="${bundle['page.system.tool.step3.outcomes_points.label']}" size="5" maxLength="3" helpText="${bundle['page.system.tool.step3.outcomes_points.instructions']}" />
          </bbNG:dataElement>
          <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_scorable.label']}">
            <bbNG:radioElement optionLabel="${bundle['option.true']}" isSelected="<%=scorable%>" name="<%=Constants.TOOL_EXT_OUTCOMES_SCORABLE%>" value="true" />
            <bbNG:radioElement optionLabel="${bundle['option.false']}" isSelected="<%=!scorable%>" name="<%=Constants.TOOL_EXT_OUTCOMES_SCORABLE%>" value="false" />
            <bbNG:elementInstructions text="${bundle['page.system.tool.step3.outcomes_scorable.instructions']}" />
          </bbNG:dataElement>
          <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_visible.label']}">
            <bbNG:radioElement optionLabel="${bundle['option.true']}" isSelected="<%=visible%>" name="<%=Constants.TOOL_EXT_OUTCOMES_VISIBLE%>" value="true" />
            <bbNG:radioElement optionLabel="${bundle['option.false']}" isSelected="<%=!visible%>" name="<%=Constants.TOOL_EXT_OUTCOMES_VISIBLE%>" value="false" />
            <bbNG:elementInstructions text="${bundle['page.system.tool.step3.outcomes_visible.instructions']}" />
          </bbNG:dataElement>
        </bbNG:dataElement>
<%
     // }
  }
  if (membershipsEnabled) {
%>
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.memberships.label']}">
          <bbNG:selectElement name="<%=Constants.TOOL_EXT_MEMBERSHIPS%>" helpText="${bundle['page.system.tool.step3.memberships.instructions']}">
            <bbNG:selectOptionElement isSelected="${params.ext_membershipsN}" value="<%=Constants.DATA_NOTUSED%>" optionLabel="${bundle['page.system.data.select.notused']}" />
            <bbNG:selectOptionElement isSelected="${params.ext_membershipsM}" value="<%=Constants.DATA_MANDATORY%>" optionLabel="${bundle['page.system.data.select.mandatory']}" />
          </bbNG:selectElement><br />
          <bbNG:checkboxElement isSelected="${params.ext_memberships_limit}" name="<%=Constants.TOOL_EXT_MEMBERSHIPS_LIMIT%>" value="true" optionLabel="${bundle['page.system.tool.step3.memberships_limit.label']}" helpText="${bundle['page.system.tool.step3.memberships_limit.instructions']}" /><br />
          <bbNG:checkboxElement isSelected="${params.ext_memberships_groups}" name="<%=Constants.TOOL_EXT_MEMBERSHIPS_GROUPS%>" value="true" optionLabel="${bundle['page.system.tool.step3.memberships_groups.label']}" helpText="${bundle['page.system.tool.step3.memberships_groups.instructions']}" /><br />
          ${bundle['page.system.tool.step3.memberships_groupnames.label']}&nbsp;<bbNG:textElement type="string" name="<%=Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES%>" value="<%=params.get(Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES)%>" size="20" helpText="${bundle['page.system.tool.step3.memberships_groupnames.instructions']}" />
        </bbNG:dataElement>
<%
  }
  if (settingEnabled) {
%>
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.setting.label']}">
          <bbNG:selectElement name="<%=Constants.TOOL_EXT_SETTING%>" helpText="${bundle['page.system.tool.step3.setting.instructions']}">
            <bbNG:selectOptionElement isSelected="${params.ext_settingN}" value="<%=Constants.DATA_NOTUSED%>" optionLabel="${bundle['page.system.data.select.notused']}" />
            <bbNG:selectOptionElement isSelected="${params.ext_settingM}" value="<%=Constants.DATA_MANDATORY%>" optionLabel="${bundle['page.system.data.select.mandatory']}" />
          </bbNG:selectElement>
        </bbNG:dataElement>
<%
  }
%>
      </bbNG:step>
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.step4.title']}" instructions="${bundle['page.system.tool.step4.instructions']}">
        <bbNG:dataElement isRequired="false" label="${bundle['page.system.tool.step4.css.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_CSS%>" value="<%=params.get(Constants.TOOL_CSS)%>" size="80" helpText="${bundle['page.system.tool.step4.css.instructions']}" />
        </bbNG:dataElement>
        <bbNG:dataElement isRequired="false" label="${bundle['page.system.tool.step4.icon.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_ICON%>" value="<%=params.get(Constants.TOOL_ICON)%>" size="80" helpText="${bundle['page.system.tool.step4.icon.instructions']}" />
        </bbNG:dataElement>
      </bbNG:step>
    </bbNG:stepGroup>
    <bbNG:stepGroup active="false>" title="${bundle['page.system.tool.tab.byxml']}">
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.xml.title']}" instructions="${bundle['page.system.tool.xml.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.xml.label']}">
          <textarea name="<%=Constants.TOOL_XML%>" cols="80" rows="20"></textarea>
        </bbNG:dataElement>
      </bbNG:step>
    </bbNG:stepGroup>
    <bbNG:stepSubmit hideNumber="false" showCancelButton="true" cancelUrl="${cancelUrl}" />
  </bbNG:dataCollection>
  </bbNG:form>
</bbNG:learningSystemPage>
