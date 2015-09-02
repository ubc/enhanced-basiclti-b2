/*
    basiclti - Building Block to provide support for Basic LTI
    Copyright (C) 2014  Stephen P Vickers

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
*/
package org.oscelot.blackboard.basiclti.services;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.ubc.ctlt.encryption.Encryption;
import org.jdom.Document;
import org.jdom.Element;

import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.OAuthException;

import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.data.course.Course;
import blackboard.data.content.Content;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;

import com.spvsoftwareproducts.blackboard.utils.B2Context;
import org.oscelot.blackboard.lti.Tool;
import org.oscelot.blackboard.lti.Constants;
import org.oscelot.blackboard.lti.Utils;


public class Controller extends HttpServlet {

  private static final long serialVersionUID = 4319979518963733963L;

  private B2Context b2Context = null;
  private Response response = null;
  private List<String> servicesData = null;
  private Tool tool = null;

  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    boolean ok;
    this.b2Context = new B2Context(request);
    this.response = new Response();

    this.response.setProviderRef("");
    this.response.setConsumerRef(String.valueOf(System.currentTimeMillis()));
    String description = "ext.codeminor.request";
    OAuthMessage message = OAuthServlet.getMessage(request, null);
    Map<String,String> authHeaders = Utils.getAuthorizationHeaders(message);
    String consumerKey = authHeaders.get("oauth_consumer_key");

    String xml = message.readBodyAsString();
    String actionName = null;
    Document xmlDoc;
    Element xmlBody = null;
    xmlDoc = Utils.getXMLDoc(xml);
    ok = xmlDoc != null;
    if (ok) {
      Element el = Utils.getXmlChild(xmlDoc.getRootElement(), "imsx_POXBody");
      xmlBody = Utils.getXmlChild(el, null);
      ok = xmlBody != null;
    }
    if (ok) {
      actionName = xmlBody.getName();
      if (actionName.endsWith("Request")) {
        actionName = actionName.substring(0, actionName.length() - 7);
      }
      this.response.setProviderRef(Utils.getXmlChildValue(xmlDoc.getRootElement(), "imsx_messageIdentifier"));
    } else if (actionName == null) {
      actionName = "";
    }
    this.response.setAction(actionName);
    Action action = null;
    String paramName = null;
    if (ok) {
      if (actionName.equals(Constants.SVC_OUTCOME_READ) ||
          actionName.equals(Constants.SVC_OUTCOME_WRITE) ||
          actionName.equals(Constants.SVC_OUTCOME_DELETE)) {
        action = new Outcome();
        paramName = "sourcedId";
      }
      ok = (action != null);
      if (!ok) {
        this.response.setCodeMajor("unsupported");
        description = "ext.codeminor.action";
      }
    }
    if (ok) {
      ok = getServicesData(consumerKey, Utils.getXmlChildValue(xmlBody, paramName));
      if (!ok) {
        description = "ext.codeminor.security";
      }
    }
    if (ok) {
      ok = checkSignature(message);
      if (!ok) {
        description = "ext.codeminor.signature";
      }
    }
    if (ok) {
      ok = Utils.checkBodyHash(message.getAuthorizationHeader(null), xml);
      if (!ok) {
        description = "svc.codeminor.bodyhash";
      }
    }
    this.response.setDescription(this.b2Context.getResourceString(description));
    if (ok) {
      ok = action.execute(actionName, this.b2Context, this.tool, xmlBody, this.servicesData, this.response);
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

  private Context initContext(String course, String content) {

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

  private boolean getServicesData(String key, String param) {

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
      this.b2Context.setContext(initContext(courseId, contentId));
      this.tool = new Tool(this.b2Context, toolId);
      ok = key.equals(this.tool.getLaunchGUID());
    }
    if (ok) {
      Encryption encryptInstance = new Encryption(tool.getEncryptKey());
      this.servicesData = new ArrayList<String>();
      StringBuilder hash = new StringBuilder();
      for (int i = 1; i < data.length; i++) {
        String item = Utils.decodeHash(data[i]);
        hash.append(item);
        if (tool.isEncryptData() && i == 4) {
          // using default IV to decrypt user ID, which is on the 4th position of data
          item = encryptInstance.decrypt(item);
        }
        this.servicesData.add(item);
      }
      ok = Utils.getHash(hash.toString(), this.tool.getSendUUID()).equals(Utils.decodeHash(data[0]));
    }

    return ok;

  }

  private boolean checkSignature(OAuthMessage message) {

    boolean ok = true;

    String consumerKey = this.tool.getLaunchGUID();
    String secret = this.tool.getLaunchSecret();

    OAuthConsumer oAuthConsumer = new OAuthConsumer(Constants.OAUTH_CALLBACK, consumerKey, secret, null);
    OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
    OAuthValidator validator = new SimpleOAuthValidator();
    try {
    	validator.validateMessage(message, oAuthAccessor);
    } catch (IOException e) {
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, "checkSignature error for " + consumerKey + "/" + secret, e);
      ok = false;
    } catch (URISyntaxException e) {
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, "checkSignature error for " + consumerKey + "/" + secret, e);
      ok = false;
    } catch (OAuthException e) {
      Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, "checkSignature error for " + consumerKey + "/" + secret, e);
      ok = false;
    }

    return ok;

  }

}
