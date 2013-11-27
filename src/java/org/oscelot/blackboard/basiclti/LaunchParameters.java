/*
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
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12  Switched to using BbSession for module custom parameters
      2.3.2  3-Apr-13  Fixed issue with splitting list of custom parameters
*/
package org.oscelot.blackboard.basiclti;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Iterator;

import net.oauth.OAuthMessage;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;

import org.apache.commons.httpclient.NameValuePair;

import blackboard.data.user.User;
import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.content.Content;
import blackboard.data.role.PortalRole;
import blackboard.portal.data.Module;
import blackboard.persist.Id;
import blackboard.persist.content.ContentDbLoader;
import blackboard.platform.user.MyPlacesUtil;
import blackboard.platform.persistence.PersistenceServiceFactory;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.PersistenceException;
import blackboard.platform.context.Context;
import blackboard.platform.session.BbSessionManagerServiceFactory;
import blackboard.platform.session.BbSession;

import com.spvsoftwareproducts.blackboard.utils.B2Context;
import java.util.Collections;

import ca.ubc.ctlt.encryption.*;


public class LaunchParameters {

  private List<Map.Entry<String, String>> params = null;

  public LaunchParameters(B2Context b2Context, String toolId, String launchUrl, Module module) {

    Context context = b2Context.getContext();

    Tool tool = new Tool(b2Context, toolId);
    String toolPrefix = Constants.TOOL_PARAMETER_PREFIX + ".";
    String settingPrefix = "";
    if (!tool.getByUrl()) {
      toolPrefix += toolId + ".";
      settingPrefix = toolPrefix;
    } else {
      String domainId = "";
      Tool domain = tool.getDomain();
      if (domain != null) {
        domainId = domain.getId();
      }
      settingPrefix = Constants.DOMAIN_PARAMETER_PREFIX + "." + domainId + ".";
    }

    Properties props = new Properties();
//    Properties scramProps = new Properties();
    Properties ecryptProps = new Properties();

      props.setProperty("lti_message_type", Constants.LTI_MESSAGE_TYPE);
    props.setProperty("lti_version", Constants.LTI_VERSION);

// User parameters
    User user = context.getUser();

    String userId = null;
    boolean isStudent = false;
    if (tool.getDoSendUserId()) {
      String userIdType = tool.getUserIdType();
      if (userIdType.equals(Constants.DATA_USERNAME)) {
        userId = user.getUserName();
      } else if (userIdType.equals(Constants.DATA_PRIMARYKEY)) {
        userId = user.getId().toExternalString();
      } else if (userIdType.equals(Constants.DATA_STUDENTID)) {
        userId = user.getStudentId();
      } else {
        userId = user.getBatchUid();
      }
      if (userId !=  null) {
        props.setProperty("user_id", userId);
      }
    }
    try {
      if (MyPlacesUtil.avatarsEnabled() && Utils.displayAvatar(user.getId()) && tool.getDoSendAvatar()) {
        String image = MyPlacesUtil.getAvatarImage(user.getId());
        if (image != null) {
          props.setProperty("user_image", b2Context.getServerUrl() + image);
        }
      }
    } catch (Exception e) {
    }

//      System.out.println("User ID");

    if (tool.getDoSendUsername()) {
      props.setProperty("lis_person_name_given", user.getGivenName());
      props.setProperty("lis_person_name_family", user.getFamilyName());
      String fullname = user.getGivenName();
      if ((user.getMiddleName() != null) && (user.getMiddleName().length() > 0)) {
        fullname += " " + user.getMiddleName();
      }
      fullname += " " + user.getFamilyName();
      props.setProperty("lis_person_name_full", fullname);
    }
    if (tool.getDoSendEmail()) {
      props.setProperty("lis_person_contact_email_primary", user.getEmailAddress());
    }
    if (tool.getDoSendUserSourcedid()) {
      props.setProperty("lis_person_sourcedid", user.getBatchUid());
    }

//      System.out.println("Username, Email");
// Context
    String customParameters = "";
    String domain = b2Context.getRequest().getRequestURL().toString();
    int pos = domain.indexOf("/", 8);
    domain = domain.substring(0, pos);
    String returnUrl = domain + b2Context.getPath() + "return.jsp";
    StringBuilder query = new StringBuilder();
// Course parameters
    Course course = context.getCourse();
    String courseId = b2Context.getRequestParameter("course_id", "");
    String contentId = b2Context.getRequestParameter("content_id", "");
    if (course != null) {
      String resourceId;
      query.append(Constants.TOOL_ID).append("=").append(toolId).append("&");
      query.append("course_id=").append(courseId).append("&");
      props.setProperty("context_type", "CourseSection");
      String contextIdType = tool.getContextIdType();
      if (contextIdType.equals(Constants.DATA_PRIMARYKEY)) {
        resourceId = course.getId().toExternalString();
      } else if (contextIdType.equals(Constants.DATA_COURSEID)) {
        resourceId = course.getCourseId();
      } else {
        resourceId = course.getBatchUid();
      }
      if (tool.getDoSendContextId() && (resourceId !=  null)) {
        props.setProperty("context_id", resourceId);
      }
      if (tool.getDoSendContextTitle()) {
        props.setProperty("context_title", Utils.stripTags(course.getTitle()));
        props.setProperty("context_label", course.getCourseId());
      }
      String title = tool.getName();
      if (contentId.length() > 0) {
        query.append("content_id=").append(contentId).append("&");
        resourceId += contentId;
        BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
        try {
          Id id = bbPm.generateId(Content.DATA_TYPE, contentId);
          ContentDbLoader courseDocumentLoader = (ContentDbLoader)bbPm.getLoader(ContentDbLoader.TYPE);
          Content content = courseDocumentLoader.loadById(id);
          title = content.getTitle();
          props.setProperty("resource_link_description", Utils.stripTags(content.getBody().getText()));
        } catch (PersistenceException e) {
        }
      }
      props.setProperty("resource_link_id", resourceId);
      props.setProperty("resource_link_title", Utils.stripTags(title));
      if (tool.getDoSendContextSourcedid()) {
        props.setProperty("lis_course_offering_sourcedid", course.getBatchUid());
        props.setProperty("lis_course_section_sourcedid", course.getBatchUid());
      }


//        System.out.println("context and resource id");

      boolean systemRolesOnly = !b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_COURSE_ROLES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
      CourseMembership.Role role = Utils.getRole(context.getCourseMembership().getRole(), systemRolesOnly);
      isStudent = role.equals(CourseMembership.Role.STUDENT);

      if (tool.getDoSendRoles()) {
        String roles = Utils.getRoles(tool.getRole(role.getIdentifier()),
           tool.getSendAdministrator().equals(Constants.DATA_TRUE) && user.getSystemRole().equals(User.SystemRole.SYSTEM_ADMIN));
        props.setProperty("roles", roles);
      }
    }
    String list = b2Context.getRequestParameter(Constants.PAGE_PARAMETER_NAME, "");
    if (list.length() > 0) {
      query.append(Utils.getQuery(b2Context.getRequest())).append("&");
    }
    props.setProperty("launch_presentation_return_url", returnUrl + "?" + query);
    if (module != null) {
      props.setProperty("context_type", "Group");
      props.setProperty("launch_presentation_return_url", returnUrl + "?" + Constants.TOOL_MODULE + "=" + module.getId().toExternalString() + "&" +
         Constants.TAB_PARAMETER_NAME + "=" + b2Context.getRequestParameter(Constants.TAB_PARAMETER_NAME, ""));
      props.setProperty("resource_link_id", module.getId().toExternalString());
      props.setProperty("resource_link_title", Utils.stripTags(module.getTitle()));
      props.setProperty("resource_link_description", Utils.stripTags(module.getDescriptionFormatted().getText()));
      if (tool.getDoSendRoles()) {
        boolean systemIRolesOnly = !b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_INSTITUTION_ROLES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
        List<PortalRole> iRoles = Utils.getInstitutionRoles(systemIRolesOnly, user);
        boolean sendAdminRole = b2Context.getSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ADMINISTRATOR, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
        String roles = Utils.getIRoles(b2Context, iRoles, sendAdminRole && user.getSystemRole().equals(User.SystemRole.SYSTEM_ADMIN));
        props.setProperty("roles", roles);
      }
      try {
        BbSession bbSession = BbSessionManagerServiceFactory.getInstance().getSession(b2Context.getRequest());
        String name = b2Context.getVendorId() + "-" + b2Context.getHandle() + "-" + module.getId().toExternalString() +
           "_" + b2Context.getRequestParameter("n", "");
        String custom = bbSession.getGlobalKey(name);
        if (custom != null) {
          customParameters = custom;
        }
      } catch (PersistenceException e) {
      }
    }
    String extensionUrl = domain + b2Context.getPath() + "extension";
    String serviceUrl = domain + b2Context.getPath() + "service";
    List<String> serviceData = new ArrayList<String>();
    serviceData.add(courseId);
    serviceData.add(contentId);
    serviceData.add(tool.getId());
    String time = Integer.toString((int)(System.currentTimeMillis() / 1000));
    String hashId = Utils.getServiceId(serviceData, time, tool.getSendUUID());
    if ((course != null) && tool.getSendUserId().equals(Constants.DATA_MANDATORY)) {
      if (tool.getDoSendOutcomesService()) {
        props.setProperty("ext_ims_lis_basic_outcome_url", extensionUrl);
        props.setProperty("ext_ims_lis_resultvalue_sourcedids", "decimal,percentage,ratio,passfail,letteraf,letterafplus,freetext");
        props.setProperty("lis_outcome_service_url", serviceUrl);
        if (isStudent) {
          String userHashId = Utils.getServiceId(serviceData, userId, tool.getSendUUID());
          props.setProperty("lis_result_sourcedid", userHashId);
        }
      }
      if (tool.getDoSendMembershipsService()) {
        props.setProperty("ext_ims_lis_memberships_id", hashId);
        props.setProperty("ext_ims_lis_memberships_url", extensionUrl);
      }
    }
    if (tool.getDoSendSettingService()) {
      props.setProperty("ext_ims_lti_tool_setting", b2Context.getSetting(false, true, toolPrefix + Constants.TOOL_EXT_SETTING_VALUE, ""));
      props.setProperty("ext_ims_lti_tool_setting_id", hashId);
      props.setProperty("ext_ims_lti_tool_setting_url", extensionUrl);
    }

      System.out.println("memberships, LTI");

// Consumer
    String css = tool.getLaunchCSS();
    if (css.length() > 0) {
      props.setProperty("ext_launch_presentation_css_url", css);
      props.setProperty("launch_presentation_css_url", css);
    }

    String[] version = B2Context.getVersionNumber("?.?.?").split("\\.");
    props.setProperty("ext_lms", Constants.LTI_LMS + "-" + version[0] + "." + version[1] + "." + version[2]);
    props.setProperty("tool_consumer_info_product_family_code", Constants.LTI_LMS);
    props.setProperty("tool_consumer_info_version", version[0] + "." + version[1] + "." + version[2]);

    String resource = tool.getResourceUrl();
    if (resource.length() > 0) {
      props.setProperty("ext_resource_link_content", resource);
      resource = tool.getResourceSignature();
      if (resource.length() > 0) {
        props.setProperty("ext_resource_link_content_signature", resource);
      }
    }

    String locale = user.getLocale();
    if ((locale == null) || (locale.length() <= 0)) {
      locale = (String)context.getAttribute(Constants.LOCALE_ATTRIBUTE);
    }
    props.setProperty("launch_presentation_locale", locale);

    props.setProperty("tool_consumer_instance_guid", tool.getLaunchGUID());
    props.setProperty("tool_consumer_instance_name", b2Context.getSetting(Constants.CONSUMER_NAME_PARAMETER, ""));
    props.setProperty("tool_consumer_instance_description", b2Context.getSetting(Constants.CONSUMER_DESCRIPTION_PARAMETER, ""));
    String email = b2Context.getSetting(Constants.CONSUMER_EMAIL_PARAMETER, "");
    if (email.length() > 0) {
      props.setProperty("tool_consumer_instance_contact_email", email);
    }

    props.setProperty("tool_consumer_instance_url", b2Context.getServerUrl());

    String target = "frame";
    if (tool.getOpenIn().equals(Constants.DATA_WINDOW)) {
      target = "window";
    } else if (tool.getOpenIn().equals(Constants.DATA_IFRAME)) {
      target = "iframe";
    }
    props.setProperty("launch_presentation_document_target", target);

    customParameters += b2Context.getSetting(false, true, toolPrefix + Constants.TOOL_CUSTOM, "");
    if (tool.getIsSystemTool() || tool.getByUrl()) {
      customParameters += "\r\n" + b2Context.getSetting(settingPrefix + Constants.TOOL_CUSTOM, "");
    } else {
      customParameters += "\r\n" + tool.getCustomParameters();
    }
    customParameters = customParameters.replaceAll("\\r\\n", "\n");
    String[] items = customParameters.split("\\n");
    for (int i=0; i<items.length; i++) {
      String[] item = items[i].split("=", 2);
      if (item.length > 0) {
        String paramName = item[0];
        if (paramName.length()>0) {
          paramName = paramName.toLowerCase();
          paramName = Constants.CUSTOM_NAME_PREFIX + paramName.replaceAll("[^a-z0-9]", "_");
          if (item.length > 1) {
            props.setProperty(paramName, Utils.parseParameter(course, user, item[1]));
          } else {
            props.setProperty(paramName, "");
          }
        }
      }
    }
    props.setProperty("oauth_callback", Constants.OAUTH_CALLBACK);

      System.out.println("oauth callback");


      //injected code here to scramble oAuth props//
      
//      scramProps = (Properties) props.clone();
      EncryptManager encrypt = new EncryptManager();
      ecryptProps =  encrypt.encrypt(props);

      //end injection

    if (launchUrl == null) {
      launchUrl = tool.getLaunchUrl();
    }
    OAuthMessage oAuthMessage = new OAuthMessage("POST", launchUrl, ecryptProps.entrySet());
      System.out.println("after change");
    String consumerKey = tool.getLaunchGUID();
    String secret = tool.getLaunchSecret();
    OAuthConsumer oAuthConsumer = new OAuthConsumer(Constants.OAUTH_CALLBACK, consumerKey, secret, null);
    OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
    try {
      oAuthMessage.addRequiredParameters(oAuthAccessor);
      this.params = oAuthMessage.getParameters();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

  }

  public List<Entry<String, String>> getParams() {
    return Collections.unmodifiableList(this.params);
  }

  public NameValuePair[] getHTTPParams() {

    NameValuePair[] nvPairs = null;
    if (this.params != null) {
      nvPairs = new NameValuePair[this.params.size()];
      int i = 0;
      for (Iterator<Entry<String,String>> iter = this.params.iterator(); iter.hasNext();) {
        Entry<String,String> entry = iter.next();
        nvPairs[i] = new NameValuePair(entry.getKey(), entry.getValue());
        i++;
      }
    }

    return nvPairs;

  }

}
