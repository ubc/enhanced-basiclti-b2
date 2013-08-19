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
      2.1.0 18-Jun-12  Added to release
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12  Added support for new Content Editor in SP10
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.util.Iterator,
                blackboard.platform.plugin.PlugInUtil,
                com.spvsoftwareproducts.blackboard.utils.B2Context,
                org.oscelot.blackboard.basiclti.Constants,
                org.oscelot.blackboard.basiclti.ToolList,
                org.oscelot.blackboard.basiclti.Tool,
                org.oscelot.blackboard.basiclti.Utils"
        errorPage="../error.jsp"%>
<%@taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:genericPage title="${bundle['page.content.create.title']}" bodyClass="popup" onLoad="doOnLoad()">
<%
  B2Context b2Context = new B2Context(request);
  b2Context.setIgnoreContentContext(true);
  ToolList toolList = new ToolList(b2Context);

  String courseIdParamName = "course_id";
  String courseId = b2Context.getRequestParameter(courseIdParamName, "");
  String toolId = b2Context.getRequestParameter(Constants.TOOL_ID, "");

  String path = b2Context.getPath() + "tool.jsp";
  String query = courseIdParamName + "=@X@course.pk_string@X@&content_id=@X@content.pk_string@X@&" +
     Constants.TOOL_ID + "=";

  if (request.getMethod().equalsIgnoreCase("POST")) {
    Tool tool = new Tool(b2Context, toolId, false);
    String text = b2Context.getRequestParameter(Constants.LINK_TEXT, "");
    if (text.length() <= 0) {
      text = tool.getName();
    }
    String title = b2Context.getRequestParameter(Constants.LINK_TITLE, "");
    if (title.length() <= 0) {
      title = text;
    }
    String url = path + "?" + query + toolId;
    String embedHtml = "<a href=\"" + url + "\" title=\"" + title + "\">" + text + "</a>";
    request.setAttribute("embedHtml", embedHtml);
    String embedUrl = PlugInUtil.getInsertToVtbePostUrl().replace(Constants.WYSIWYG_WEBAPP, "");
    RequestDispatcher rd = getServletContext().getContext(Constants.WYSIWYG_WEBAPP).getRequestDispatcher(embedUrl);
    rd.forward(request, response);
  }

  pageContext.setAttribute("bundle", b2Context.getResourceStrings());
  pageContext.setAttribute("courseId", courseId);
  pageContext.setAttribute("serverUrl", b2Context.getServerUrl());
  pageContext.setAttribute("serverPath", path);
  pageContext.setAttribute("serverQuery", query);
%>
  <bbNG:pageHeader instructions="${bundle['page.content.create.instructions']}">
    <bbNG:pageTitleBar iconUrl="../images/lti.gif" title="${bundle['page.content.create.title']}" />
  </bbNG:pageHeader>
  <bbNG:jsBlock>
<script language="javascript" type="text/javascript">
var lti_url = '${serverUrl}';
var lti_path = '${serverPath}';
var lti_query = '${serverQuery}';
var lti_sel;
var lti_sel_index = -1;
var lti_el;
var lti_el_text;
var lti_el_title;

function getParamValue(url, paramName){
  var paramValue = '';
  var pos = url.indexOf('?');
  if (pos >= 0) {
    var query = url.substr(pos) + '&';
    var regex = new RegExp('.*?[&\\?]' + paramName + '=(.*?)&.*');
    var value = query.replace(regex, "$1");
    if (value != query) {
      paramValue = value;
    }
  }
  return paramValue;
}

function setOption(value) {
  for (var i=0; i < lti_sel.options.length; i++) {
    if (lti_sel.options[i].value == value) {
      lti_sel.selectedIndex = i;
      lti_sel_index = i;
      break;
    }
  }
  if ((lti_sel_index < 0) && (lti_sel.options.length > 0)) {
    lti_sel.selectedIndex = 0;
  }
  doOnSelChange();
  lti_sel._defaultValue = lti_sel.selectedIndex;
}

function doOnLoad() {
  var ok = true;
  var editor;
  lti_sel = document.getElementById('id_tool');
  var isVTBE = (typeof window.opener.currentVTBE != 'undefined');
  if (isVTBE) {
    editor = window.opener.editors[window.opener.currentVTBE];
    lti_el = editor.getParentElement();
  } else if (typeof window.opener.tinyMceWrapper.currentEditorId != 'undefined') {
    editor = window.opener.tinyMceWrapper.editors.get(window.opener.tinyMceWrapper.currentEditorId).getTinyMceEditor();
    lti_el = editor.selection.getNode();
  } else {
    ok = false;
  }
  if (ok) {
    lti_el_text = document.getElementById('id_text');
    lti_el_title = document.getElementById('id_title');
    var isLink = /^a$/i.test(lti_el.tagName);
    if (isLink) {
      if (!isVTBE) {
        editor.selection.select(lti_el);
      } else if (editor.selectNodeContents) {
        editor.selectNodeContents(lti_el);
      }
      self.focus();
      lti_el_text.value = lti_el.innerHTML;
      lti_el_title.value = lti_el.title;
      var isLTI = (lti_el.href.indexOf(lti_url + lti_path) == 0);
      if (isLTI) {
        var toolId = getParamValue(lti_el.href, 'id');
        if (toolId.length > 0) {
          setOption(toolId);
        }
      }
    } else {
      lti_el = null;
      if (isVTBE) {
        lti_el_text.value = editor.getSelectedHTML();
      } else {
        lti_el_text.value = editor.selection.getContent({format:'text'});
      }
      if (lti_el_text.value.toLowerCase() == '<p>&nbsp;</p>') {
        lti_el_text.value = '';
      }
      lti_el_title.value = '';
    }
    lti_el_text._defaultValue = lti_el_text.value;
    lti_el_title._defaultValue = lti_el_title.value;
  }
}

function doOnSelChange() {
  if (lti_sel_index >= 0) {
    if ((lti_el_text.value == lti_sel.options[lti_sel_index].innerHTML) &&
        (lti_el_text.value != lti_sel.options[lti_sel.selectedIndex].innerHTML)) {
      lti_el_text.value = lti_sel.options[lti_sel.selectedIndex].innerHTML;
      widget.ShowUnsavedChanges.onValueChange(null, lti_el_text);
    }
    if ((lti_el_title.value == lti_sel.options[lti_sel_index].innerHTML) &&
        (lti_el_title.value != lti_sel.options[lti_sel.selectedIndex].innerHTML)) {
      lti_el_title.value = lti_sel.options[lti_sel.selectedIndex].innerHTML;
      widget.ShowUnsavedChanges.onValueChange(null, lti_el_title);
    }
  }
  lti_sel_index = lti_sel.selectedIndex;
}

function doOnSubmit() {
  var ok = validateForm();
  if (ok) {
    if (lti_el) {
      lti_el.href = lti_path + '?' + lti_query + lti_sel.options[lti_sel.selectedIndex].value;
      if (lti_el_text.value.length > 0) {
        lti_el.innerHTML = lti_el_text.value;
      } else {
        lti_el.innerHTML = lti_sel.options[lti_sel.selectedIndex].innerHTML;
      }
      if (lti_el_title.value.length > 0) {
        lti_el.title = lti_el_title.value;
      } else {
        lti_el.title = lti_sel.options[lti_sel.selectedIndex].innerHTML;
      }
      lti_el.target = '';
      self.close();
      ok = false;
    }
  }
  return ok;
}
</script>
  </bbNG:jsBlock>
  <bbNG:form action="link.jsp?course_id=${courseId}" method="post" onsubmit="return doOnSubmit();">
  <bbNG:dataCollection markUnsavedChanges="true" showSubmitButtons="true">
    <bbNG:step hideNumber="false" title="${bundle['page.content.create.step1.title']}" instructions="${bundle['page.content.create.step1.instructions']}">
      <bbNG:dataElement isRequired="true" label="${bundle['page.content.create.step1.tools.label']}">
        <bbNG:selectElement id="id_tool" name="<%=Constants.TOOL_ID%>" helpText="${bundle['page.content.create.step1.tools.instructions']}" onchange="doOnSelChange();">
<%
      for (Iterator<Tool> iter=toolList.getList().iterator(); iter.hasNext();) {
        Tool atool = iter.next();
        String atoolId = atool.getId();
        String atoolName = atool.getName();
%>
          <bbNG:selectOptionElement value="<%=atoolId%>" optionLabel="<%=atoolName%>" />
<%
      }
%>
        </bbNG:selectElement>
      </bbNG:dataElement>
    </bbNG:step>
    <bbNG:step hideNumber="false" title="${bundle['page.vtbe.create.step2.title']}" instructions="${bundle['page.vtbe.create.step2.instructions']}">
      <bbNG:dataElement isRequired="false" label="${bundle['page.vtbe.create.step2.text.label']}">
        <bbNG:textElement type="string" id="id_text" name="<%=Constants.LINK_TEXT%>" value="" size="40" helpText="${bundle['page.vtbe.create.step2.text.instructions']}" />
      </bbNG:dataElement>
      <bbNG:dataElement isRequired="false" label="${bundle['page.vtbe.create.step2.title.label']}">
        <bbNG:textElement type="string" id="id_title" name="<%=Constants.LINK_TITLE%>" value="" size="40" helpText="${bundle['page.vtbe.create.step2.title.instructions']}" />
      </bbNG:dataElement>
    </bbNG:step>
    <bbNG:stepSubmit hideNumber="false" showCancelButton="true" cancelOnClick="self.close();" />
  </bbNG:dataCollection>
  </bbNG:form>
</bbNG:genericPage>
