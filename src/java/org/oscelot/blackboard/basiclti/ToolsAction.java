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
      2.0.0 29-Jan-12  Added class to process UI requests
      2.0.1 20-May-12
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12  Added options to handle content menu items
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
*/
package org.oscelot.blackboard.basiclti;

import java.util.Map;
import java.util.Iterator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spvsoftwareproducts.blackboard.utils.B2Context;


public class ToolsAction extends HttpServlet {

  private static final long serialVersionUID = -6830346901062135484L;

  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    B2Context b2Context = new B2Context(request);

    String action = b2Context.getRequestParameter(Constants.ACTION, "");
    String[] ids = request.getParameterValues(Constants.TOOL_ID);
    boolean isDomain = b2Context.getRequestParameter(Constants.DOMAIN_PARAMETER_PREFIX, "").length() > 0;

    ToolList toolList = new ToolList(b2Context, true, isDomain);

    String redirectUrl;
    String prefix;
    if (isDomain) {
      redirectUrl = "domains.jsp";
      prefix = Constants.DOMAIN_PARAMETER_PREFIX;
    } else {
      redirectUrl = "tools.jsp";
      prefix = Constants.TOOL_PARAMETER_PREFIX;
    }
    redirectUrl += "?" + Utils.getQuery(request);
    String version = B2Context.getVersionNumber("");
    boolean isv90 = version.compareTo("9.1.") < 0;

    boolean saveGlobal = false;
    boolean saveLocal = false;
    for (int i = 0; i < ids.length; i++) {
      String toolId = ids[i];
      String toolSettingPrefix = prefix + "." + toolId;
      boolean isTool = (b2Context.getSetting(toolSettingPrefix + "." + Constants.TOOL_NAME).length() > 0) ||
                       (b2Context.getSetting(false, true, toolSettingPrefix + "." + Constants.TOOL_NAME).length() > 0);
      if (isTool) {
        boolean isLocal = !isDomain && toolId.startsWith(Constants.COURSE_TOOL_PREFIX);
        if (action.equals(Constants.ACTION_ENABLE)) {
          if (!b2Context.getSetting(toolSettingPrefix, Constants.DATA_FALSE).equals(Constants.DATA_TRUE)) {
            b2Context.setSetting(toolSettingPrefix, Constants.DATA_TRUE);
            saveGlobal = true;
            if (!isDomain) {
              doMenuAvailable(b2Context, toolId, true);
            }
          }
        } else if (action.equals(Constants.ACTION_DISABLE)) {
          if (b2Context.getSetting(toolSettingPrefix, Constants.DATA_FALSE).equals(Constants.DATA_TRUE)) {
            b2Context.setSetting(toolSettingPrefix, Constants.DATA_FALSE);
            saveGlobal = true;
            if (!isDomain) {
              doMenuAvailable(b2Context, toolId, false);
              doCourseToolDelete(b2Context, toolId);
            } else {
              Utils.doCourseToolsDelete(b2Context, toolId);
            }
          }
        } else if (action.equals(Constants.ACTION_DELETE)) {
          doMenuDelete(b2Context, toolId);
          doCourseToolDelete(b2Context, toolId);
          toolList.deleteTool(toolId);
          Map<String,String> settings = b2Context.getSettings(!isLocal, true);
          for (Iterator<String> iter2 = settings.keySet().iterator(); iter2.hasNext();) {
            String setting = iter2.next();
            b2Context.setSetting(!isLocal, true, toolSettingPrefix, null);
            if (setting.startsWith(toolSettingPrefix + ".")) {
              b2Context.setSetting(!isLocal, true, setting, null);
            }
          }
          if (isLocal) {
            saveLocal = true;
          } else {
            saveGlobal = true;
          }
        } else if (action.equals(Constants.ACTION_AVAILABLE)) {
          if (!b2Context.getSetting(false, true, toolSettingPrefix, Constants.DATA_FALSE).equals(Constants.DATA_TRUE)) {
            b2Context.setSetting(false, true, toolSettingPrefix, Constants.DATA_TRUE);
            saveLocal = true;
          }
          if (b2Context.getSetting(!isLocal, true, toolSettingPrefix + "." + Constants.TOOL_EXT_OUTCOMES_COLUMN).equals(Constants.DATA_TRUE)) {
            Utils.checkColumn(b2Context, toolId, b2Context.getSetting(!isLocal, true, toolSettingPrefix + "." + Constants.TOOL_NAME),
               b2Context.getSetting(!isLocal, true, toolSettingPrefix + "." + Constants.TOOL_EXT_OUTCOMES_FORMAT, Constants.EXT_OUTCOMES_COLUMN_PERCENTAGE),
               Utils.stringToInteger(b2Context.getSetting(!isLocal, true, toolSettingPrefix + "." + Constants.TOOL_EXT_OUTCOMES_POINTS, null)), false, false, true);
          }
        } else if (action.equals(Constants.ACTION_UNAVAILABLE)) {
          if (b2Context.getSetting(false, true, toolSettingPrefix, Constants.DATA_FALSE).equals(Constants.DATA_TRUE)) {
            b2Context.setSetting(false, true, toolSettingPrefix, Constants.DATA_FALSE);
            saveLocal = true;
          }
        } else if (action.equals(Constants.ACTION_TOOL)) {
          doCourseToolAdd(b2Context, toolId);
        } else if (action.equals(Constants.ACTION_NOTOOL)) {
          doCourseToolDelete(b2Context, toolId);
        } else if (action.equals(Constants.ACTION_NOMENU)) {
          doMenuItemMenu(b2Context, toolId, null);
        } else if (!isv90 && Constants.MENU_NAME.contains(action)) {
          doMenuItemMenu(b2Context, toolId, action);
        }
      }
    }

    if (saveGlobal) {
      b2Context.persistSettings();
    }
    if (saveLocal) {
      b2Context.persistSettings(false, true);
    }
    redirectUrl = b2Context.setReceiptOptions(redirectUrl, b2Context.getResourceString("page.receipt.success"), null);
    response.sendRedirect(redirectUrl);
  }

  private void doMenuItemMenu(B2Context b2Context, String toolId, String menu) {

    Tool tool = new Tool(b2Context, toolId);
    MenuItem menuItem = tool.getMenuItem(menu != null);
    if (menuItem != null) {
      if (menu != null) {
        menuItem.setMenu(menu);
      } else {
        menuItem.delete();
      }
      menuItem.persist();
    }

  }

  private void doMenuAvailable(B2Context b2Context, String toolId, boolean isAvailable) {

    Tool tool = new Tool(b2Context, toolId);
    MenuItem menuItem = tool.getMenuItem();
    if (menuItem != null) {
      menuItem.setIsAvailable(isAvailable);
      menuItem.persist();
    }

  }

  private void doMenuDelete(B2Context b2Context, String toolId) {

    Tool tool = new Tool(b2Context, toolId);
    MenuItem menuItem = tool.getMenuItem();
    if (menuItem != null) {
      menuItem.delete();
    }

  }

  private void doCourseToolAdd(B2Context b2Context, String toolId) {

    Tool tool = new Tool(b2Context, toolId);
    CourseTool courseTool = tool.getCourseTool(true);
    if (courseTool != null) {
      courseTool.persist();
    }

  }

  private void doCourseToolDelete(B2Context b2Context, String toolId) {

    Tool tool = new Tool(b2Context, toolId);
    CourseTool courseTool = tool.getCourseTool();
    if (courseTool != null) {
      courseTool.delete();
    }

  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      processRequest(request, response);
  }

}
