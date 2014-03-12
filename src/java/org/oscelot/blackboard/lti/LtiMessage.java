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
      2.3.2 20-Jan-13  Fixed issue with splitting list of custom parameters
      3.0.0 30-Oct-13
*/
package org.oscelot.blackboard.lti;

import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.role.PortalRole;
import blackboard.data.user.User;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.platform.context.Context;
import blackboard.platform.institutionalhierarchy.service.Node;
import blackboard.platform.institutionalhierarchy.service.NodeManagerFactory;
import blackboard.platform.persistence.PersistenceServiceFactory;
import blackboard.platform.user.MyPlacesUtil;
import blackboard.portal.data.Module;
import ca.ubc.ctlt.encryption.Encryption;
import ca.ubc.ctlt.encryption.UserWrapper;
import com.spvsoftwareproducts.blackboard.utils.B2Context;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import org.apache.commons.httpclient.NameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LtiMessage {

  protected User user = null;
  protected Course course = null;
  public Tool tool = null;
  protected String toolPrefix = null;
  protected String settingPrefix = null;
  protected Properties props = null;
  private List<Map.Entry<String, String>> params = null;

  public LtiMessage(B2Context b2Context, String toolId, Module module) {

    Context context = b2Context.getContext();

    this.tool = new Tool(b2Context, toolId);
    this.toolPrefix = Constants.TOOL_PARAMETER_PREFIX + ".";
    this.settingPrefix = "";
    if (!tool.getByUrl()) {
      this.toolPrefix += toolId + ".";
      this.settingPrefix = this.toolPrefix;
    } else {
      String domainId = "";
      Tool domain = tool.getDomain();
      if (domain != null) {
        domainId = domain.getId();
      }
      this.settingPrefix = Constants.DOMAIN_PARAMETER_PREFIX + "." + domainId + ".";
    }

    this.props = new Properties();
    props.setProperty("lti_version", Constants.LTI_VERSION);

// User parameters
    this.user = new UserWrapper(context.getUser(), new Encryption(tool.getEncryptKey()), tool.isEncryptData());
    String userId;
    if (this.tool.getDoSendUserId()) {
      String userIdType = this.tool.getUserIdType();
      if (userIdType.equals(Constants.DATA_USERNAME)) {
        userId = this.user.getUserName();
      } else if (userIdType.equals(Constants.DATA_PRIMARYKEY)) {
        userId = this.user.getId().toExternalString();
      } else if (userIdType.equals(Constants.DATA_STUDENTID)) {
        userId = this.user.getStudentId();
      } else {
        userId = this.user.getBatchUid();
      }
      if (userId !=  null) {
        this.props.setProperty("user_id", userId);
      }
    }
    try {
      if (MyPlacesUtil.avatarsEnabled() && Utils.displayAvatar(user.getId()) && this.tool.getDoSendAvatar()) {
        String image = MyPlacesUtil.getAvatarImage(this.user.getId());
        if (image != null) {
          this.props.setProperty("user_image", b2Context.getServerUrl() + image);
        }
      }
    } catch (Exception e) {
    }

    if (this.tool.getDoSendUsername()) {
      this.props.setProperty("lis_person_name_given", this.user.getGivenName());
      this.props.setProperty("lis_person_name_family", this.user.getFamilyName());
      String fullname = ((UserWrapper)this.user).getFullName();
      this.props.setProperty("lis_person_name_full", fullname);
    }
    if (this.tool.getDoSendEmail()) {
      this.props.setProperty("lis_person_contact_email_primary", this.user.getEmailAddress());
    }
    if (this.tool.getDoSendUserSourcedid()) {
      this.props.setProperty("lis_person_sourcedid", this.user.getBatchUid());
    }

// Course parameters
    if (!b2Context.getIgnoreCourseContext()) {
      this.course = context.getCourse();
    }
    String roles = "";
    boolean sendAdminRole = this.tool.getSendAdministrator().equals(Constants.DATA_TRUE);
    if (this.course != null) {
      String contentId = b2Context.getRequestParameter("content_id", "");
      String resourceId;
      this.props.setProperty("context_type", "CourseSection");
      String contextIdType = this.tool.getContextIdType();
      if (contextIdType.equals(Constants.DATA_PRIMARYKEY)) {
        resourceId = this.course.getId().toExternalString();
      } else if (contextIdType.equals(Constants.DATA_COURSEID)) {
        resourceId = this.course.getCourseId();
      } else {
        resourceId = this.course.getBatchUid();
      }
      if (this.tool.getDoSendContextId() && (resourceId !=  null)) {
        this.props.setProperty("context_id", resourceId);
      }
      if (this.tool.getDoSendContextTitle()) {
        this.props.setProperty("context_title", Utils.stripTags(course.getTitle()));
        this.props.setProperty("context_label", course.getCourseId());
      }
      String title = tool.getName();
      if (contentId.length() > 0) {
        resourceId += contentId;
        BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
        try {
          Id id = bbPm.generateId(Content.DATA_TYPE, contentId);
          ContentDbLoader courseDocumentLoader = (ContentDbLoader)bbPm.getLoader(ContentDbLoader.TYPE);
          Content content = courseDocumentLoader.loadById(id);
          title = content.getTitle();
          this.props.setProperty("resource_link_description", Utils.stripTags(content.getBody().getText()));
        } catch (PersistenceException e) {
        }
      }
      this.props.setProperty("resource_link_id", resourceId);
      this.props.setProperty("resource_link_title", Utils.stripTags(title));
      if (this.tool.getDoSendContextSourcedid()) {
        this.props.setProperty("lis_course_offering_sourcedid", this.course.getBatchUid());
        this.props.setProperty("lis_course_section_sourcedid", this.course.getBatchUid());
      }

      boolean systemRolesOnly = !b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_COURSE_ROLES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
      CourseMembership.Role role = Utils.getRole(context.getCourseMembership().getRole(), systemRolesOnly);

      if (this.tool.getDoSendRoles()) {
        roles = Utils.getCRoles(this.tool.getRole(role.getIdentifier()),
            sendAdminRole && this.user.getSystemRole().equals(User.SystemRole.SYSTEM_ADMIN));
      }
    }
    if (this.tool.getDoSendRoles()) {
      if (sendAdminRole) {
        roles = Utils.addAdminRole(roles, this.user);
      }
      this.props.setProperty("roles", roles);
    }
    if (module != null) {
      if (this.tool.getDoSendContextId()) {
        String contextId = "";
        Id id = b2Context.getContext().getUserId();
        if (id != Id.UNSET_ID) {
          try {
            List<Node> nodes = NodeManagerFactory.getAssociationManager().loadUserAssociatedNodes(id);
            if (nodes.size() > 0) {
              contextId = nodes.get(0).getIdentifier();
            }
          } catch (PersistenceException e) {
          }
        }
        if (contextId.length() > 0) {
          this.props.setProperty("context_id", contextId);
        }
      }
      this.props.setProperty("context_type", "Group");
      this.props.setProperty("resource_link_id", module.getId().toExternalString());
      this.props.setProperty("resource_link_title", Utils.stripTags(module.getTitle()));
      this.props.setProperty("resource_link_description", Utils.stripTags(module.getDescriptionFormatted().getText()));
      if (tool.getDoSendRoles()) {
        boolean systemIRolesOnly = !b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_INSTITUTION_ROLES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
        List<PortalRole> iRoles = Utils.getInstitutionRoles(systemIRolesOnly, this.user);
        sendAdminRole = b2Context.getSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ADMINISTRATOR, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
        roles = Utils.getIRoles(b2Context, iRoles, sendAdminRole && this.user.getSystemRole().equals(User.SystemRole.SYSTEM_ADMIN));
        if (sendAdminRole) {
          roles = Utils.addAdminRole(roles, this.user);
        }
        this.props.setProperty("roles", roles);
      }
    }
// Consumer
    String css = this.tool.getLaunchCSS();
    if (css.length() > 0) {
      this.props.setProperty("ext_launch_presentation_css_url", css);
      this.props.setProperty("launch_presentation_css_url", css);
    }

    String[] version = B2Context.getVersionNumber("?.?.?").split("\\.");
    this.props.setProperty("ext_lms", Constants.LTI_LMS + "-" + version[0] + "." + version[1] + "." + version[2]);
    this.props.setProperty("tool_consumer_info_product_family_code", Constants.LTI_LMS);
    this.props.setProperty("tool_consumer_info_version", version[0] + "." + version[1] + "." + version[2]);

    String resource = this.tool.getResourceUrl();
    if (resource.length() > 0) {
      this.props.setProperty("ext_resource_link_content", resource);
      resource = this.tool.getResourceSignature();
      if (resource.length() > 0) {
        this.props.setProperty("ext_resource_link_content_signature", resource);
      }
    }

    String locale = this.user.getLocale();
    if ((locale == null) || (locale.length() <= 0)) {
      locale = (String)context.getAttribute(Constants.LOCALE_ATTRIBUTE);
    }
    this.props.setProperty("launch_presentation_locale", locale);

    this.props.setProperty("tool_consumer_instance_guid", this.tool.getLaunchGUID());
    this.props.setProperty("tool_consumer_instance_name", b2Context.getSetting(Constants.CONSUMER_NAME_PARAMETER, ""));
    this.props.setProperty("tool_consumer_instance_description", b2Context.getSetting(Constants.CONSUMER_DESCRIPTION_PARAMETER, ""));
    String email = b2Context.getSetting(Constants.CONSUMER_EMAIL_PARAMETER, "");
    if (email.length() > 0) {
      this.props.setProperty("tool_consumer_instance_contact_email", email);
    }

    this.props.setProperty("tool_consumer_instance_url", b2Context.getServerUrl());

    String target = "frame";
    if (this.tool.getOpenIn().equals(Constants.DATA_WINDOW)) {
      target = "window";
    } else if (this.tool.getOpenIn().equals(Constants.DATA_IFRAME)) {
      target = "iframe";
    }
    this.props.setProperty("launch_presentation_document_target", target);

  }

  public void signParameters(String url, String consumerKey, String secret) {

    this.props.setProperty("oauth_callback", Constants.OAUTH_CALLBACK);

    OAuthMessage oAuthMessage = null;
    oAuthMessage = new OAuthMessage("POST", url, this.props.entrySet());
    OAuthConsumer oAuthConsumer = new OAuthConsumer(Constants.OAUTH_CALLBACK, consumerKey, secret, null);
    OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
    try {
      oAuthMessage.addRequiredParameters(oAuthAccessor);
      this.params = oAuthMessage.getParameters();
    } catch (OAuthException e) {
      Logger.getLogger(LtiMessage.class.getName()).log(Level.SEVERE, null, e);
    } catch (IOException e) {
      Logger.getLogger(LtiMessage.class.getName()).log(Level.SEVERE, null, e);
    } catch (URISyntaxException e) {
      Logger.getLogger(LtiMessage.class.getName()).log(Level.SEVERE, null, e);
    }

  }

  public List<Entry<String, String>> getParams() {

    List<Map.Entry<String, String>> p;
    if (this.params != null) {
      p = Collections.unmodifiableList(this.params);
    } else {
      p = new ArrayList<Map.Entry<String, String>>();
    }

    return p;

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
