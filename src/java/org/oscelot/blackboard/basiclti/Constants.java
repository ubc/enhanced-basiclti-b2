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
      1.2.0 17-Sep-11  Added constants for outcomes, memberships and setting extension services
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11  Added LTI_EXT_LMS constant
      2.0.0 29-Jan-12  Added LTI 1.1 support
      2.0.1 20-May-12
      2.1.0 18-Jun-12  Added constants for XML and updated image alt text
      2.2.0  2-Sep-12  Added constants for content page menus and cache
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12  Added constants for grade column options
*/
package org.oscelot.blackboard.basiclti;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class Constants {

  public static final String LTI_MESSAGE_TYPE = "basic-lti-launch-request";
  public static final String LTI_VERSION = "LTI-1p0";
  public static final String CUSTOM_NAME_PREFIX = "custom_";
  public static final String OAUTH_CALLBACK = "about:blank";
  public static final String LTI_MESSAGE = "lti_msg";
  public static final String LTI_LOG = "lti_log";
  public static final String LTI_ERROR_MESSAGE = "lti_errormsg";
  public static final String LTI_ERROR_LOG = "lti_errorlog";
  public static final String LTI_LMS = "learn";

// Content handler constants
  public static final String RESOURCE_HANDLE = "resource/x-osc-basiclti";
  public static final String DEFAULT_TITLE = "Untitled";

//  Systemwide setting parameter names
  public static final String CONSUMER_NAME_PARAMETER = "ConsumerName";
  public static final String CONSUMER_DESCRIPTION_PARAMETER = "ConsumerDescription";
  public static final String CONSUMER_EMAIL_PARAMETER = "ConsumerEmail";

// Default tool ID
  public static final String DEFAULT_TOOL_ID = "default";

// Tool setting parameter names
  public static final String TOOL_PARAMETER_PREFIX = "tool";
  public static final String DOMAIN_PARAMETER_PREFIX = "domain";
  public static final String TOOL_ID = "id";
  public static final String TOOL_ID_NEW = "id_new";
  public static final String TOOL_ENABLE = "enable";
  public static final String TOOL_AVAILABLE = "available";
  public static final String TOOL_NAME = "name";
  public static final String TOOL_DESCRIPTION = "description";
  public static final String TOOL_URL = "url";
  public static final String TOOL_GUID = "guid";
  public static final String TOOL_SECRET = "secret";
  public static final String TOOL_CSS = "css";
  public static final String TOOL_ICON = "icon";
  public static final String TOOL_CONTEXT_ID = "contextid";
  public static final String TOOL_CONTEXTIDTYPE = "contextidtype";
  public static final String TOOL_CONTEXT_SOURCEDID = "contextsourcedid";
  public static final String TOOL_CONTEXT_TITLE = "contexttitle";
  public static final String TOOL_AVATAR = "avatar";
  public static final String TOOL_ROLES = "roles";
  public static final String TOOL_USERID = "userid";
  public static final String TOOL_USERIDTYPE = "useridtype";
  public static final String TOOL_USER_SOURCEDID = "usersourcedid";
  public static final String TOOL_USERNAME = "username";
  public static final String TOOL_EMAIL = "email";
  public static final String TOOL_OPEN_IN = "openin";
  public static final String TOOL_WINDOW_NAME = "windowname";
  public static final String TOOL_SPLASH = "splash";
  public static final String TOOL_SPLASHFORMAT = TOOL_SPLASH + "type";
  public static final String TOOL_SPLASHTEXT = TOOL_SPLASH + "text";
  public static final String TOOL_CUSTOM = "custom";
  public static final String TOOL_ROLE = "role";
  public static final String TOOL_IROLE = "irole";
  public static final String TOOL_ADMINISTRATOR = "administrator";
  public static final String TOOL_XML = "xml";
  public static final String TOOL_EXT_OUTCOMES = "ext_outcomes";
  public static final String TOOL_EXT_OUTCOMES_COLUMN = "ext_outcomes_column";
  public static final String TOOL_EXT_OUTCOMES_FORMAT = "ext_outcomes_format";
  public static final String TOOL_EXT_OUTCOMES_POINTS = "ext_outcomes_points";
  public static final String TOOL_EXT_OUTCOMES_SCORABLE = "ext_outcomes_scorable";
  public static final String TOOL_EXT_OUTCOMES_VISIBLE = "ext_outcomes_visible";
  public static final String TOOL_EXT_MEMBERSHIPS = "ext_memberships";
  public static final String TOOL_EXT_MEMBERSHIPS_LIMIT = "ext_memberships_limit";
  public static final String TOOL_EXT_MEMBERSHIPS_GROUPS = "ext_memberships_groups";
  public static final String TOOL_EXT_MEMBERSHIPS_GROUPNAMES = "ext_memberships_groupnames";
  public static final String TOOL_EXT_SETTING = "ext_setting";
  public static final String TOOL_EXT_UUID = "ext_uuid";
  public static final String TOOL_EXT_SETTING_VALUE = "ext_settingvalue";
  public static final String TOOL_COURSE_ROLES = "courseroles";
  public static final String TOOL_INSTITUTION_ROLES = "institutionroles";
  public static final String TOOL_DELEGATE = "delegate";
  public static final String TOOL_RESOURCE_URL = "resourceurl";
  public static final String TOOL_RESOURCE_SIGNATURE = "resourcesignature";
  public static final String TOOL_LINEITEM = "lineitem";
  public static final String TOOL_MENU = "menu";
  public static final String TOOL_MENUITEM = "menuitem";
  public static final String TOOL_COURSETOOLAPP = "coursetoolapp";
  public static final String TOOL_COURSETOOL = "coursetool";
  public static final String TOOL_MODULE = "mid";
  public static final String TOOL_COURSEID = "courseid";
  public static final String TOOL_ENCRYPT_DATA = "encrypt_data";

// Cache setting parameter names
  public static final String CACHE_AGE_PARAMETER = "cacheage";
  public static final String CACHE_CAPACITY_PARAMETER = "cachecapacity";

// Mashup setting parameter name
  public static final String MASHUP_PARAMETER = "mashup";
  public static final String AVAILABILITY_PARAMETER = "availability";

// Menu actions
  public static final String ACTION = "action";
  public static final String ACTION_ENABLE = "enable";
  public static final String ACTION_DISABLE = "disable";
  public static final String ACTION_DELETE = "delete";
  public static final String ACTION_AVAILABLE = "available";
  public static final String ACTION_UNAVAILABLE = "unavailable";
  public static final String ACTION_TOOL = "tool";
  public static final String ACTION_NOTOOL = "notool";
  public static final String ACTION_NOMENU = "nomenu";

// BasicLTI XML names
  public static final String XML_ROOT = "basic_lti_link";
  public static final String XML_TITLE = "title";
  public static final String XML_DESCRIPTION = "description";
  public static final String XML_CUSTOM = "custom";
  public static final String XML_PARAMETER = "property";
  public static final String XML_PARAMETER_KEY = "name";
  public static final String XML_URL = "launch_url";
  public static final String XML_URL_SECURE = "secure_launch_url";
  public static final String XML_EXTENSION = "extensions";
  public static final String XML_EXTENSION_PLATFORM = "platform";
  public static final String XML_ICON = "icon";
  public static final String XML_ICON_SECURE = "secure_icon";

// Data privacy option values
  public static final String DATA_MANDATORY = "M";
  public static final String DATA_OPTIONAL = "O";
  public static final String DATA_NOTUSED = "N";
  public static final String DATA_FALSE = "false";
  public static final String DATA_TRUE = "true";

// User ID format option values
  public static final String DATA_BATCHUID = "B";
  public static final String DATA_USERNAME = "N";
  public static final String DATA_STUDENTID = "S";
  public static final String DATA_PRIMARYKEY = "P";
  public static final String DATA_COURSEID = "C";

// Open tool options
  public static final String DATA_FRAME = "F";
  public static final String DATA_WINDOW = "W";
  public static final String DATA_IFRAME = "I";
  public static final String DATA_FRAME_NO_BREADCRUMBS = "FNB";
  public static final String DATA_BLANK_WINDOW_NAME = "_blank";

// Tool availability options
  public static final String AVAILABILITY_DEFAULT_ON = "DefaultOn";
  public static final String AVAILABILITY_DEFAULT_OFF = "DefaultOff";
  public static final String AVAILABILITY_ALWAYS_ON = "LockedOn";
  public static final String AVAILABILITY_ALWAYS_OFF = "LockedOff";

// Role names
  public static final String ROLE_INSTRUCTOR = "Instructor";
  public static final String ROLE_CONTENT_DEVELOPER = "ContentDeveloper";
  public static final String ROLE_TEACHING_ASSISTANT = "TeachingAssistant";
  public static final String ROLE_LEARNER = "Learner";
  public static final String ROLE_MENTOR = "Mentor";
  public static final String ROLE_ADMINISTRATOR = "Administrator";
  public static final String ROLE_SYSTEM_ADMINISTRATOR = "urn:lti:sysrole:ims/lis/Administrator";

// Role IDs
  public static final String ROLE_ID_INSTRUCTOR = "I";
  public static final String ROLE_ID_CONTENT_DEVELOPER = "D";
  public static final String ROLE_ID_TEACHING_ASSISTANT = "T";
  public static final String ROLE_ID_LEARNER = "L";
  public static final String ROLE_ID_MENTOR = "M";

// Institution role names
  public static final String IROLE_FACULTY = "urn:lti:instrole:ims/lis/Faculty";
  public static final String IROLE_STAFF = "urn:lti:instrole:ims/lis/Staff";
  public static final String IROLE_STUDENT ="urn:lti:instrole:ims/lis/Student";
  public static final String IROLE_PROSPECTIVE_STUDENT = "urn:lti:instrole:ims/lis/ProspectiveStudent";
  public static final String IROLE_ALUMNI = "urn:lti:instrole:ims/lis/Alumni";
  public static final String IROLE_OBSERVER = "urn:lti:instrole:ims/lis/Observer";
  public static final String IROLE_GUEST = "urn:lti:instrole:ims/lis/Guest";
  public static final String IROLE_OTHER = "urn:lti:instrole:ims/lis/Other";

// Institution role IDs
  public static final String IROLE_ID_STUDENT = "L";
  public static final String IROLE_ID_FACULTY = "F";
  public static final String IROLE_ID_STAFF = "S";
  public static final String IROLE_ID_ALUMNI = "A";
  public static final String IROLE_ID_PROSPECTIVE_STUDENT = "P";
  public static final String IROLE_ID_GUEST = "G";
  public static final String IROLE_ID_OTHER = "Z";
  public static final String IROLE_ID_OBSERVER = "O";

// Outcomes column option values
  public static final String EXT_OUTCOMES_COLUMN_PERCENTAGE = "P";
  public static final String EXT_OUTCOMES_COLUMN_SCORE = "S";

// Extension actions names
  public static final String EXT_OUTCOMES_READ = "basic-lis-readresult";
  public static final String EXT_OUTCOMES_WRITE = "basic-lis-updateresult";
  public static final String EXT_OUTCOMES_DELETE = "basic-lis-deleteresult";
  public static final String EXT_MEMBERSHIPS_READ = "basic-lis-readmembershipsforcontext";
  public static final String EXT_MEMBERSHIP_GROUPS_READ = "basic-lis-readmembershipsforcontextwithgroups";
  public static final String EXT_SETTING_READ = "basic-lti-loadsetting";
  public static final String EXT_SETTING_WRITE = "basic-lti-savesetting";
  public static final String EXT_SETTING_DELETE = "basic-lti-deletesetting";

// Module setting parameter names
  public static final String MODULE_TOOL_ID = "id";
  public static final String MODULE_CONTENT_URL = "url";
  public static final String MODULE_CONTENT_TYPE = "type";
  public static final String MODULE_NO_DATA = "nodata";
  public static final String MODULE_AUTO_OPEN = "autoopen";
  public static final String MODULE_LAUNCH = "launch";
  public static final String MODULE_LAUNCH_BUTTON = "launchbutton";
  public static final String MODULE_MODULE_ID = "modId";

// Module content type option values
  public static final String CONTENT_TYPE_RSS = "rss";
  public static final String CONTENT_TYPE_ATOM = "atom";
  public static final String CONTENT_TYPE_HTML = "html";

// Service action names
  public static final String SVC_OUTCOME_READ = "readResult";
  public static final String SVC_OUTCOME_WRITE = "replaceResult";
  public static final String SVC_OUTCOME_DELETE = "deleteResult";

// Outcomes service result types
  public static final String DECIMAL_RESULT_TYPE = "decimal";
  public static final String PERCENTAGE_RESULT_TYPE = "percentage";
  public static final String RATIO_RESULT_TYPE = "ratio";
  public static final String LETTERAF_RESULT_TYPE = "letteraf";
  public static final String LETTERAFPLUS_RESULT_TYPE = "letterafplus";
  public static final String PASSFAIL_RESULT_TYPE = "passfail";
  public static final String FREETEXT_RESULT_TYPE = "freetext";

// VTBE link parameter  names
  public static final String LINK_TEXT = "text";
  public static final String LINK_TITLE = "title";

// Other constants
  public static final String COURSE_TOOL_PREFIX = "!";
  public static final String CUSTOM_PARAMETER = "Custom";
  public static final String COLUMN_PREFIX = "BLTI";
  public static final String HASH_SEPARATOR = ":";
  public static final int SETTING_MAX_LENGTH = 2048;
  public static final String LOCALE_ATTRIBUTE = "browser.session.locale";
  public static final String DATE_FORMAT = "d-MMM-yyyy HH:mm";
  public static final String GROUPS_PARAMETER_NAME = "groups";
  public static final String PAGE_PARAMETER_NAME = "lti_page";
  public static final String COURSE_TOOLS_PAGE = "ctools";
  public static final String TOOLS_PAGE = "tools";
  public static final String CONTENT_PAGE = "content";
  public static final String WYSIWYG_WEBAPP = "/webapps/wysiwyg";
  public static final String XSL_FILE_EXTENSION = ".xsl";
  public static final int TIMEOUT = 30000;
  public static final String CACHE_OPTION = "0";
  public static final String TAB_PARAMETER_NAME = "tab_tab_group_id";
  public static final String DEFAULT_POINTS_POSSIBLE = "100";

// Names of content item menu areas
  public static final String MENU_COLLABORATE = "collaborate";
  public static final String MENU_EVALUATE = "evaluate";
  public static final String MENU_MASHUP = "mashup";
  public static final String MENU_CREATE_ITEM = "createItem";
  public static final String MENU_CREATE_MEDIA = "createMedia";
  public static final String MENU_CREATE_OTHER = "createOther";
  public static final String MENU_NEW_PAGE = "newPage";
  public static final String MENU_TEXTBOOK = "textbook";

  public static final List<String> MENU_NAME = new ArrayList<String>() {{
    add(MENU_COLLABORATE);
    add(MENU_EVALUATE);
    add(MENU_MASHUP);
    add(MENU_CREATE_ITEM);
    add(MENU_CREATE_MEDIA);
    add(MENU_CREATE_OTHER);
    add(MENU_NEW_PAGE);
    add(MENU_TEXTBOOK);
  }};

// location of image files
  public static Map<String,String> IMAGE_FILE = new HashMap<String,String>() {{
    put(DATA_MANDATORY, "/images/ci/icons/checkmark_ia.gif");
    put(DATA_OPTIONAL, "/images/ci/icons/task_ia.gif");
    put(DATA_NOTUSED, "/images/ci/icons/x_ia.gif");
    put(DATA_TRUE, "/images/ci/icons/checkmark_ia.gif");
    put(DATA_FALSE, "/images/ci/icons/x_ia.gif");
  }};

// name of resource string for alt text of image files
  private static final String IMAGE_ALT_RESOURCE_PREFIX = "image.alt.";
  public static Map<String,String> IMAGE_ALT_RESOURCE = new HashMap<String,String>() {{
    put(DATA_MANDATORY, IMAGE_ALT_RESOURCE_PREFIX + DATA_MANDATORY);
    put(DATA_OPTIONAL, IMAGE_ALT_RESOURCE_PREFIX + DATA_OPTIONAL);
    put(DATA_NOTUSED, IMAGE_ALT_RESOURCE_PREFIX + DATA_NOTUSED);
    put(DATA_TRUE, IMAGE_ALT_RESOURCE_PREFIX + DATA_TRUE);
    put(DATA_FALSE, IMAGE_ALT_RESOURCE_PREFIX + DATA_FALSE);
    put("enable." + DATA_TRUE, IMAGE_ALT_RESOURCE_PREFIX + "enable." + DATA_TRUE);
    put("enable." + DATA_FALSE, IMAGE_ALT_RESOURCE_PREFIX + "enable." + DATA_FALSE);
    put(TOOL_PARAMETER_PREFIX + "." + DATA_TRUE, IMAGE_ALT_RESOURCE_PREFIX + TOOL_PARAMETER_PREFIX + "." + DATA_TRUE);
    put(TOOL_PARAMETER_PREFIX + "." + DATA_FALSE, IMAGE_ALT_RESOURCE_PREFIX + TOOL_PARAMETER_PREFIX + "." + DATA_FALSE);
    put(DOMAIN_PARAMETER_PREFIX + "." + DATA_TRUE, IMAGE_ALT_RESOURCE_PREFIX + DOMAIN_PARAMETER_PREFIX + "." + DATA_TRUE);
    put(DOMAIN_PARAMETER_PREFIX + "." + DATA_FALSE, IMAGE_ALT_RESOURCE_PREFIX + DOMAIN_PARAMETER_PREFIX + "." + DATA_FALSE);
    put(COURSE_TOOL_PREFIX + TOOL_PARAMETER_PREFIX + "." + DATA_TRUE,
       IMAGE_ALT_RESOURCE_PREFIX + COURSE_TOOL_PREFIX + TOOL_PARAMETER_PREFIX + "." + DATA_TRUE);
    put(COURSE_TOOL_PREFIX + TOOL_PARAMETER_PREFIX + "." + DATA_FALSE,
       IMAGE_ALT_RESOURCE_PREFIX + COURSE_TOOL_PREFIX + TOOL_PARAMETER_PREFIX + "." + DATA_FALSE);
  }};

}
