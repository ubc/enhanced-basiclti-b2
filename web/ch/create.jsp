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
      1.1.2  9-Oct-10
      1.1.3  1-Jan-11
      1.2.0 17-Sep-11  Added support for outcomes, memberships and setting extension services
      1.2.1 10-Oct-11  Added custom parameters option
      1.2.2 13-Oct-11  Fixed bug with creating content item not persisting position value
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
      2.0.1 20-May-12  Fixed page doctype
      2.1.0 18-Jun-12  Added "By URL" and "By XML" options
                       Changed to use default render type
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12  Added grade column options when defining tools by URL
      2.3.2  3-Apr-13
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.Arrays,
                java.util.Map,
                java.util.HashMap,
                java.util.Iterator,
                java.util.UUID,
                java.net.URL,
                java.net.MalformedURLException,
                blackboard.platform.context.Context,
                blackboard.platform.context.ContextManagerFactory,
                blackboard.platform.persistence.PersistenceServiceFactory,
                blackboard.persist.BbPersistenceManager,
                blackboard.data.course.Course,
                blackboard.data.content.Content,
                blackboard.persist.Id,
                blackboard.base.FormattedText,
                blackboard.persist.content.ContentDbLoader,
                blackboard.persist.content.ContentDbPersister,
                blackboard.servlet.tags.ngui.datacollection.fields.TextboxTag,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.basiclti.Gradebook,
                org.oscelot.blackboard.basiclti.Gradebook_v90,
                org.oscelot.blackboard.basiclti.Constants,
                org.oscelot.blackboard.basiclti.ToolList,
                org.oscelot.blackboard.basiclti.Tool,
                org.oscelot.blackboard.basiclti.Utils"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:learningSystemPage title="${bundle['page.content.create.title']}" onLoad="doOnLoad()">
<%
  B2Context b2Context = new B2Context(request);
  Utils.checkCourse(b2Context);
  b2Context.setIgnoreContentContext(true);
  ToolList toolList = new ToolList(b2Context);
  boolean allowLocal = b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_DELEGATE, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);

  String courseIdParamName = "course_id";
  String contentIdParamName = "content_id";
  String courseId = b2Context.getRequestParameter(courseIdParamName, "");
  String contentId = b2Context.getRequestParameter(contentIdParamName, "");
  String toolId = b2Context.getRequestParameter(Constants.TOOL_ID, "");
  String toolUrl = b2Context.getRequestParameter(Constants.TOOL_URL, "");
  String toolName = b2Context.getRequestParameter(Constants.TOOL_NAME, "");
  String xml = b2Context.getRequestParameter(Constants.TOOL_XML, "");

  boolean byXML = allowLocal && (xml.length() > 0);
  boolean xmlTab = byXML;
  boolean urlTab = allowLocal && !xmlTab && ((toolName.length() > 0) || (toolUrl.length() > 0));
  boolean nameTab = !urlTab && !xmlTab;

  String cancelUrl = b2Context.getNavigationItem("cp_content_quickedit").getHref();
  cancelUrl = cancelUrl.replace("@X@course.pk_string@X@", courseId);
  cancelUrl = cancelUrl.replace("@X@content.pk_string@X@", contentId);

  boolean outcomesEnabled = b2Context.getSetting(Constants.TOOL_EXT_OUTCOMES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);

  String errorResourceString = null;
  Map<String,String> params = null;
  boolean ok = request.getMethod().equalsIgnoreCase("POST");
  if (byXML) {
    ok = (toolId.length() <= 0) && (toolUrl.length() <= 0) && (toolName.length() <= 0);
    if (!ok) {
      errorResourceString = "page.content.create.receipt.bothtools";
    } else {
      boolean isSecure = b2Context.getServerUrl().startsWith("https://");
      params = Utils.getToolFromXML(xml, isSecure, false, false, true);
      toolUrl = params.get(Constants.TOOL_URL);
      toolName = params.get(Constants.TOOL_NAME);
      ok = (toolUrl != null) && (toolName != null) && (toolUrl.length() > 0) && (toolName.length() > 0);
      if (!ok) {
        errorResourceString = "page.content.create.receipt.incompletexml";
      } else {
        params.remove(Constants.TOOL_URL);
        params.remove(Constants.TOOL_NAME);
      }
    }
  } else if (ok) {
    ok = ((toolId.length() > 0) ^ ((toolUrl.length() > 0) || (toolName.length() > 0)));
    if (!ok) {
      if (toolId.length() <= 0) {
        errorResourceString = "page.content.create.receipt.notool";
      } else {
        errorResourceString = "page.content.create.receipt.bothtools";
      }
    } else if (toolId.length() <= 0) {
      ok = (toolUrl.length() > 0) && (toolName.length() > 0);
      if (!ok) {
        errorResourceString = "page.system.tool.receipt.incompletetool";
      } else {
        try {
          URL url = new URL(toolUrl);
        } catch (MalformedURLException e) {
          ok = false;
          errorResourceString = "page.system.tool.receipt.invalidurl";
        }
      }
    } else if (!toolList.isTool(toolId)) {
      ok = false;
      errorResourceString = "page.content.create.receipt.invalidtool";
    }
  } else if ((toolId.length() > 0) && toolList.isTool(toolId)) {
    ok = true;
  }

  if (ok) {

    if (toolId.length() > 0) {
      Tool tool = new Tool(b2Context, toolId);
      toolName = tool.getName();
    }

    BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
    ContentDbPersister persister = (ContentDbPersister)bbPm.getPersister(ContentDbPersister.TYPE);

    Content content = new Content();
    content.setContentHandler(Constants.RESOURCE_HANDLE);
    Id childId = bbPm.generateId(Course.DATA_TYPE, courseId);
    Id parentId = bbPm.generateId(Content.DATA_TYPE, contentId);
    content.setCourseId(childId);
    content.setParentId(parentId);
    content.setRenderType(Content.RenderType.DEFAULT);  // URL ???
    content.setTitle(toolName);
    if (byXML && params.containsKey(Constants.TOOL_DESCRIPTION)) {
      FormattedText text = new FormattedText(params.get(Constants.TOOL_DESCRIPTION), FormattedText.Type.PLAIN_TEXT);
      content.setBody(text);
      params.remove(Constants.TOOL_DESCRIPTION);
    }
    persister.persist(content);

    contentId = content.getId().toExternalString();
    String contentQuery = courseIdParamName + "=" + courseId + "&" + contentIdParamName + "=" + contentId;

    Context context = b2Context.getContext();
    B2Context contentContext = new B2Context(request);
    contentContext.setContext(ContextManagerFactory.getInstance().setContext(context.getVirtualHost().getId(),
       context.getCourseId(), Id.UNSET_ID, Id.UNSET_ID, content.getId()));
    String toolSettingPrefix = null;
    String contentSettingPrefix = null;
    boolean isDomain = false;
    boolean createColumn = false;
    if (toolId.length() <= 0) {
      contentSettingPrefix =  Constants.TOOL_PARAMETER_PREFIX + ".";
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_URL, toolUrl);
      boolean hasAuth = false;
      Tool domain = Utils.urlToDomain(b2Context, toolUrl);
      if (domain != null) {
        hasAuth = (domain.getGUID().length() > 0) || (domain.getSecret().length() > 0);
        toolSettingPrefix = Constants.DOMAIN_PARAMETER_PREFIX + "." + domain.getId() + ".";
        isDomain = true;
        createColumn = domain.getOutcomesService().equals(Constants.DATA_MANDATORY) &&
            domain.getOutcomesColumn().equals(Constants.DATA_TRUE);
      }
      if (params != null) {
        if (hasAuth) {
          params.remove(Constants.TOOL_GUID);
          params.remove(Constants.TOOL_SECRET);
        } else {
          hasAuth = params.containsKey(Constants.TOOL_GUID) || params.containsKey(Constants.TOOL_SECRET);
        }
      }
      if (!hasAuth) {
        cancelUrl = "modify.jsp?" + contentQuery;
        cancelUrl = contentContext.setReceiptOptions(cancelUrl,
           contentContext.getResourceString("page.system.tool.receipt.xml"), null);
      } else {
        cancelUrl = contentContext.setReceiptOptions(cancelUrl,
           contentContext.getResourceString("page.receipt.success"), null);
      }
    } else {
      contentSettingPrefix = Constants.TOOL_PARAMETER_PREFIX + "." + toolId + ".";
      toolSettingPrefix = contentSettingPrefix;
      contentContext.setSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID, toolId);
      cancelUrl = "modify.jsp?" + contentQuery;
      cancelUrl = contentContext.setReceiptOptions(cancelUrl,
         contentContext.getResourceString("page.content.create.receipt.success"), null);
    }
    if (toolSettingPrefix != null) {
      b2Context.setIgnoreContentContext(true);
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_USERID,
         b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_USERID,
         b2Context.getSetting(toolSettingPrefix + Constants.TOOL_USERID, Constants.DATA_NOTUSED)));
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_USERNAME,
         b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_USERNAME,
         b2Context.getSetting(toolSettingPrefix + Constants.TOOL_USERNAME, Constants.DATA_NOTUSED)));
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EMAIL,
         b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EMAIL,
         b2Context.getSetting(toolSettingPrefix + Constants.TOOL_EMAIL, Constants.DATA_NOTUSED)));
      String enable = null;
      if (b2Context.getSetting(isDomain, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES,
                               Constants.DATA_NOTUSED).equals(Constants.DATA_MANDATORY)) {
        enable = Constants.DATA_TRUE;
      } else {
        enable = Constants.DATA_FALSE;
      }
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_OUTCOMES, enable);
      if (!createColumn) {
        contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_OUTCOMES_FORMAT,
           b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_FORMAT,
           b2Context.getSetting(toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE)));
        contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_OUTCOMES_POINTS,
           b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_POINTS,
           b2Context.getSetting(toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_POINTS, Constants.DEFAULT_POINTS_POSSIBLE)));
        contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_OUTCOMES_SCORABLE,
           b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_SCORABLE,
           b2Context.getSetting(toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_SCORABLE, Constants.DATA_FALSE)));
        contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_OUTCOMES_VISIBLE,
           b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_VISIBLE,
           b2Context.getSetting(toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_VISIBLE, Constants.DATA_FALSE)));
      }
      if (b2Context.getSetting(isDomain, true, toolSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS,
                               Constants.DATA_NOTUSED).equals(Constants.DATA_MANDATORY)) {
        enable = Constants.DATA_TRUE;
      } else {
        enable = Constants.DATA_FALSE;
      }
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_MEMBERSHIPS, enable);
      if (b2Context.getSetting(isDomain, true, toolSettingPrefix + Constants.TOOL_EXT_SETTING,
                               Constants.DATA_NOTUSED).equals(Constants.DATA_MANDATORY)) {
        enable = Constants.DATA_TRUE;
      } else {
        enable = Constants.DATA_FALSE;
      }
      contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_SETTING, enable);
    }
    contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_CUSTOM, "");
    contentContext.setSetting(false, true, contentSettingPrefix + Constants.TOOL_EXT_UUID,
       UUID.randomUUID().toString());
    if (params != null) {
      for (Iterator<Map.Entry<String,String>> iter = params.entrySet().iterator(); iter.hasNext();) {
        Map.Entry<String,String> param = iter.next();
        contentContext.setSetting(false, true, contentSettingPrefix + param.getKey(), param.getValue());
      }
    }
    contentContext.persistSettings(false, true);

    if (createColumn) {
      String outcomes_format = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE);
      String points = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_POINTS, Constants.DEFAULT_POINTS_POSSIBLE);
      boolean scorable = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_SCORABLE, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
      boolean visible = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_VISIBLE, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
      Utils.checkColumn(contentContext, null, toolName, outcomes_format, Utils.stringToInteger(points),
         scorable, visible, true);
    }

    response.sendRedirect(cancelUrl);
  }

  if (errorResourceString != null) {
    b2Context.setReceipt(b2Context.getResourceString(errorResourceString), false);
  }

  String outcomes_format = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE);
  String points = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_POINTS, Constants.DEFAULT_POINTS_POSSIBLE);
  boolean scorable = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_SCORABLE, Constants.DATA_TRUE).equals(Constants.DATA_TRUE);
  boolean visible = b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES_VISIBLE, Constants.DATA_TRUE).equals(Constants.DATA_TRUE);

  pageContext.setAttribute("scorable", Constants.TOOL_EXT_OUTCOMES_SCORABLE);
  pageContext.setAttribute("visible", Constants.TOOL_EXT_OUTCOMES_VISIBLE);
  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("courseId", courseId);
  pageContext.setAttribute("contentId", contentId);
  pageContext.setAttribute("outcomes_format" + outcomes_format, "true");
%>
  <bbNG:pageHeader instructions="${bundle['page.content.create.instructions']}">
    <bbNG:breadcrumbBar>
      <bbNG:breadcrumb title="${bundle['page.content.create.title']}" />
    </bbNG:breadcrumbBar>
    <bbNG:pageTitleBar iconUrl="../images/lti.gif" title="${bundle['page.content.create.title']}" />
  </bbNG:pageHeader>
  <bbNG:jsFile href="js/ajax.js" />
  <bbNG:jsBlock>
<%
    if (allowLocal && outcomesEnabled) {
%>
<script language="javascript" type="text/javascript">
var lti_checking = false;
var lti_url = '';
var lti_domain = '';
var lti_step_el;
function checkDomain(el) {
  var ok = true;
  if (lti_url != el.value) {
    lti_url = el.value;
    var data = {"url":lti_url};
    lti_checking = true;
    osc_basiclti.checkUrl('check_domain.jsp', 'application/json', JSON.stringify(data), onCallback);
    if (lti_step_el.style.display != 'block') {
      ok = false;
    }
  }
  return ok;
}
function onCallback(response) {
  eval('var result=' + response);
  if (result.createColumn) {
    var domain = result.domain;
    if (domain != lti_domain) {
      var el = document.getElementById('<%=Constants.TOOL_EXT_OUTCOMES_FORMAT%>');
      el.selectedIndex = result.format;
      el = document.getElementById('<%=Constants.TOOL_EXT_OUTCOMES_POINTS%>');
      el.value = result.points;
      el = document.getElementById('<%=Constants.TOOL_EXT_OUTCOMES_SCORABLE%>_y');
      el.checked = result.scorable;
      el = document.getElementById('<%=Constants.TOOL_EXT_OUTCOMES_SCORABLE%>_n');
      el.checked = !result.scorable;
      el = document.getElementById('<%=Constants.TOOL_EXT_OUTCOMES_VISIBLE%>_y');
      el.checked = result.visible;
      el = document.getElementById('<%=Constants.TOOL_EXT_OUTCOMES_VISIBLE%>_n');
      el.checked = !result.visible;
      lti_domain = result.domain;
    }
    if (lti_step_el.style.display != 'block') {
      lti_step_el.style.display = 'block';
    }
  } else if (lti_step_el.style.display != 'none') {
    lti_step_el.style.display = 'none';
  }
  lti_checking = false;
}
function doOnLoad() {
  lti_step_el = document.getElementById('step3');
  lti_step_el.style.display = 'none';
  checkDomain(document.getElementById('<%=Constants.TOOL_URL%>'));
}
function doValidateForm() {
  var ok = validateForm();
  if (ok) {
    ok = !lti_checking;
  }
  return ok;
}
<%
    } else {
%>
function doOnLoad() {
}
function doValidateForm() {
  return validateForm();
}
<%
    }
%>
</script>
  </bbNG:jsBlock>
  <bbNG:form action="create.jsp?course_id=${courseId}&content_id=${contentId}" method="post" onsubmit="return doValidateForm();">
  <bbNG:dataCollection markUnsavedChanges="true" showSubmitButtons="true">
    <bbNG:stepGroup active="<%=nameTab%>" title="${bundle['page.content.create.tab.byname']}">
      <bbNG:step hideNumber="false" title="${bundle['page.content.create.step1.title']}" instructions="${bundle['page.content.create.step1.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.content.create.step1.tools.label']}">
          <bbNG:selectElement name="<%=Constants.TOOL_ID%>" helpText="${bundle['page.content.create.step1.tools.instructions']}">
            <bbNG:selectOptionElement value="" optionLabel="Select by name..." />
<%
    for (Iterator<Tool> iter=toolList.getList().iterator(); iter.hasNext();) {
      Tool atool = iter.next();
      String atoolId = atool.getId();
      String atoolName = atool.getName();
      boolean selected = toolId.equals(atoolId);
%>
            <bbNG:selectOptionElement isSelected="<%=selected%>" value="<%=atoolId%>" optionLabel="<%=atoolName%>" />
<%
    }
%>
          </bbNG:selectElement>
        </bbNG:dataElement>
      </bbNG:step>
    </bbNG:stepGroup>
    <bbNG:stepGroup active="<%=urlTab%>" title="${bundle['page.content.create.tab.byurl']}">
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.step1.title']}" instructions="${bundle['page.system.tool.step1.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step1.name.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_NAME%>" value="<%=toolName%>" size="50" helpText="${bundle['page.system.tool.step1.name.instructions']}" />
        </bbNG:dataElement>
      </bbNG:step>
      <bbNG:step hideNumber="false" title="${bundle['page.content.create.byurl.step2.title']}" instructions="${bundle['page.content.create.byurl.step2.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.content.create.byurl.step2.url.label']}">
          <bbNG:textElement type="string" name="<%=Constants.TOOL_URL%>" id="<%=Constants.TOOL_URL%>" value="<%=toolUrl%>" size="80" helpText="${bundle['page.content.create.byurl.step2.url.instructions']}" onchange="return checkDomain(this);" />
        </bbNG:dataElement>
      </bbNG:step>
<%
    if (outcomesEnabled) {
%>
      <bbNG:step hideNumber="false" title="${bundle['page.content.create.byurl.step3.title']}" instructions="${bundle['page.content.create.byurl.step3.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_format.label']}">
          <bbNG:selectElement name="<%=Constants.TOOL_EXT_OUTCOMES_FORMAT%>" id="<%=Constants.TOOL_EXT_OUTCOMES_FORMAT%>" helpText="${bundle['page.system.tool.step3.outcomes_format.instructions']}">
            <bbNG:selectOptionElement isSelected="${outcomes_formatP}" value="<%=Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE%>" optionLabel="${bundle['page.system.tool.step3.outcomes_format.percentage']}" />
            <bbNG:selectOptionElement isSelected="${outcomes_formatS}" value="<%=Constants.EXT_OUTCOMES_COLUMN_SCORE%>" optionLabel="${bundle['page.system.tool.step3.outcomes_format.score']}" />
          </bbNG:selectElement>
        </bbNG:dataElement>
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_points.label']}">
          <bbNG:textElement type="unsigned_integer" name="<%=Constants.TOOL_EXT_OUTCOMES_POINTS%>" id="<%=Constants.TOOL_EXT_OUTCOMES_POINTS%>" value="<%=points%>" title="${bundle['page.system.tool.step3.outcomes_points.label']}" size="5" maxLength="3" helpText="${bundle['page.system.tool.step3.outcomes_points.instructions']}" />
        </bbNG:dataElement>
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_scorable.label']}">
          <bbNG:radioElement optionLabel="${bundle['option.true']}" isSelected="<%=scorable%>" name="<%=Constants.TOOL_EXT_OUTCOMES_SCORABLE%>" id="${scorable}_y" value="true" />
          <bbNG:radioElement optionLabel="${bundle['option.false']}" isSelected="<%=!scorable%>" name="<%=Constants.TOOL_EXT_OUTCOMES_SCORABLE%>" id="${scorable}_n" value="false" />
          <bbNG:elementInstructions text="${bundle['page.system.tool.step3.outcomes_scorable.instructions']}" />
        </bbNG:dataElement>
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.step3.outcomes_visible.label']}">
          <bbNG:radioElement optionLabel="${bundle['option.true']}" isSelected="<%=visible%>" name="<%=Constants.TOOL_EXT_OUTCOMES_VISIBLE%>" id="${visible}_y" value="true" />
          <bbNG:radioElement optionLabel="${bundle['option.false']}" isSelected="<%=!visible%>" name="<%=Constants.TOOL_EXT_OUTCOMES_VISIBLE%>" id="${visible}_n" value="false" />
          <bbNG:elementInstructions text="${bundle['page.system.tool.step3.outcomes_visible.instructions']}" />
        </bbNG:dataElement>
      </bbNG:step>
<%
    }
%>
    </bbNG:stepGroup>
    <bbNG:stepGroup active="<%=xmlTab%>" title="${bundle['page.content.create.tab.byxml']}">
      <bbNG:step hideNumber="false" title="${bundle['page.system.tool.xml.title']}" instructions="${bundle['page.system.tool.xml.instructions']}">
        <bbNG:dataElement isRequired="true" label="${bundle['page.system.tool.xml.label']}">
          <textarea name="<%=Constants.TOOL_XML%>" cols="80" rows="20"><%=xml%></textarea>
        </bbNG:dataElement>
      </bbNG:step>
    </bbNG:stepGroup>
    <bbNG:stepSubmit hideNumber="false" showCancelButton="true" cancelUrl="<%=cancelUrl%>" />
  </bbNG:dataCollection>
  </bbNG:form>
</bbNG:learningSystemPage>
