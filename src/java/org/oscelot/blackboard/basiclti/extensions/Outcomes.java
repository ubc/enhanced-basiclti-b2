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
      2.2.0  2-Sep-12
      2.3.0  5-Nov-12
      2.3.1 17-Dec-12
      2.3.2  3-Apr-13
*/
package org.oscelot.blackboard.basiclti.extensions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.oscelot.blackboard.basiclti.Constants;
import org.oscelot.blackboard.basiclti.Gradebook;
import org.oscelot.blackboard.basiclti.Gradebook_v90;
import org.oscelot.blackboard.basiclti.Tool;
import org.oscelot.blackboard.basiclti.Utils;

import blackboard.data.course.CourseMembership;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;
import blackboard.data.gradebook.impl.Outcome.GradebookStatus;
import blackboard.data.user.User;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.SearchOperator;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.persist.user.UserSearch;
import blackboard.persist.user.UserSearch.SearchKey;
import blackboard.persist.user.UserSearch.SearchParameter;
import blackboard.platform.persistence.PersistenceServiceFactory;
import ca.ubc.ctlt.encryption.Encryption;

import com.spvsoftwareproducts.blackboard.utils.B2Context;


public class Outcomes implements Action {

  public Outcomes() {
  }

  public boolean execute(String actionName, B2Context b2Context, Tool tool, List<String> serviceData,
     Response response) {

    boolean ok = true;
    Encryption encryptInstance = new Encryption();
    String[] version = B2Context.getVersionNumber("?.?.?").split("\\.");
    boolean isV90 = (version[0].equals("9") && version[1].equals("0"));

    String sourcedid = b2Context.getRequestParameter("sourcedid", "");

    String codeMinor = "ext.codeminor.success";
    String description = null;

    if (ok) {
      ok = tool.getSendOutcomesService().equals(Constants.DATA_TRUE) &&
           tool.getSendUserId().equals(Constants.DATA_MANDATORY);
      if (!ok) {
        codeMinor = "ext.codeminor.notavailable";
      }
    }
    BbPersistenceManager bbPm = null;
// Load user object
    User user = null;
    if (ok) {
      bbPm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
      String userId = serviceData.get(3);
      // decrypt the user if necessary
		if (tool.isEncryptData()) {
			try {
				userId = encryptInstance.decrypt(userId);
			} catch (Exception e1) {
			}
		}
      try {
        UserDbLoader userdbloader = (UserDbLoader)bbPm.getLoader(UserDbLoader.TYPE);
        String userIdType = tool.getUserIdType();
        if (userIdType.equals(Constants.DATA_USERNAME)) {
          user = userdbloader.loadByUserName(userId);
        } else if (userIdType.equals(Constants.DATA_PRIMARYKEY)) {
          user = userdbloader.loadById(bbPm.generateId(User.DATA_TYPE, userId));
        } else if (userIdType.equals(Constants.DATA_STUDENTID)) {
          SearchParameter sp = new SearchParameter(SearchKey.StudentId, userId, SearchOperator.Equals);
          UserSearch us = new UserSearch();
          us.addParameter(sp);
          List<User> users = userdbloader.loadByUserSearch(us);
          if (users.size() > 0) {
            user = users.get(0);
          }
        } else if (userIdType.equals(Constants.DATA_BATCHUID)) {
          user = userdbloader.loadByBatchUid(userId);
        }
        ok = (user != null);
        if (ok) {
          ok = user.getIsAvailable();
        }
        if (!ok) {
          codeMinor = "ext.codeminor.user";
        }
      } catch (PersistenceException e) {
        ok = false;
        codeMinor = "ext.codeminor.system";
      }
    }
// Load enrolment
    if (ok) {
      CourseMembership coursemembership = null;
      try {
        CourseMembershipDbLoader coursemembershipdbloader = (CourseMembershipDbLoader)bbPm.getLoader(CourseMembershipDbLoader.TYPE);
        coursemembership = coursemembershipdbloader.loadByCourseAndUserId(b2Context.getContext().getCourseId(), user.getId());
        ok = coursemembership.getRole().equals(CourseMembership.Role.STUDENT) && coursemembership.getIsAvailable();
        if (!ok) {
          codeMinor = "ext.codeminor.role";
        }
      } catch (KeyNotFoundException e) {
        ok = false;
        codeMinor = "ext.codeminor.role";
      } catch (PersistenceException e) {
        ok = false;
        codeMinor = "ext.codeminor.system";
      }
    }
// Perform requested action
    String type = b2Context.getRequestParameter("result_resultvaluesourcedid", Constants.DECIMAL_RESULT_TYPE);
    String value = null;
    String date = "";
    if (ok) {
      value = b2Context.getRequestParameter("result_resultscore_textstring", null);
      Lineitem lineitem = null;
      if (isV90) {
        lineitem = Gradebook_v90.getColumn(b2Context, tool.getId(), type, null, false, false, value, true);
      } else {
        lineitem = Gradebook.getColumn(b2Context, tool.getId(), tool.getName(), type, null, false, false, value, true);
      }
      if (actionName.equals(Constants.EXT_OUTCOMES_WRITE)) {
        ok = ((value != null) && (value.length() > 0));
        if (ok) {
          if (isV90) {
            ok = Gradebook_v90.updateGradebook(user, lineitem, type, value);
          } else {
            ok = Gradebook.updateGradebook(user, lineitem, type, value);
          }
          if (ok) {
            description = "ext.description.outcomes.updated";
            actionName = Constants.EXT_OUTCOMES_READ;
          } else {
            codeMinor = "ext.codeminor.system";
          }
        } else {
          codeMinor = "ext.codeminor.outcomevalue";
        }
      }
      if (actionName.equals(Constants.EXT_OUTCOMES_DELETE)) {
        if (isV90) {
          ok = Gradebook_v90.updateGradebook(user, lineitem, type, "");
        } else {
          ok = Gradebook.updateGradebook(user, lineitem, type, "");
        }
        if (ok) {
          value = null;
          description = "ext.description.outcomes.deleted";
        } else {
          codeMinor = "ext.codeminor.system";
        }
      } else if (actionName.equals(Constants.EXT_OUTCOMES_READ)) {
        Score score = null;
        if (isV90) {
          score = Gradebook_v90.getScore(lineitem, user.getId(), false);
        } else {
          score = Gradebook.getScore(lineitem, user.getId(), false);
        }
        if (score != null) {
          value = score.getGrade();
          if (score.getOutcome().getGradebookStatus().equals(GradebookStatus.NEEDSGRADING)) {
            value = "";
          } else if (value.length() > 0) {
            float max = lineitem.getPointsPossible();
            if (max == 0.0f) {
              max = 1.0f;
            }
            if (type.equals(Constants.DECIMAL_RESULT_TYPE)) {
              Float fValue = Utils.stringToFloat(value);
              ok = fValue != null;
              if (!ok) {
                codeMinor = "svc.codeminor.outcomevalue";
              } else if (max != 1.0f) {
                value = Utils.floatToString(fValue / max);
              }
            } else if (type.equals(Constants.RATIO_RESULT_TYPE)) {
              if (Utils.stringToFloat(value) != null) {
                value += "/" + Utils.floatToString(max);
              } else {
                codeMinor = "svc.codeminor.outcomevalue";
                ok = false;
              }
            } else if (type.equals(Constants.PERCENTAGE_RESULT_TYPE)) {
              Float fValue = Utils.stringToFloat(value);
              ok = fValue != null;
              if (!ok) {
                codeMinor = "svc.codeminor.outcomevalue";
              } else if (max != 100.0f) {
                value = Utils.floatToString(fValue / max * 100.0f);
              }
              value += "%";
            }
          }
          Calendar dateChanged = score.getDateChanged();
          if (dateChanged != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = dateFormat.format(dateChanged.getTime());
          }
          if (description == null) {
            description = "ext.description.outcomes.read";
          }
        } else {
          value = "";
        }
      } else if (!actionName.equals(Constants.EXT_OUTCOMES_WRITE)) {
        ok = false;
        codeMinor = "ext.codeminor.action";
      }
    }
    if (ok) {
      codeMinor = "ext.codeminor.success";
    } else {
      description = null;
    }
    response.setCodeMinor(b2Context.getResourceString(codeMinor));
    if (ok) {
      if (value != null) {
    	// re-encrypt the user
    	String encUser = user.getBatchUid();
		if (tool.isEncryptData()) {
			try {
				encUser = encryptInstance.encrypt(encUser);
			} catch (Exception e) {
			}
		}
        StringBuilder xml = new StringBuilder();
        xml.append("  <result>\n");
        xml.append("    <sourcedid>").append(sourcedid).append("</sourcedid>\n");
        if (tool.getDoSendUserSourcedid()) {
          xml.append("    <personsourcedid>").append(encUser).append("</personsourcedid>\n");
        }
        if (date.length() > 0) {
          xml.append("    <date>").append(date).append("</date>\n");
        }
        xml.append("    <resultscore>\n");
        xml.append("      <resultvaluesourcedid>").append(type).append("</resultvaluesourcedid>\n");
        xml.append("      <textstring>").append(value).append("</textstring>\n");
        xml.append("    </resultscore>\n");
        xml.append("  </result>\n");
        response.setData(xml.toString());
      }
      if (description != null) {
        response.setDescription(b2Context.getResourceString(description));
      } else {
        response.setDescription(null);
      }
    }

    return true;

  }

}
