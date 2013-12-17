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
      1.0.0  9-Feb-10  First public release
      1.1.0  2-Aug-10  Renamed class domain to org.oscelot
      1.1.1  7-Aug-10
      1.1.2  9-Oct-10
      1.1.3  1-Jan-11
      1.2.0 17-Sep-11
      1.2.1 10-Oct-11
      1.2.2 13-Oct-11
      1.2.3 14-Oct-11
      2.0.0 29-Jan-12
      2.0.1 20-May-12
      2.1.0 18-Jun-12
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
      3.0.0 30-Oct-13
*/
package org.oscelot.blackboard.lti;

import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spvsoftwareproducts.blackboard.utils.B2Context;


public class Reorder extends HttpServlet {

  private static final long serialVersionUID = 9186415984234279935L;

  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Map<String,String> resultMap = new HashMap<String,String>();
    B2Context b2Context = new B2Context(request);
    String toolId = request.getParameter("dnd_itemId");
    String newPosition = request.getParameter("dnd_newPosition");
    String timestamp = request.getParameter("dnd_timestamp");
    String newOrder[] = request.getParameterValues("dnd_newOrder");
    boolean isDomain = request.getServletPath().endsWith("domains");
    ToolList toolList = new ToolList(b2Context, true, isDomain);
    if ((toolId != null) && (newPosition != null) && (timestamp != null)) {
      toolList.reorder(toolId, Integer.parseInt(newPosition));
      resultMap.put("success", "true");
    } else if ((newOrder != null) && (newOrder.length > 0) && (timestamp != null)) {
      toolList.reorder(newOrder);
      resultMap.put("success", "true");
    } else {
      resultMap.put("success", "false");
      resultMap.put("error", "error");
      resultMap.put("errorMessage", "Unknown drag and drop error");
    }
    resultMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
    response.setContentType("text/x-json");
    response.setCharacterEncoding("UTF-8");
    GsonBuilder gb = new GsonBuilder();
    Gson gson = gb.create();
    response.getWriter().print(gson.toJson(resultMap));

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
    return "Reorder tools";
  }

}
