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
      2.3.1 17-Dec-12  Added to release
      2.3.2  3-Apr-13
--%>
<%@page import="org.oscelot.blackboard.basiclti.Constants"%>
<%@page import="org.oscelot.blackboard.basiclti.Utils"%>
<%@page contentType="application/json" pageEncoding="UTF-8"
        import="java.io.BufferedReader,
                net.sf.json.JSONObject,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.basiclti.Tool"
        errorPage="../error.jsp"%>
<%
  B2Context b2Context = new B2Context(request);

  JSONObject result = new JSONObject();

  BufferedReader reader = request.getReader();
  StringBuilder data = new StringBuilder();
  String line;
  while ((line = reader.readLine()) != null) {
    data.append(line);
  }
  JSONObject json = JSONObject.fromString(data.toString());

  String url = json.getString("url");

  boolean createColumn = false;
  Tool domain = Utils.urlToDomain(b2Context, url);
  if (domain != null) {
    createColumn = domain.getOutcomesService().equals(Constants.DATA_MANDATORY) &&
       domain.getOutcomesColumn().equals(Constants.DATA_TRUE);
    if (createColumn) {
      result.put("domain", domain.getName());
      if (domain.getOutcomesFormat().equals(Constants.EXT_OUTCOMES_COLUMN_SCORE)) {
        result.put("format", 1);
      } else {
        result.put("format", 0);
      }
      result.put("points", domain.getOutcomesPointsPossible());
      result.put("scorable", domain.getOutcomesScorable().equals(Constants.DATA_TRUE));
      result.put("visible", domain.getOutcomesVisible().equals(Constants.DATA_TRUE));
    }
  }

  result.put("createColumn", createColumn);
  response.getWriter().print(result.toString());
%>
