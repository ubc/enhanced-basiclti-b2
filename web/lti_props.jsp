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
      1.1.2  9-Oct-10  Split connection to tool code according to where it is to be opened
                       Added support for resource_link_title, resource_link_description,
                          tool_consumer_instance_url, tool_consumer_instance_description and
                          launch_presentation_document_target parameters
                       Corrected name of lis_person_sourcedid parameter
                       Improved exception handling when redirecting to a tool producer
      1.1.3  1-Jan-11  Added User ID type option
      1.2.0 17-Sep-11  Added support for outcomes, memberships and setting extension services
      1.2.1 10-Oct-11  Added custom parameters option for tool instances
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11  Added EXT_LMS parameter
      2.0.0 29-Jan-12  Significant update to user interface
                       Added lis_course_section_sourcedid parameter
                       Added support for LTI 1.1
                       Changed format of URL and sourcedids for extension services
      2.0.1 20-May-12  Added return to control panel tools page (including paging option)
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12  Moved generation of launch parameters to a Java class
      2.3.0  5-Nov-12  Added support for launching from a module outside a course
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
      3.0.0 30-Oct-13
--%>
<%@page import="java.util.Map,
                java.util.List,
                blackboard.portal.data.Module,
                blackboard.portal.persist.ModuleDbLoader,
                blackboard.persist.Id,
                blackboard.persist.KeyNotFoundException,
                blackboard.persist.PersistenceException,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.lti.Constants,
                org.oscelot.blackboard.lti.Tool,
                org.oscelot.blackboard.lti.Utils,
                org.oscelot.blackboard.lti.LaunchMessage"%>
<%
  String moduleId = Utils.checkForModule(request);
  Module module = Utils.getModule(moduleId);
  B2Context b2Context = new B2Context(request);
  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  String toolId = b2Context.getSetting(false, true,
     Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID,
     b2Context.getRequestParameter(Constants.TOOL_ID, ""));
  String idString = "";
  Tool tool = new Tool(b2Context, toolId);
  if (tool.getName().length() <= 0) {
    idString = toolId;
    toolId = b2Context.getSetting(false, true, Constants.TOOL_ID + "." + idString + "." + Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_ID, "");
    tool = new Tool(b2Context, toolId);
  }
  String toolURL = tool.getLaunchUrl();
  LaunchMessage message = new LaunchMessage(b2Context, toolId, idString, module);
  message.signParameters(toolURL, tool.getLaunchGUID(), tool.getLaunchSecret());
  List<Map.Entry<String, String>> params = message.getParams();
%>
