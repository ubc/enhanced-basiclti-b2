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
      1.2.0 17-Sep-11  Added support for outcomes, memberships and setting extension services
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Significant update to user interface
                       Added option to allow instructors to create their own links
      2.0.1 20-May-12  Fixed page doctype
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12  Added cache settings
                       Added option to enable VTBE mashup
      2.3.0  5-Nov-12  Added option for passing institution roles
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.Map,
                java.util.HashMap,
                blackboard.platform.user.MyPlacesUtil,
                org.oscelot.blackboard.basiclti.Constants,
                org.oscelot.blackboard.basiclti.Utils,
                org.oscelot.blackboard.utils.StringCache,
                org.oscelot.blackboard.utils.StringCacheFile,
                com.spvsoftwareproducts.blackboard.utils.B2Context"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:genericPage title="${bundle['page.system.settings.title']}" entitlement="system.admin.VIEW">
<%
  B2Context b2Context = new B2Context(request);
  String query = Utils.getQuery(request);
  String cancelUrl = "tools.jsp?" + query;
//  String version = B2Context.getVersionNumber("");
//  boolean isv91 = version.compareTo("9.1.") >= 0;
  boolean isv91 = B2Context.getIsVersion(9, 1, 0);
  boolean enabledMashup = b2Context.getSetting(Constants.MASHUP_PARAMETER, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);

  if (request.getMethod().equalsIgnoreCase("POST")) {
    b2Context.setSetting(Constants.CONSUMER_NAME_PARAMETER, request.getParameter(Constants.CONSUMER_NAME_PARAMETER));
    b2Context.setSetting(Constants.CONSUMER_DESCRIPTION_PARAMETER, request.getParameter(Constants.CONSUMER_DESCRIPTION_PARAMETER));
    b2Context.setSetting(Constants.CONSUMER_EMAIL_PARAMETER, request.getParameter(Constants.CONSUMER_EMAIL_PARAMETER));
    b2Context.setSetting(Constants.TOOL_EXT_OUTCOMES, b2Context.getRequestParameter(Constants.TOOL_EXT_OUTCOMES, Constants.DATA_FALSE));
    b2Context.setSetting(Constants.TOOL_EXT_MEMBERSHIPS, b2Context.getRequestParameter(Constants.TOOL_EXT_MEMBERSHIPS, Constants.DATA_FALSE));
    b2Context.setSetting(Constants.TOOL_EXT_SETTING, b2Context.getRequestParameter(Constants.TOOL_EXT_SETTING, Constants.DATA_FALSE));
    b2Context.setSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_AVATAR, b2Context.getRequestParameter(Constants.TOOL_AVATAR, Constants.DATA_FALSE));
    b2Context.setSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_DELEGATE, b2Context.getRequestParameter(Constants.TOOL_DELEGATE, Constants.DATA_FALSE));
    if (isv91) {
      b2Context.setSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_COURSE_ROLES, b2Context.getRequestParameter(Constants.TOOL_COURSE_ROLES, Constants.DATA_FALSE));
      boolean setMashup = b2Context.getRequestParameter(Constants.MASHUP_PARAMETER, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
      if (enabledMashup ^ setMashup) {
        if (Utils.checkVTBEMashup(b2Context, setMashup)) {
          if (setMashup) {
            b2Context.setSetting(Constants.MASHUP_PARAMETER, Constants.DATA_TRUE);
          } else {
            b2Context.setSetting(Constants.MASHUP_PARAMETER, Constants.DATA_FALSE);
          }
        }
      }
    }
    if (B2Context.getIsVersion(9, 1, 8)) {
      b2Context.setSetting(Constants.AVAILABILITY_PARAMETER, b2Context.getRequestParameter(Constants.AVAILABILITY_PARAMETER, Constants.AVAILABILITY_DEFAULT_OFF));
    }
    b2Context.setSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_INSTITUTION_ROLES, b2Context.getRequestParameter(Constants.TOOL_INSTITUTION_ROLES, Constants.DATA_FALSE));
    b2Context.setSetting(Constants.CACHE_AGE_PARAMETER, b2Context.getRequestParameter(Constants.CACHE_AGE_PARAMETER, Constants.CACHE_OPTION));
    b2Context.setSetting(Constants.CACHE_CAPACITY_PARAMETER, b2Context.getRequestParameter(Constants.CACHE_CAPACITY_PARAMETER, Constants.CACHE_OPTION));
    if (!b2Context.getSetting(Constants.CACHE_AGE_PARAMETER).matches("\\d+")) {
      b2Context.setReceipt(b2Context.getResourceString("page.system.settings.step4.cacheage.error"), false);
    } else if (!b2Context.getSetting(Constants.CACHE_CAPACITY_PARAMETER).matches("\\d+")) {
      b2Context.setReceipt(b2Context.getResourceString("page.system.settings.step4.cachecapacity.error"), false);
    } else {
      StringCache xmlCache = StringCacheFile.getInstance(
         b2Context.getSetting(Constants.CACHE_AGE_PARAMETER),
         b2Context.getSetting(Constants.CACHE_CAPACITY_PARAMETER));
      b2Context.setSetting(Constants.CACHE_AGE_PARAMETER,
         String.valueOf(xmlCache.getAge()));
      b2Context.setSetting(Constants.CACHE_CAPACITY_PARAMETER,
         String.valueOf(xmlCache.getCapacity()));
      b2Context.persistSettings();
      cancelUrl = b2Context.setReceiptOptions(cancelUrl,
         b2Context.getResourceString("page.receipt.success"), null);
      response.sendRedirect(cancelUrl);
    }
  }

  Map<String,String> params = new HashMap<String,String>();
  params.put(Constants.TOOL_EXT_OUTCOMES, b2Context.getSetting(Constants.TOOL_EXT_OUTCOMES, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_MEMBERSHIPS, b2Context.getSetting(Constants.TOOL_EXT_MEMBERSHIPS, Constants.DATA_FALSE));
  params.put(Constants.TOOL_EXT_SETTING, b2Context.getSetting(Constants.TOOL_EXT_SETTING, Constants.DATA_FALSE));
  if (MyPlacesUtil.avatarsEnabled()) {
    pageContext.setAttribute("disableAvatar", "false");
    params.put(Constants.TOOL_AVATAR, b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_AVATAR, Constants.DATA_FALSE));
  } else {
    pageContext.setAttribute("disableAvatar", "true");
    params.put(Constants.TOOL_AVATAR, Constants.DATA_FALSE);
  }
  params.put(Constants.TOOL_DELEGATE, b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_DELEGATE, Constants.DATA_FALSE));
  if (isv91) {
    params.put(Constants.TOOL_COURSE_ROLES, b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_COURSE_ROLES, Constants.DATA_FALSE));
    params.put(Constants.MASHUP_PARAMETER, b2Context.getSetting(Constants.MASHUP_PARAMETER, Constants.DATA_FALSE));
  }
  if (B2Context.getIsVersion(9, 1, 8)) {
    params.put(Constants.AVAILABILITY_PARAMETER, b2Context.getSetting(Constants.AVAILABILITY_PARAMETER, Constants.AVAILABILITY_DEFAULT_OFF));
    params.put(Constants.AVAILABILITY_PARAMETER + params.get(Constants.AVAILABILITY_PARAMETER), Constants.DATA_TRUE);
    params.put(Constants.AVAILABILITY_PARAMETER + Constants.AVAILABILITY_DEFAULT_ON + "label",
       b2Context.getResourceString("page.system.settings.step3.availability." + Constants.AVAILABILITY_DEFAULT_ON));
    params.put(Constants.AVAILABILITY_PARAMETER + Constants.AVAILABILITY_DEFAULT_OFF + "label",
       b2Context.getResourceString("page.system.settings.step3.availability." + Constants.AVAILABILITY_DEFAULT_OFF));
    params.put(Constants.AVAILABILITY_PARAMETER + Constants.AVAILABILITY_ALWAYS_ON + "label",
       b2Context.getResourceString("page.system.settings.step3.availability." + Constants.AVAILABILITY_ALWAYS_ON));
    params.put(Constants.AVAILABILITY_PARAMETER + Constants.AVAILABILITY_ALWAYS_OFF + "label",
       b2Context.getResourceString("page.system.settings.step3.availability." + Constants.AVAILABILITY_ALWAYS_OFF));
  }
  params.put(Constants.TOOL_INSTITUTION_ROLES, b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_INSTITUTION_ROLES, Constants.DATA_FALSE));
  pageContext.setAttribute("query", query);
  pageContext.setAttribute("params", params);
  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("fields", b2Context.getSettings());
  pageContext.setAttribute("cancelUrl", cancelUrl);
%>
  <bbNG:pageHeader instructions="${bundle['page.system.settings.instructions']}">
    <bbNG:breadcrumbBar environment="SYS_ADMIN_PANEL" navItem="admin_plugin_manage">
      <bbNG:breadcrumb href="tools.jsp?${query}" title="${bundle['plugin.name']}" />
      <bbNG:breadcrumb title="${bundle['page.system.settings.title']}" />
    </bbNG:breadcrumbBar>
    <bbNG:pageTitleBar iconUrl="../images/lti.gif" showTitleBar="true" title="${bundle['page.system.settings.title']}"/>
  </bbNG:pageHeader>
  <bbNG:form action="settings.jsp?${query}" id="configForm" name="configForm" method="post" onsubmit="return validateForm();">
  <bbNG:dataCollection markUnsavedChanges="true" showSubmitButtons="true">
    <bbNG:step hideNumber="false" id="stepOne" title="${bundle['page.system.settings.step1.title']}" instructions="${bundle['page.system.settings.step1.instructions']}">
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step1.name.label']}">
        <bbNG:textElement name="<%=Constants.CONSUMER_NAME_PARAMETER%>" value="<%=b2Context.getSetting(Constants.CONSUMER_NAME_PARAMETER)%>" helpText="${bundle['page.system.settings.step1.name.instructions']}" size="20" minLength="1" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step1.description.label']}">
        <bbNG:textElement name="<%=Constants.CONSUMER_DESCRIPTION_PARAMETER%>" value="<%=b2Context.getSetting(Constants.CONSUMER_DESCRIPTION_PARAMETER)%>" helpText="${bundle['page.system.settings.step1.description.instructions']}" size="50" minLength="1" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="false" label="${bundle['page.system.settings.step1.email.label']}">
        <bbNG:textElement name="<%=Constants.CONSUMER_EMAIL_PARAMETER%>" value="<%=b2Context.getSetting(Constants.CONSUMER_EMAIL_PARAMETER)%>" helpText="${bundle['page.system.settings.step1.email.instructions']}" size="50" />
      </bbNG:dataElement>
    </bbNG:step>
    <bbNG:step hideNumber="false" id="stepTwo" title="${bundle['page.system.settings.step2.title']}" instructions="${bundle['page.system.settings.step2.instructions']}">
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step2.outcomes.label']}">
        <bbNG:checkboxElement isSelected="${params.ext_outcomes}" name="<%=Constants.TOOL_EXT_OUTCOMES%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step2.outcomes.instructions']}" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step2.memberships.label']}">
        <bbNG:checkboxElement isSelected="${params.ext_memberships}" name="<%=Constants.TOOL_EXT_MEMBERSHIPS%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step2.memberships.instructions']}" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step2.setting.label']}">
        <bbNG:checkboxElement isSelected="${params.ext_setting}" name="<%=Constants.TOOL_EXT_SETTING%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step2.setting.instructions']}" />
      </bbNG:dataElement>
    </bbNG:step>
    <bbNG:step hideNumber="false" id="stepThree" title="${bundle['page.system.settings.step3.title']}" instructions="${bundle['page.system.settings.step3.instructions']}">
<%
  if (isv91) {
%>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step3.courseroles.label']}">
        <bbNG:checkboxElement isSelected="${params.courseroles}" name="<%=Constants.TOOL_COURSE_ROLES%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step3.courseroles.instructions']}" />
      </bbNG:dataElement>
<%
  }
%>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step3.institutionroles.label']}">
        <bbNG:checkboxElement isSelected="${params.institutionroles}" name="<%=Constants.TOOL_INSTITUTION_ROLES%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step3.institutionroles.instructions']}" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step3.avatar.label']}">
        <bbNG:checkboxElement isSelected="${params.avatar}" name="<%=Constants.TOOL_AVATAR%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step3.avatar.instructions']}" isDisabled="${disableAvatar}" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step3.delegate.label']}">
        <bbNG:checkboxElement isSelected="${params.delegate}" name="<%=Constants.TOOL_DELEGATE%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step3.delegate.instructions']}" />
      </bbNG:dataElement>
<%
  if (isv91) {
%>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step3.mashup.label']}">
        <bbNG:checkboxElement isSelected="${params.mashup}" name="<%=Constants.MASHUP_PARAMETER%>" value="<%=Constants.DATA_TRUE%>" helpText="${bundle['page.system.settings.step3.mashup.instructions']}" />
      </bbNG:dataElement>
<%
  }
  if (B2Context.getIsVersion(9, 1, 8)) {
%>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step3.availability.label']}">
        <bbNG:selectElement name="<%=Constants.AVAILABILITY_PARAMETER%>" helpText="${bundle['page.system.settings.step3.availability.instructions']}">
          <bbNG:selectOptionElement isSelected="${params.availabilityDefaultOn}" value="<%=Constants.AVAILABILITY_DEFAULT_ON%>" optionLabel="${params.availabilityDefaultOnlabel}" />
          <bbNG:selectOptionElement isSelected="${params.availabilityDefaultOff}" value="<%=Constants.AVAILABILITY_DEFAULT_OFF%>" optionLabel="${params.availabilityDefaultOfflabel}" />
          <bbNG:selectOptionElement isSelected="${params.availabilityLockedOn}" value="<%=Constants.AVAILABILITY_ALWAYS_ON%>" optionLabel="${params.availabilityLockedOnlabel}" />
          <bbNG:selectOptionElement isSelected="${params.availabilityLockedOff}" value="<%=Constants.AVAILABILITY_ALWAYS_OFF%>" optionLabel="${params.availabilityLockedOfflabel}" />
        </bbNG:selectElement>
      </bbNG:dataElement>
<%
  }
%>
    </bbNG:step>
    <bbNG:step hideNumber="false" id="stepFour" title="${bundle['page.system.settings.step4.title']}" instructions="${bundle['page.system.settings.step4.instructions']}">
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step4.cacheage.label']}">
        <bbNG:textElement name="<%=Constants.CACHE_AGE_PARAMETER%>" value="<%=b2Context.getSetting(Constants.CACHE_AGE_PARAMETER, Constants.CACHE_OPTION)%>" helpText="${bundle['page.system.settings.step4.cacheage.instructions']}" size="10" minLength="1" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="true" label="${bundle['page.system.settings.step4.cachecapacity.label']}">
        <bbNG:textElement name="<%=Constants.CACHE_CAPACITY_PARAMETER%>" value="<%=b2Context.getSetting(Constants.CACHE_CAPACITY_PARAMETER, Constants.CACHE_OPTION)%>" helpText="${bundle['page.system.settings.step4.cachecapacity.instructions']}" size="10" minLength="1" />
      </bbNG:dataElement>
    </bbNG:step>
    <bbNG:stepSubmit hideNumber="false" showCancelButton="true" cancelUrl="${cancelUrl}" />
  </bbNG:dataCollection>
  </bbNG:form>
</bbNG:genericPage>
