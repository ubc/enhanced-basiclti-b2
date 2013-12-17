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
      1.1.2  9-Oct-10  Constants moved to a this Class to share between Java and JSP code
      1.1.3  1-Jan-11  Added constants for User ID type and image file locations
      1.2.0 17-Sep-11  Added hash methods to support extension services
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12  Added functions for handling course roles, query strings, parsing
                       parameters and formatting calendar objects
      2.0.1 20-May-12
      2.1.0 18-Jun-12  Added functions to auto-generate tool IDs, provide domain support
                       and process XML descriptors
      2.2.0  2-Sep-12  Added function for handling dates from form fields
                       Added functions for handling VTBE mashup option (to fix support for Learn 9.0)
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12  Added $User custom substitution parameters
                       Added support for multiple roles in XML format
      2.3.2  3-Apr-13  Fixed bug in getToolFromXML when running Learn 9 under SSL
                       Fixed bug with generating new tool IDs for instructor-defined tools
                       Removed entity expansion from XML parsing
      3.0.0 30-Oct-13
*/
package org.oscelot.blackboard.lti;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Properties;

import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

import net.oauth.OAuthMessage;
import net.oauth.OAuth.Parameter;

import blackboard.data.course.Course;
import blackboard.data.user.User;
import blackboard.data.course.CourseMembership.Role;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.impl.OutcomeDefinition;
import blackboard.data.navigation.NavigationItem;
import blackboard.data.navigation.NavigationItem.NavigationType;
import blackboard.data.navigation.NavigationItem.ComponentType;
import blackboard.data.navigation.Mask;
import blackboard.data.role.PortalRole;
import blackboard.data.ValidationException;
import blackboard.platform.user.MyPlacesUtil;
import blackboard.platform.user.MyPlacesUtil.AvatarType;
import blackboard.platform.user.MyPlacesUtil.Setting;
import blackboard.platform.servlet.InlineReceiptUtil;
import blackboard.platform.gradebook2.GradebookManager;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.persist.role.PortalRoleDbLoader;
import blackboard.platform.security.CourseRole;
import blackboard.platform.security.persist.CourseRoleDbLoader;
import blackboard.platform.security.authentication.BbSecurityException;
import blackboard.platform.persistence.PersistenceServiceFactory;
import blackboard.persist.navigation.NavigationItemDbLoader;
import blackboard.persist.navigation.NavigationItemDbPersister;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.KeyNotFoundException;
import blackboard.servlet.util.DatePickerUtil;
import blackboard.portal.data.Module;
import blackboard.portal.persist.ModuleDbLoader;

import org.oscelot.blackboard.lti.services.Service;

import com.spvsoftwareproducts.blackboard.utils.B2Context;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.methods.GetMethod;


public class Utils {

  private static final char[] HEX_CHARS = {'0', '1', '2', '3',
                                           '4', '5', '6', '7',
                                           '8', '9', 'a', 'b',
                                           'c', 'd', 'e', 'f',};
  private static Comparator<PortalRole> cmSortByName = null;

// ---------------------------------------------------
// Function to generate an encrypted ID for use with LTI service requests

  public static String getServiceId(List<String> data, String value, String secret) {

    data.add(value);
    StringBuilder id = new StringBuilder();
    id.append(encodeHash(getHash(data, secret)));
    for (Iterator<String> iter = data.iterator(); iter.hasNext();) {
      String item = iter.next();
      id.append(Constants.HASH_SEPARATOR).append(encodeHash(item));
    }
    data.remove(data.size() - 1);

    return id.toString();

  }

// ---------------------------------------------------
// Function to get an encrypted hash value from a list using SHA-256 as base64

  private static String getHash(List<String> dataList, String secret) {

    StringBuilder data = new StringBuilder();

    for (Iterator<String> iter = dataList.iterator(); iter.hasNext();) {
      data.append(iter.next());
    }

    return getHash(data.toString(), secret);

  }

// ---------------------------------------------------
// Function to get an encrypted hash value from a string using SHA-256 as base64

  public static String getHash(String data, String secret) {

    return getHash(data, secret, "SHA-256", false);

  }

// ---------------------------------------------------
// Function to get an encrypted hash value from a string

  public static String getHash(String data, String secret, String algorithm, boolean asHex) {

    if ((secret == null) || (secret.length()<=0)) {
      return "";
    }

    String hash;

// Append the shared secret
    data = data + secret;
// Calculate the hash
    try {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      digest.reset();
      byte hashdata[];
      try {
        hashdata = digest.digest(data.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        hashdata = digest.digest(data.getBytes());
      }
      if (asHex) {
        hash = arrayToHexString(hashdata);
      } else {
        hash = new String(Base64.encodeBase64(hashdata));
      }
    } catch (NoSuchAlgorithmException e) {
      hash = "";
    }

    return hash;

  }

// ---------------------------------------------------
// Function to check the hash value.  The value is a concatenation of a hash and other
// data values; each element separated by a separator string.  The hash value is calculated
// from a concatenation of the query string and the data elements.  If the hash is
// verified the function returns the data values.

  public static String checkHash(HttpServletRequest request, String value, String secret) {

    String data = null;
    if (value != null) {
      String query = request.getQueryString();
      int pos = value.indexOf(Constants.HASH_SEPARATOR);
      if (pos >= 0) {
        data = value.substring(pos + 1);
        value = value.substring(0, pos);
      } else {
        data = "";
      }
      String calcHash = getHash(query + data, secret, "SHA-256", true);
      if (!calcHash.equals(value)) {
        data = null;
      }
    }

    return data;

  }

// ---------------------------------------------------
// Function to check hash value of the request body

  public static boolean checkBodyHash(String header, String xml) {

    boolean ok = false;

    List<Parameter> authParams;
    String value = null;
    authParams = OAuthMessage.decodeAuthorization(header);
    for (Iterator<Parameter> iter = authParams.iterator(); iter.hasNext();) {
      Parameter param = iter.next();
      if (param.getKey().equals("oauth_body_hash")) {
        value = param.getValue();
        break;
      }
    }
    if (value != null) {
      ok = value.equals(getHash("", xml, "SHA-1", false));
    }

    return ok;

  }

// ---------------------------------------------------
// Function to convert a byte array to a hexadecimal string

  private static String arrayToHexString (byte hash[]) {

    char buf[] = new char[hash.length * 2];
    int x = 0;
    for (int i = 0; i < hash.length; i++) {
      buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
      buf[x++] = HEX_CHARS[hash[i] & 0xf];
    }

    return new String(buf);

  }

// ---------------------------------------------------
// Function to get the authorization headers from a request

  public static Map<String,String> getAuthorizationHeaders(OAuthMessage message) {

    Map<String,String> headers = new HashMap<String,String>();

    try {
      String[] authHeaders = message.getAuthorizationHeader("").split(", ");
      for (int i = 0; i < authHeaders.length; i++) {
        String[] header = authHeaders[i].split("=");
        if (header.length == 2) {
          String name = header[0].trim();
          String value = header[1].trim();
          if (value.equals("\"\"")) {
            value = "";
          } else if ((value.length() > 2) && value.startsWith("\"")) {
            value = value.substring(1, value.length() - 1);
          }
          try {
            name = URLDecoder.decode(name, "UTF-8");
            value = URLDecoder.decode(value, "UTF-8");
          } catch (UnsupportedEncodingException e) {
          }
          headers.put(name, value);
        }
      }
    } catch (IOException e) {
      headers.clear();
    }

    return headers;

  }

// ---------------------------------------------------
// Function to get a course role with an option for replacing any admin-defined roles with
// a standard system role (either Instructor or Teaching Assistant).

  public static Role getRole(Role role, boolean systemRolesOnly) {

    if (systemRolesOnly) {
//      String version = B2Context.getVersionNumber("");
//      if (version.compareTo("9.1.") >= 0) {
      if (B2Context.getIsVersion(9, 1, 0)) {
        CourseRole cRole = role.getDbRole();
        if (cRole.isRemovable()) {
          if (cRole.isActAsInstructor()) {
            role = Role.INSTRUCTOR;
          } else {
            role = Role.TEACHING_ASSISTANT;
          }
        }
      }
    }

    return role;

  }

// ---------------------------------------------------
// Function to get a comma separated list of the LTI role names

  public static String getCRoles(String roleSetting, boolean isAdmin) {

    StringBuilder roles = new StringBuilder();
    if (roleSetting.contains("I")) {
      roles.append(Constants.ROLE_INSTRUCTOR).append(',');
    }
    if (roleSetting.contains("D")) {
      roles.append(Constants.ROLE_CONTENT_DEVELOPER).append(',');
    }
    if (roleSetting.contains("T")) {
     roles.append(Constants.ROLE_TEACHING_ASSISTANT).append(',');
    }
    if (roleSetting.contains("L")) {
      roles.append(Constants.ROLE_LEARNER).append(',');
    }
    if (roleSetting.contains("M")) {
      roles.append(Constants.ROLE_MENTOR).append(',');
    }
    if (isAdmin) {
      roles.append(Constants.ROLE_ADMINISTRATOR).append(',');
    }
    String rolesParameter = roles.toString();
    if (rolesParameter.endsWith(",")) {
      rolesParameter = rolesParameter.substring(0, rolesParameter.length() - 1);
    }

    return rolesParameter;

  }

// ---------------------------------------------------
// Function to get a comma separated list of the LTI role names

  public static String addAdminRole(String roles, User user) {

    if (user.getSystemRole().equals(User.SystemRole.SYSTEM_ADMIN)) {
      if (roles.length() > 0) {
        roles += ",";
      }
      roles += Constants.ROLE_SYSTEM_ADMINISTRATOR;
    }

    return roles;

  }

// ---------------------------------------------------
// Function to get a list of available course roles (with an option for including admin-defined roles)

  public static List<CourseRole> getCourseRoles(boolean systemRolesOnly) {

    List<CourseRole> roles;
    try {
      BbPersistenceManager pm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
      CourseRoleDbLoader crLoader = (CourseRoleDbLoader)pm.getLoader("CourseRoleDbLoader");
      List<CourseRole> allRoles = crLoader.loadAll();
      if (systemRolesOnly && B2Context.getIsVersion(9, 1, 0)) {
        roles = new ArrayList<CourseRole>();
        for (Iterator<CourseRole> iter = allRoles.listIterator(); iter.hasNext();) {
          CourseRole role = iter.next();
          if (!role.isRemovable()) {
            roles.add(role);
          }
        }
      } else {
        roles = new ArrayList<CourseRole>(allRoles);
      }
    } catch (PersistenceException e) {
      roles = new ArrayList<CourseRole>();
    }

    return roles;

  }

// ---------------------------------------------------
// Function to get a comma separated list of the LTI institution role names

  public static String getIRoles(B2Context b2Context, List<PortalRole> iRoles, boolean isAdmin) {

    HashSet<String> roles = new HashSet<String>();
    for (Iterator<PortalRole> iter = iRoles.iterator(); iter.hasNext();) {
      PortalRole role = iter.next();
      String iRoleSetting = b2Context.getSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_IROLE + "." + role.getRoleID(), "");
      if (iRoleSetting.contains("F")) {
        roles.add(Constants.IROLE_FACULTY);
      }
      if (iRoleSetting.contains("S")) {
        roles.add(Constants.IROLE_STAFF);
      }
      if (iRoleSetting.contains("L")) {
        roles.add(Constants.IROLE_STUDENT);
      }
      if (iRoleSetting.contains("P")) {
        roles.add(Constants.IROLE_PROSPECTIVE_STUDENT);
      }
      if (iRoleSetting.contains("A")) {
        roles.add(Constants.IROLE_ALUMNI);
      }
      if (iRoleSetting.contains("O")) {
        roles.add(Constants.IROLE_OBSERVER);
      }
      if (iRoleSetting.contains("G")) {
        roles.add(Constants.IROLE_GUEST);
      }
      if (iRoleSetting.contains("Z")) {
        roles.add(Constants.IROLE_OTHER);
      }
    }

    StringBuilder sRoles = new StringBuilder();
    for (Iterator<String> iter = roles.iterator(); iter.hasNext();) {
      String role = iter.next();
      sRoles.append(role).append(",");
    }
    if (isAdmin) {
      sRoles.append(Constants.IROLE_ADMINISTRATOR).append(',');
    }
    String rolesParameter = sRoles.toString();
    if (rolesParameter.endsWith(",")) {
      rolesParameter = rolesParameter.substring(0, rolesParameter.length() - 1);
    }

    return rolesParameter;

  }

// ---------------------------------------------------
// Function to get a list of available institution roles

  public static List<PortalRole> getInstitutionRoles(boolean systemRolesOnly) {

    return getInstitutionRoles(systemRolesOnly, null);

  }

// ---------------------------------------------------
// Function to get a list of available institution roles

  public static List<PortalRole> getInstitutionRoles(boolean systemRolesOnly, User user) {

    List<PortalRole> roles;
    try {
      BbPersistenceManager pm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
      PortalRoleDbLoader prLoader = (PortalRoleDbLoader)pm.getLoader("PortalRoleDbLoader");
      List<PortalRole> allRoles;
      if (user == null) {
        allRoles = prLoader.loadAll();
      } else {
        allRoles = prLoader.loadAllByUserId(user.getId());
      }
      if (systemRolesOnly) {
        roles = new ArrayList<PortalRole>();
        for (Iterator<PortalRole> iter = allRoles.listIterator(); iter.hasNext();) {
          PortalRole role = iter.next();
          if (!role.isRemovable()) {
            roles.add(role);
          }
        }
      } else {
        roles = new ArrayList<PortalRole>(allRoles);
      }
    } catch (PersistenceException e) {
      roles = new ArrayList<PortalRole>();
    }
    java.util.Collections.sort(roles, getSortByName());

    return roles;

  }

// ---------------------------------------------------
// Function to replace placeholders with user or course properties

  public static String parseParameter(B2Context b2Context, Properties props, Course course, User user, Tool tool, String value) {

    if (value.indexOf("$User.") >= 0) {
      if (tool.getDoSendUserId()) {
        value = value.replaceAll("\\$User.id", user.getId().toExternalString());
        value = value.replaceAll("\\$User.username", user.getUserName());
        value = value.replaceAll("\\$Person.studentId", user.getStudentId());
      }
    }
    if (value.indexOf("$Person.") >= 0) {
      if (tool.getDoSendUserSourcedid()) {
        value = value.replaceAll("\\$Person.sourcedId", user.getBatchUid());
      }
      if (tool.getDoSendUsername()) {
        value = value.replaceAll("\\$Person.name.full", (user.getGivenName() + " " + user.getFamilyName()).trim());
        value = value.replaceAll("\\$Person.name.family", user.getFamilyName());
        value = value.replaceAll("\\$Person.name.given", user.getGivenName());
        value = value.replaceAll("\\$Person.name.middle", user.getMiddleName());
        value = value.replaceAll("\\$Person.name.prefix", user.getTitle());
//        value = value.replaceAll("\\$Person.name.suffix", user.getSuffix());
        value = value.replaceAll("\\$Person.address.street1", user.getStreet1());
        value = value.replaceAll("\\$Person.address.street2", user.getStreet2());
//        value = value.replaceAll("\\$Person.address.street3", "");
//        value = value.replaceAll("\\$Person.address.street4", "");
        value = value.replaceAll("\\$Person.address.locality", user.getCity());
        value = value.replaceAll("\\$Person.address.statepr", user.getState());
        value = value.replaceAll("\\$Person.address.country", user.getCountry());
        value = value.replaceAll("\\$Person.address.postcode", user.getZipCode());
        value = value.replaceAll("\\$Person.address.timezone", user.getLocale());
        value = value.replaceAll("\\$Person.phone.mobile", user.getMobilePhone());
        value = value.replaceAll("\\$Person.phone.primary", user.getHomePhone1());
        value = value.replaceAll("\\$Person.phone.home", user.getHomePhone1());
        value = value.replaceAll("\\$Person.phone.work", user.getBusinessPhone1());
        value = value.replaceAll("\\$Person.webaddress", user.getWebPage());
//        value = value.replaceAll("\\$Person.sms", "");
      }
      if (tool.getDoSendEmail()) {
        value = value.replaceAll("\\$Person.email.primary", user.getEmailAddress());
        value = value.replaceAll("\\$Person.email.personal", user.getEmailAddress());
      }
    }
    if (value.indexOf("$CourseSection.") >= 0) {
      if (tool.getDoSendContextSourcedid()) {
        value = value.replaceAll("\\$CourseSection.sourcedId", course.getBatchUid());
        value = value.replaceAll("\\$CourseSection.dataSource", course.getDataSourceId().toExternalString());
        value = value.replaceAll("\\$CourseSection.sourceSectionId", course.getCourseId());
      }
      value = value.replaceAll("\\$CourseSection.label", "");
      value = value.replaceAll("\\$CourseSection.title", course.getTitle());
      value = value.replaceAll("\\$CourseSection.shortDescription", course.getDescription());
      value = value.replaceAll("\\$CourseSection.longDescription", course.getDescription());
//      value = value.replaceAll("\\$CourseSection.courseNumber", "");
//      value = value.replaceAll("\\$CourseSection.credits", "");
//      value = value.replaceAll("\\$CourseSection.maxNumberofStudents", "");
//      value = value.replaceAll("\\$CourseSection.numberofStudents", "");
//      value = value.replaceAll("\\$CourseSection.dept", "");
      value = value.replaceAll("\\$CourseSection.timeFrame.begin",
         formatCalendar(course.getStartDate(), Constants.DATE_FORMAT));
      value = value.replaceAll("\\$CourseSection.timeFrame.end",
         formatCalendar(course.getEndDate(), Constants.DATE_FORMAT));
//      value = value.replaceAll("\\$CourseSection.enrollControl.accept", "");
//      value = value.replaceAll("\\$CourseSection.enrollControl.allowed", "");
    }
    if (value.indexOf("$Result.") >= 0) {
      if (props.containsKey("lis_result_sourcedid")) {
        value = value.replaceAll("\\$Result.sourcedId", props.getProperty("lis_result_sourcedid"));
      }
    }
    ServiceList serviceList = new ServiceList(b2Context, false);
    List<Service> services = serviceList.getList();
    Service service = null;
    for (Iterator<Service> iter = services.iterator(); iter.hasNext();) {
      service = iter.next();
      service.setTool(tool);
      value = service.parseValue(value);
    }

    return value;

  }

// ---------------------------------------------------
// Function to check if settings have moved to a new course

  public static void checkCourse(B2Context b2Context) {

    if (b2Context.getContext().hasCourseContext()) {
      String courseId = b2Context.getContext().getCourseId().toExternalString();
      B2Context courseContext = new B2Context(b2Context.getRequest());
      courseContext.setIgnoreContentContext(true);
      courseContext.setIgnoreGroupContext(true);

      String oldCourseId = courseContext.getSetting(false, true, Constants.TOOL_COURSEID, "");

      boolean doSave = false;
      if (oldCourseId.length() <= 0) {
        courseContext.setSetting(false, true, Constants.TOOL_COURSEID, courseId);
        doSave = true;
      } else if (!oldCourseId.equals(courseId)) {
        String toolOrder = courseContext.getSetting(false, true, "tools.order", "");
        String[] tools = toolOrder.split(",");
        for (int i = 0; i < tools.length; i++) {
          String tool = tools[i];
          courseContext.setSetting(false, true, Constants.TOOL_PARAMETER_PREFIX + "." + tool + "." + Constants.TOOL_LINEITEM, null);
        }
        courseContext.setSetting(false, true, Constants.TOOL_COURSEID, courseId);
        doSave = true;
      }
      if (doSave) {
        courseContext.persistSettings(false, true);
      }
    }

  }

// ---------------------------------------------------
// Function to check if grade centre column should be created

  public static boolean checkColumn(B2Context b2Context, String toolId, String toolName, String columnFormat,
     Integer points, boolean scorable, boolean visible, boolean create) {

    boolean exists = false;

    boolean isLocal = true;
    String toolSettingPrefix = Constants.TOOL_PARAMETER_PREFIX + ".";
    if (toolId == null) {
      toolId = b2Context.getSetting(false, true, toolSettingPrefix + Constants.TOOL_ID, "");
    }
    if (toolId.length() > 0) {
      isLocal = toolId.startsWith(Constants.COURSE_TOOL_PREFIX);
      toolSettingPrefix += toolId + ".";
    }
    if (isLocal) {
      b2Context.setIgnoreContentContext(true);
    }
    if (columnFormat == null) {
      columnFormat = b2Context.getSetting(!isLocal, true, toolSettingPrefix + Constants.TOOL_EXT_OUTCOMES_FORMAT,
         Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE);
    }
    if (isLocal) {
      b2Context.setIgnoreContentContext(false);
    }
    String scaleType = Constants.PERCENTAGE_RESULT_TYPE;
    if (columnFormat.equals(Constants.EXT_OUTCOMES_COLUMN_SCORE)) {
      scaleType = Constants.RATIO_RESULT_TYPE;
    }
    if (!B2Context.getIsVersion(9, 1, 0)) {
      exists = (Gradebook_v90.getColumn(b2Context, toolId, scaleType, points, scorable, visible, null, create) != null);
    } else {
      Lineitem lineitem = Gradebook.getColumn(b2Context, toolId, toolName, scaleType, points, scorable, visible, null, create);
      if (lineitem != null) {
        exists = true;
        OutcomeDefinition def = lineitem.getOutcomeDefinition();
        if ((def != null) && !toolName.equals(def.getTitle())) {
          try {
            def.setTitle(toolName);
            def.persist();
          } catch (ValidationException e) {
          } catch (PersistenceException e) {
          }
        }
      }
    }

    return exists;

  }

  public static Id getLineItemIdByContentId(Id contentId) {

    Id id = null;

    try {
      GradebookManager gbManager = GradebookManagerFactory.getInstanceWithoutSecurityCheck();
      GradableItem gradableItem = gbManager.getGradebookItemByContentId(contentId);
      if (gradableItem != null) {
        id = gradableItem.getId();
      }
    } catch (BbSecurityException e) {
    }

    return id;

  }

// ---------------------------------------------------
// Function to convert a float value to a String value

  public static String floatToString(float fValue) {

    String value = String.valueOf(fValue);
    value = value.replaceFirst("\\.*0*$", "");

    return value;

  }

// ---------------------------------------------------
// Function to convert a String value to a float value

  public static Integer stringToInteger(String value) {

    Integer iValue = null;
    try {
      iValue = Integer.valueOf(value);
    } catch (NumberFormatException e) {
      iValue = null;
    }

    return iValue;

  }

// ---------------------------------------------------
// Function to convert a String value to a float value

  public static Float stringToFloat(String value) {

    Float fValue = null;
    try {
      fValue = Float.valueOf(value);
    } catch (NumberFormatException e) {
      fValue = null;
    }

    return fValue;

  }

// ---------------------------------------------------
// Function to encode any instances of the hash separator

  public static String encodeHash(String hash) {

    hash = hash.replace("%", "%25");
    hash = hash.replace(Constants.HASH_SEPARATOR, "%" + arrayToHexString(Constants.HASH_SEPARATOR.getBytes()));

    return hash;

  }

// ---------------------------------------------------
// Function to decode any instances of the hash separator

  public static String decodeHash(String hash) {

    hash = hash.replace("%" + arrayToHexString(Constants.HASH_SEPARATOR.getBytes()), Constants.HASH_SEPARATOR);
    hash = hash.replace("%25", "%");

    return hash;

  }

// ---------------------------------------------------
// Function to convert an XML string value to an XML document object

  public static Document getXMLDoc(String xml) {

    Document xmlDoc = null;

// Remove any garbage from the top of the XML response
    int pos = xml.indexOf("<?xml ");
    if (pos > 0) {
      xml = xml.substring(pos);
    }
    try {
      SAXBuilder sb = new SAXBuilder();
      sb.setExpandEntities(false);
      xmlDoc = sb.build(new ByteArrayInputStream(xml.getBytes()));
    } catch (JDOMException e) {
      Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
    } catch (IOException e) {
      Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
    }

    return xmlDoc;

  }

// ---------------------------------------------------
// Function to get a named XML child element from a parent element

  public static Element getXmlChild(Element root, String name) {

    Element child = null;
    List<Element> elements = null;
    if (name != null) {
      ElementFilter elementFilter = new ElementFilter(name);
      Iterator<Element> iter = (Iterator<Element>)root.getDescendants(elementFilter);
      if (iter.hasNext()) {
        child = iter.next();
      }
    } else {
      elements = (List<Element>)root.getChildren();
      if (elements.size() >= 1) {
        child = elements.get(0);
      }
    }

    return child;

  }

// ---------------------------------------------------
// Function to get a named XML child value from a parent element

  public static String getXmlChildValue(Element root, String name) {

    String value = null;
    Element child = getXmlChild(root, name);
    if (child != null) {
      value = child.getText();
    }

    return value;

  }

// ---------------------------------------------------
// Function to remove tags from a string

  public static String stripTags(String str) {

    if (str != null ) {
      str = str.replaceAll("\\<.*?>","").trim();
    }

    return str;

  }

// ---------------------------------------------------
// Function to replace special characters with their HTML codes

  public static String htmlSpecialChars(String str) {

    if (str != null ) {
      str = str.replace("&", "&amp;");
      str = str.replace("\"", "&quot;");
      str = str.replace("<", "&lt;");
      str = str.replace(">", "&gt;");
    }

    return str;

  }

// ---------------------------------------------------
// Function to convert a null string to an empty string

  public static String nullToEmpty(String str) {

    if (str == null ) {
      str = "";
    }

    return str;

  }

// ---------------------------------------------------
// Function to get the query string, removing any instances of action, tool ID and receipt
// messsage parameters

  public static String getQuery(HttpServletRequest request) {

    String query = "&" + nullToEmpty(request.getQueryString());
    query = query.replaceAll("&" + Constants.ACTION + "=[^&]*", "");
    query = query.replaceAll("&" + Constants.TOOL_ID + "=[^&]*", "");
    query = query.replaceAll("&" + InlineReceiptUtil.SIMPLE_STRING_KEY + "[A-Za-z0-9]*=[^&]*", "");
    query = query.replaceAll("&" + InlineReceiptUtil.SIMPLE_ERROR_KEY + "[A-Za-z0-9]*=[^&]*", "");
    query = query.replaceAll("&" + Constants.LTI_MESSAGE + "=[^&]*", "");
    query = query.replaceAll("&" + Constants.LTI_LOG + "=[^&]*", "");
    query = query.replaceAll("&" + Constants.LTI_ERROR_MESSAGE + "=[^&]*", "");
    query = query.replaceAll("&" + Constants.LTI_ERROR_LOG + "=[^&]*", "");
    if (query.length() > 1) {
      query = query.substring(1);
    } else {
      query = "";
    }

    return query;

  }

// ---------------------------------------------------
// Function to format a calendar object as a String

  public static String formatCalendar(Calendar cal, String format) {

    String dateString = "";

    if (cal != null) {
      SimpleDateFormat formatter = new SimpleDateFormat(format);
      dateString = formatter.format(cal.getTime());
    }

    return dateString;

  }

// ---------------------------------------------------
// Function to make an HTTP GET request and return the response

  public static String readUrlAsString(B2Context b2Context, String urlString) {

    String str = "";
    int timeout;
    try {
      timeout = Integer.parseInt(b2Context.getSetting(Constants.TIMEOUT_PARAMETER) + "000");
    } catch (NumberFormatException e) {
      timeout = Constants.TIMEOUT;
    }
    GetMethod fileGet = null;
    try {
      fileGet = new GetMethod(urlString);
      HttpClient client = new HttpClient();
      client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
      int resp = client.executeMethod(fileGet);
      if (resp == 200) {
        str = fileGet.getResponseBodyAsString();
      }
    } catch (IOException e) {
      Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
      str = "";
    }
    if (fileGet != null) {
      fileGet.releaseConnection();
    }

    return str;

  }

// ---------------------------------------------------
// Function to extract a domain (including optional path) from a URL

  public static String urlToDomainName(String urlString) {

    try {
      if (urlString.indexOf("://") < 0) {
        urlString = "http://" + urlString;
      }
      URL url = new URL(urlString);
      String path = url.getPath();
      String[] pathParts = path.split("/");
      if (pathParts.length > 0) {
        String lastPart = pathParts[pathParts.length - 1];
        if (lastPart.indexOf(".") >= 0) {
          path = path.substring(0, path.length() - lastPart.length() - 1);
        }
      }
      if (path.equals("/")) {
        path = "";
      }
      urlString = url.getHost() + path;
      urlString = urlString.toLowerCase();
    } catch (MalformedURLException e) {
      urlString = "";
    }

    return urlString;

  }

// ---------------------------------------------------
// Function to extract a domain (including optional path) from a URL

  public static Tool urlToDomain(B2Context b2Context, String urlString) {

    Tool domain = null;
    urlString = Utils.urlToDomainName(urlString);
    if (urlString.length() > 0) {
      try {
        if (urlString.indexOf("://") < 0) {
          urlString = "http://" + urlString;
        }
        URL url = new URL(urlString);
        String urlHost = url.getHost();
        String urlPath = url.getPath();
        String domainHost = "";
        String domainPath = "";
        ToolList domainList = new ToolList(b2Context, true, true);
        List<Tool> domains = domainList.getList();
        for (Iterator<Tool> iter = domains.iterator(); iter.hasNext();) {
          Tool tool = iter.next();
          String[] name = tool.getName().split("/", 2);
          if (urlHost.endsWith(name[0])) {
            if ((name[0].length() > domainHost.length()) &&
                ((name.length <= 1) || urlPath.startsWith("/" + name[1]))) {
              domainHost = name[0];
              if (name.length > 1) {
                domainPath = name[1];
              } else {
                domainPath = "";
              }
              domain = tool;
            } else if (name[0].equals(domainHost) && (name.length > 1) && urlPath.startsWith("/" + name[1]) &&
               (name[1].length() > domainPath.length())) {
              domainPath = name[1];
              domain = tool;
            }
          }
        }
      } catch (MalformedURLException e) {
        urlString = "";
      }
    }

    return domain;

  }

// ---------------------------------------------------
// Function to generate a new unique ID for a tool or domain

  public static String getNewToolId(B2Context b2Context, String toolName, boolean isDomain, boolean isSystemTool) {

    String prefix;
    if (isDomain) {
      prefix = Constants.DOMAIN_PARAMETER_PREFIX + ".";
    } else {
      prefix = Constants.TOOL_PARAMETER_PREFIX + ".";
    }
    String baseName = toolName.toLowerCase();
    baseName = baseName.replaceAll("[^0-9a-z]", "");
    if (baseName.length() > 10) {
      baseName = baseName.substring(0, 6);
    }
    if (!isSystemTool) {
      baseName = Constants.COURSE_TOOL_PREFIX + baseName;
    }
    String name = baseName;
    int i = 0;
    do {
      if (!name.equals(Constants.DEFAULT_TOOL_ID) &&
          b2Context.getSetting(isSystemTool, true, prefix + name + "." + Constants.TOOL_NAME).length() <= 0) {
        break;
      }
      i++;
      name = baseName + String.valueOf(i);
    } while (true);

    return name;

  }

// ---------------------------------------------------
// Function to extract the configuration settings for a tool or domain from an XML description

  public static Map<String,String> getToolFromXML(B2Context b2Context, String xml, boolean isSecure,
     boolean isDomain, boolean isSystemTool, boolean isContentItem) {

    Map<String,String> params = null;

    Document doc = getXMLDoc(xml);
    Element root = null;

    boolean ok = (doc != null);
    if (ok) {
      root = doc.getRootElement();
      ok = root.getName().equals(Constants.XML_ROOT);
    }
    if (ok) {
      params = new HashMap<String,String>();
      params.put(Constants.TOOL_NAME, getXmlChildValue(root, Constants.XML_TITLE));
      params.put(Constants.TOOL_DESCRIPTION, getXmlChildValue(root, Constants.XML_DESCRIPTION));
      String secure = getXmlChildValue(root, Constants.XML_URL_SECURE);
      if (isSecure && (secure != null) && (secure.length() > 0)) {
        params.put(Constants.TOOL_URL, secure);
      } else {
        params.put(Constants.TOOL_URL, getXmlChildValue(root, Constants.XML_URL));
      }
      secure = getXmlChildValue(root, Constants.XML_ICON_SECURE);
      if (isSecure && (secure != null) && (secure.length() > 0)) {
        params.put(Constants.TOOL_ICON, secure);
      } else {
        params.put(Constants.TOOL_ICON, getXmlChildValue(root, Constants.XML_ICON));
      }
      Map<String,String> customParams = new HashMap<String,String>();
      Element node = getXmlChild(root, Constants.XML_CUSTOM);
      if (node != null) {
        List<Element> properties = (List<Element>)node.getChildren();
        if (properties != null) {
          for (Iterator<Element> iter = properties.iterator(); iter.hasNext();) {
            node = iter.next();
            if (node.getName().equals(Constants.XML_PARAMETER)) {
              String name = node.getAttributeValue(Constants.XML_PARAMETER_KEY);
              String value = node.getValue();
              if ((name != null) && (value != null)) {
                customParams.put(name, value);
              }
            }
          }
        }
      }
      ElementFilter elementFilter = new ElementFilter(Constants.XML_EXTENSION);
      for (Iterator<Element> iter = (Iterator<Element>)root.getDescendants(elementFilter); iter.hasNext();) {
        Element extension = iter.next();
        String platform = extension.getAttributeValue(Constants.XML_EXTENSION_PLATFORM);
        if (platform.equals(Constants.LTI_LMS)) {
          List<Element> properties = (List<Element>)extension.getChildren();
          if (properties != null) {
            for (Iterator<Element> prop = properties.iterator(); prop.hasNext();) {
              node = prop.next();
              if (node.getName().equals(Constants.XML_PARAMETER)) {
                String name = node.getAttributeValue(Constants.XML_PARAMETER_KEY);
                String value = node.getValue();
                if (name.startsWith(Constants.CUSTOM_NAME_PREFIX)) {
                  name = name.substring(Constants.CUSTOM_NAME_PREFIX.length());
                  customParams.put(name, value);
                } else {
                  params.put(name, value);
                }
              }
            }
          }
        }
      }
      if (!customParams.isEmpty()) {
        StringBuilder custom = new StringBuilder();
        for (Iterator<Map.Entry<String,String>> iter = customParams.entrySet().iterator(); iter.hasNext();) {
          Map.Entry<String,String> entry = iter.next();
          custom = custom.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        params.put(Constants.TOOL_CUSTOM, custom.toString());
      }
System.err.println(mapToString(params));
      params = checkXMLParams(b2Context, params, isDomain, isSystemTool, isContentItem);
    }
System.err.println(mapToString(params));

    return params;

  }

// ---------------------------------------------------
// Function to validatae the values for tool/domain configuration settings

  private static Map<String,String> checkXMLParams(B2Context b2Context, Map<String,String>params,
     boolean isDomain, boolean isSystemTool, boolean isContentItem) {

    Map<String,String> extensionProps = new HashMap<String,String>();
    extensionProps.put(Constants.TOOL_NAME, "");
    extensionProps.put(Constants.TOOL_DESCRIPTION, "");
    extensionProps.put(Constants.TOOL_URL, "");
    extensionProps.put(Constants.TOOL_GUID, "");
    extensionProps.put(Constants.TOOL_SECRET, "");
    extensionProps.put(Constants.TOOL_USERID, Constants.DATA_OPTIONAL);
    extensionProps.put(Constants.TOOL_USERNAME, Constants.DATA_OPTIONAL);
    extensionProps.put(Constants.TOOL_EMAIL, Constants.DATA_OPTIONAL);
    extensionProps.put(Constants.TOOL_CUSTOM, "");
    extensionProps.put(Constants.TOOL_EXT_OUTCOMES, Constants.DATA_OPTIONAL);
    extensionProps.put(Constants.TOOL_EXT_OUTCOMES_COLUMN, Constants.DATA_TRUE);
    extensionProps.put(Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE + Constants.EXT_OUTCOMES_COLUMN_SCORE);
    extensionProps.put(Constants.TOOL_EXT_OUTCOMES_POINTS, "1");
    extensionProps.put(Constants.TOOL_EXT_OUTCOMES_SCORABLE, Constants.DATA_TRUE);
    extensionProps.put(Constants.TOOL_EXT_OUTCOMES_VISIBLE, Constants.DATA_TRUE);
    extensionProps.put(Constants.TOOL_EXT_MEMBERSHIPS, Constants.DATA_OPTIONAL);
    extensionProps.put(Constants.TOOL_EXT_MEMBERSHIPS_LIMIT, Constants.DATA_TRUE);
    extensionProps.put(Constants.TOOL_EXT_MEMBERSHIPS_GROUPS, Constants.DATA_TRUE);
    extensionProps.put(Constants.TOOL_EXT_MEMBERSHIPS_GROUPNAMES, "");
    extensionProps.put(Constants.TOOL_EXT_SETTING, Constants.DATA_OPTIONAL);
    extensionProps.put(Constants.TOOL_CSS, "");
    extensionProps.put(Constants.TOOL_ICON, "");

    StringBuilder roles = new StringBuilder();
    if (!isContentItem) {
      extensionProps.put(Constants.TOOL_CONTEXT_ID, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_CONTEXTIDTYPE, Constants.DATA_BATCHUID + Constants.DATA_COURSEID + Constants.DATA_PRIMARYKEY);
      extensionProps.put(Constants.TOOL_CONTEXT_SOURCEDID, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_CONTEXT_TITLE, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_AVATAR, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_ROLES, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_USERIDTYPE, Constants.DATA_BATCHUID + Constants.DATA_USERNAME + Constants.DATA_STUDENTID + Constants.DATA_PRIMARYKEY);
      extensionProps.put(Constants.TOOL_USER_SOURCEDID, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_OPEN_IN, Constants.DATA_FRAME + Constants.DATA_FRAME_NO_BREADCRUMBS +
         Constants.DATA_WINDOW + Constants.DATA_IFRAME);
      extensionProps.put(Constants.TOOL_WINDOW_NAME, null);
      extensionProps.put(Constants.TOOL_SPLASH, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_SPLASHTEXT, "");
      extensionProps.put(Constants.TOOL_ADMINISTRATOR, Constants.DATA_TRUE);
      extensionProps.put(Constants.TOOL_EXT_MEMBERSHIPS_LIMIT, Constants.DATA_TRUE);
      List<CourseRole> cRoles = Utils.getCourseRoles(true);
      for (Iterator<CourseRole> iter = cRoles.iterator(); iter.hasNext();) {
        CourseRole cRole = iter.next();
        roles.append(cRole.getIdentifier());
      }
      if (isSystemTool) {
        extensionProps.put(Constants.MESSAGE_PARAMETER_PREFIX + "." + Constants.MESSAGE_CONFIG, Constants.DATA_TRUE);
        ServiceList services = new ServiceList(b2Context, true);
        Service service;
        for (Iterator<Service> iter = services.getList().iterator(); iter.hasNext();) {
          service = iter.next();
          extensionProps.put(Constants.SERVICE_PARAMETER_PREFIX + "." + service.getId(), Constants.DATA_TRUE);
        }
      }
    }
    String roleIDs = Constants.ROLE_ID_INSTRUCTOR + Constants.ROLE_ID_CONTENT_DEVELOPER + Constants.ROLE_ID_TEACHING_ASSISTANT +
                     Constants.ROLE_ID_LEARNER + Constants.ROLE_ID_MENTOR;
    if (params != null) {
      for (Iterator<Map.Entry<String,String>> iter = params.entrySet().iterator(); iter.hasNext();) {
        Map.Entry<String,String> param = iter.next();
        String name = param.getKey();
        String value = param.getValue();
        boolean ok = (value != null) && extensionProps.containsKey(name);
        if (ok) {
          String propType = extensionProps.get(name);
          if (propType == null) {
            params.put(name, value.trim());
          } else if (propType.equals(Constants.DATA_TRUE)) {
            value = value.toLowerCase();
            ok = value.equals(Constants.DATA_TRUE) || value.equals(Constants.DATA_FALSE);
          } else if (propType.equals(Constants.DATA_OPTIONAL)) {
            if (isDomain || isSystemTool) {
              value = value.toUpperCase();
              ok = value.equals(Constants.DATA_NOTUSED) || value.equals(Constants.DATA_OPTIONAL) || value.equals(Constants.DATA_MANDATORY);
            } else {
              value = value.toUpperCase();
              ok = value.equals(Constants.DATA_NOTUSED) || value.equals(Constants.DATA_MANDATORY);
              if (ok && isContentItem) {
                if (value.equals(Constants.DATA_NOTUSED)) {
                  value = Constants.DATA_FALSE;
                } else {
                  value = Constants.DATA_TRUE;
                }
                params.put(name, value);
              }
            }
          } else if (propType.equals("1")) {
            Integer num = Utils.stringToInteger(value);
            ok = (num != null) && (num > 0);
          } else if (propType.length() > 0) {
            value = value.toUpperCase();
            ok = propType.indexOf(value) >= 0;
          }
        } else if (name.startsWith(Constants.TOOL_ROLE + ".")) {
          String role = name.substring(Constants.TOOL_ROLE.length() + 1);
          role = role.toUpperCase();
          ok = roles.toString().indexOf(role) >= 0;
          if (ok) {
            String[] allRoles = value.split(",");
            StringBuilder valueString = new StringBuilder();
            for (int i = 0; i < allRoles.length; i++) {
              String aRole = allRoles[i].trim();
              if (i > 0) {
                valueString.append(",");
              }
              valueString.append(aRole);
              ok = ok && (roleIDs.indexOf(aRole) >= 0);
            }
            if (!value.equals(valueString.toString())) {
              params.put(name, valueString.toString());
            }
          }
        }
        if (!ok) {
          iter.remove();
        }
      }
      if (isDomain && params.containsKey(Constants.TOOL_URL)) {
        params.put(Constants.TOOL_NAME, params.get(Constants.TOOL_URL));
        params.remove(Constants.TOOL_URL);
      }
    }

    return params;

  }

// ---------------------------------------------------
// Function to add the VTBE mashup option (to allow Learn 9.0 to accept the manifest file)

  private static void addVTBEMashup(B2Context b2Context, String toolId, String name, String description) {

    String appName = b2Context.getVendorId() + "-" + b2Context.getHandle();
    NavigationItem navItem = new NavigationItem();

    String url;
    String suffix = toolId;
    if (toolId.length() <= 0) {
      url = "vtbe/link.jsp?course_id=@X@course.pk_string@X@&amp;content_id=@X@content.pk_string@X@";
    } else {
      url = "vtbe/item.jsp?course_id=@X@course.pk_string@X@&content_id=@X@content.pk_string@X@&tool=" + toolId;
      suffix = "-" + suffix;
    }
    navItem.setInternalHandle(appName + "-nav-vtbe" + suffix);
    navItem.setLabel(name);
    navItem.setDescription(description);
    navItem.setHref(b2Context.getPath() + url);
    navItem.setSrc(null);
    navItem.setApplication(appName);
    navItem.setFamily("0");
    navItem.setSubGroup("vtbe_mashup");
    navItem.setNavigationType(NavigationType.COURSE);
    navItem.setComponentType(ComponentType.MENU_ITEM);
    navItem.setIsEnabledMask(new Mask(3));
    navItem.setEntitlementUid("system.generic.VIEW");

    try {
      navItem.persist();
    } catch (ValidationException e) {
    } catch (PersistenceException e) {
    }

  }

// ---------------------------------------------------
// Function to check that the VTBE mashup tool is enabled/disabled as per the configuration setting
  public static boolean checkVTBEMashup(B2Context b2Context, boolean enabled) {

    return checkVTBEMashup(b2Context, enabled, "", b2Context.getResourceString("plugin.vtbe.name"),
       b2Context.getResourceString("plugin.vtbe.description"));

  }

  public static boolean checkVTBEMashup(B2Context b2Context, boolean enabled, String toolId, String name, String description) {

    boolean ok = true;
    Id id = Id.UNSET_ID;
    String suffix = toolId;
    if (toolId.length() > 0) {
      suffix = "-" + suffix;
    }
    String handle = b2Context.getVendorId() + "-" + b2Context.getHandle() + "-nav-vtbe" + suffix;
    try {
      NavigationItemDbLoader navLoader = NavigationItemDbLoader.Default.getInstance();
      NavigationItem navItem = navLoader.loadByInternalHandle(handle);
      id = navItem.getId();
    } catch (KeyNotFoundException e) {
    } catch (PersistenceException e) {
      ok = false;
    }
    if (ok && (enabled ^ id.getIsSet())) {
      if (!id.getIsSet()) {
        addVTBEMashup(b2Context, toolId, name, description);
      } else {
        try {
          NavigationItemDbPersister navPersister = NavigationItemDbPersister.Default.getInstance();
          navPersister.deleteById(id);
        } catch (KeyNotFoundException e) {
        } catch (PersistenceException e) {
          ok = false;
        }
      }
    }

    return ok;

  }

// ---------------------------------------------------
// Function to remove course tools option for any tools which have become disabled because a domain is denied

  public static void doCourseToolsDelete(B2Context b2Context, String domainId) {

    ToolList toolList = new ToolList(b2Context);
    List<Tool> tools = toolList.getList();
    for (Iterator<Tool> iter = tools.iterator(); iter.hasNext();) {
      Tool tool = iter.next();
      if ((tool.getDomain() != null) && (tool.getDomain().getId().contains(domainId))) {
        CourseTool courseTool = tool.getCourseTool();
        if ((courseTool != null) && !tool.getIsEnabled().equals(Constants.DATA_TRUE)) {
          courseTool.delete();
        }
      }
    }

  }

// ---------------------------------------------------
// Function to extract a date from a form field (replicates function available only in Learn 9.1)

  public static Calendar getDateFromPicker(String checkbox, String dateStr) {

    Calendar cal = null;
    boolean enabled = DatePickerUtil.isCheckboxChecked(checkbox);
    if (enabled && (dateStr != null) && (dateStr.length() > 0)) {
      cal = DatePickerUtil.pickerDatetimeStrToCal(dateStr);
    }

    return cal;

  }

// ---------------------------------------------------
// Function to determine is a user has an available avatar

  public static boolean displayAvatar(Id userId) {

    boolean usingSystem = false;
    boolean usingUploaded = false;
      Map<String,String> userData = MyPlacesUtil.getMyPlacesUserData(userId);
      usingSystem = MyPlacesUtil.getAvatarType().equals(AvatarType.system) && (userData.get(Setting.AVATAR_SHOW_SYSDEF.getKey())).equalsIgnoreCase("true");
      if (!usingSystem) {
        usingUploaded = MyPlacesUtil.getAvatarType().equals(AvatarType.user) && (userData.get(Setting.AVATAR_SHOW_USER.getKey())).equalsIgnoreCase("true");
      }

    return usingSystem || usingUploaded;

  }

// ---------------------------------------------------
// Function to get a module from an ID

  public static Module getModule(String moduleId) {

    Module module = null;
    if ((moduleId != null) && (moduleId.length() > 0)) {
      try {
        Id id = Id.generateId(Module.DATA_TYPE, moduleId);
        module = ModuleDbLoader.Default.getInstance().loadById(id);
      } catch (KeyNotFoundException e) {
      } catch (PersistenceException e) {
      }
    }

    return module;

  }

// ---------------------------------------------------
// Function to check if a module ID has been passed

  public static String checkForModule(HttpServletRequest request) {

    String moduleId = request.getParameter(Constants.TOOL_MODULE);
    if (moduleId != null) {
      Module module = null;
      try {
        Id id = Id.generateId(Module.DATA_TYPE, moduleId);
        module = ModuleDbLoader.Default.getInstance().loadById(id);
        request.setAttribute("blackboard.portal.data.Module", module);
      } catch (KeyNotFoundException e) {
      } catch (PersistenceException e) {
      }
    }

    return moduleId;

  }

/**
 * Returns the prefix used for session parameters.
 *
 * @param b2Context  B2Context object
 * @return prefix
 */
  public static String getSessionPrefix(B2Context b2Context) {

    return b2Context.getVendorId() + "-" + b2Context.getHandle() + "-";

  }

/**
 * Returns the value of a session parameter.
 *
 * @param session  HTTP session
 * @param b2Context  B2Context object
 * @param name  Name of parameter
 * @return value
 */
  public static String getValueFromSession(HttpSession session, B2Context b2Context, String name, String defaultValue) {

    String value = (String)session.getAttribute(getSessionPrefix(b2Context) + name);
    if (value == null) {
      value = defaultValue;
    }

    return value;

  }

/**
 * Sets the value of a session parameter.
 *
 * @param session  HTTP session
 * @param b2Context  B2Context object
 * @param name  Name of parameter
 * @param value  Parameter value
 */
  public static void setValueInSession(HttpSession session, B2Context b2Context, String name, String value) {

    if (value != null) {
      session.setAttribute(getSessionPrefix(b2Context) + name, value);
    } else {
      session.removeAttribute(getSessionPrefix(b2Context) + name);
    }

  }

// ---------------------------------------------------
// Function to URL encode a string

  public static String urlEncode(String value) {

    try {
      value = URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      value = URLEncoder.encode(value);
    }

    return value;

  }

// ---------------------------------------------------
// Function to encode a string for insertionmin XML

  public static String xmlEncode(String value) {

    value = value.replaceAll("&", "&#x26;");
    value = value.replaceAll("<", "&#x60;");
    value = value.replaceAll(">", "&#x62;");

    return value;

  }

// ---------------------------------------------------
// Function to convert a map to a string

  public static String mapToString(Map<String,String> map) {

    StringBuilder data = new StringBuilder();
    if (map != null) {
      for (Iterator<Map.Entry<String,String>> iter = map.entrySet().iterator(); iter.hasNext();) {
        Map.Entry<String,String> entry = iter.next();
        data.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
      }
    }

    return data.toString();

  }

// ---------------------------------------------------
// Function to convert a query string to a map of the query parameters

  public static Map<String,String> queryToMap(String query) {

    Map<String,String> params = new HashMap<String,String>();
    String[] queryParams = query.split("&");
    String[] parts;
    for (int i = 0; i < queryParams.length; i++) {
      parts = queryParams[i].split("=", 2);
      if (parts[0].length() > 0) {
        if (parts.length < 2) {
          params.put(parts[0], "");
        } else {
          params.put(parts[0], parts[1]);
        }
      }
    }

    return params;

  }

// ---------------------------------------------------
// Comparator for sorting institution roles by name

  public static Comparator<PortalRole> getSortByName() {

    if (cmSortByName == null) {
      cmSortByName = new Comparator<PortalRole>() {
        @Override
        public int compare(PortalRole r1, PortalRole r2) {
          return r1.getRoleName().compareToIgnoreCase(r2.getRoleName());
        }
      };
    }

    return cmSortByName;

  }

}
