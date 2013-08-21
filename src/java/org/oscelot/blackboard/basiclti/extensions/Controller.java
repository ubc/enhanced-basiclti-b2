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
      2.0.0 29-Jan-12
      2.0.1 20-May-12
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12  Added group membership option
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
*/
package org.oscelot.blackboard.basiclti.extensions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.data.course.Course;
import blackboard.data.content.Content;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;

import java.util.List;
import java.util.ArrayList;

import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;

import com.spvsoftwareproducts.blackboard.utils.B2Context;
import org.oscelot.blackboard.basiclti.Tool;
import org.oscelot.blackboard.basiclti.Constants;
import org.oscelot.blackboard.basiclti.Utils;


public class Controller extends HttpServlet {

  private static final long serialVersionUID = 8851746207424719168L;

  private B2Context b2Context = null;
  private Response response = null;
  private List<String> servicesData = null;
  private Tool tool = null;

  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    this.b2Context = new B2Context(request);
    this.response = new Response();
    this.response.setCodeMinor(b2Context.getResourceString("ext.codeminor.request"));

    String consumerKey = this.b2Context.getRequestParameter("oauth_consumer_key", null);
    String version = this.b2Context.getRequestParameter("lti_version", null);
    String actionName = this.b2Context.getRequestParameter("lti_message_type", "");
    this.response.setAction(actionName);

    boolean ok = (consumerKey != null) && (version != null) && (actionName.length() > 0);
    Action action = null;
    String paramName = null;
    if (ok) {
      if (actionName.equals(Constants.EXT_OUTCOMES_READ) ||
          actionName.equals(Constants.EXT_OUTCOMES_WRITE) ||
          actionName.equals(Constants.EXT_OUTCOMES_DELETE)) {
        action = new Outcomes();
        paramName = "sourcedid";
      } else if (actionName.equals(Constants.EXT_MEMBERSHIPS_READ) ||
                 actionName.equals(Constants.EXT_MEMBERSHIP_GROUPS_READ)) {
        action = new Memberships();
        paramName = "id";
      } else if (actionName.equals(Constants.EXT_SETTING_READ) ||
                 actionName.equals(Constants.EXT_SETTING_WRITE) ||
                 actionName.equals(Constants.EXT_SETTING_DELETE)) {
        action = new Setting();
        paramName = "id";
      }
      ok = (action != null);
      if (!ok) {
        this.response.setCodeMinor(b2Context.getResourceString("ext.codeminor.action"));
      }
    }
    if (ok) {
      ok = getServicesData(consumerKey, paramName);
      if (!ok) {
        this.response.setCodeMinor(b2Context.getResourceString("ext.codeminor.security"));
      }
    }
    if (ok) {
      ok = checkSignature(consumerKey);
      if (!ok) {
        this.response.setCodeMinor(b2Context.getResourceString("ext.codeminor.signature"));
      }
    }
    if (ok) {
      ok = action.execute(actionName, this.b2Context, this.tool, this.servicesData, this.response);
      if (!ok) {
        this.response.setCodeMinor(b2Context.getResourceString("ext.codeminor.action"));
      }
    }

    this.response.setOk(ok);
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().print(this.response.toXML());

  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  public String getServletInfo() {
    return "Extension services";
  }

  public static Context initContext(String course, String content) {

    Context ctx = ContextManagerFactory.getInstance().getContext();
    Id vhId = Id.UNSET_ID;
    Id courseId = Id.UNSET_ID;
    Id contentId = Id.UNSET_ID;
    try {
      vhId = ctx.getVirtualHost().getId();
      if (course != null) {
        courseId = Id.generateId(Course.DATA_TYPE, course);
      }
      if (content != null) {
        contentId = Id.generateId(Content.DATA_TYPE, content);
      }
    } catch (PersistenceException e) {
    }

    return ContextManagerFactory.getInstance().setContext(vhId, courseId, Id.UNSET_ID,
       Id.UNSET_ID, contentId);

  }

  public boolean getServicesData(String key, String paramName) {

    String param = this.b2Context.getRequestParameter(paramName, "");
    String[] data = param.split(Constants.HASH_SEPARATOR);

    boolean ok = data.length >= 4;

    String courseId = null;
    String contentId = null;
    String toolId = null;
    if (ok) {
      courseId = data[1];
      if (data[2].length() > 0) {
        contentId = data[2];
      }
      toolId = data[3];
      ok = (courseId.length() > 0);
    }
    if (ok) {
      this.b2Context.setContext(Controller.initContext(courseId, contentId));
      this.tool = new Tool(this.b2Context, toolId);
      ok = key.equals(tool.getLaunchGUID());
    }
    if (ok) {
      this.servicesData = new ArrayList<String>();
      StringBuilder hash = new StringBuilder();
      for (int i = 1; i < data.length; i++) {
        String item = Utils.decodeHash(data[i]);
        this.servicesData.add(item);
        hash.append(item);
      }
      ok = Utils.getHash(hash.toString(), tool.getSendUUID()).equals(Utils.decodeHash(data[0]));
    }

    return ok;

  }

  private boolean checkSignature(String consumerKey) {

    boolean ok = true;

    String secret = tool.getLaunchSecret();
    OAuthConsumer oAuthConsumer = new OAuthConsumer(Constants.OAUTH_CALLBACK, consumerKey, secret, null);
    OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
    OAuthValidator validator = new SimpleOAuthValidator();
    OAuthMessage message = OAuthServlet.getMessage(this.b2Context.getRequest(), null);
    try {
      validator.validateMessage(message, oAuthAccessor);
    } catch (Exception e) {
      this.response.setCodeMinor(this.b2Context.getResourceString("ext.codeminor.signature"));
      ok = false;
    }

    return ok;

  }

}
