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
package org.oscelot.blackboard.lti;

import java.util.logging.Level;
import java.util.logging.Logger;

import blackboard.data.navigation.NavigationApplication;
import blackboard.data.navigation.NavigationItem;
import blackboard.data.navigation.NavigationItem.NavigationType;
import blackboard.data.navigation.NavigationItem.ComponentType;
import blackboard.data.navigation.Mask;
import blackboard.platform.plugin.PlugIn;
import blackboard.platform.plugin.PlugInManagerFactory;
import blackboard.data.ValidationException;
import blackboard.data.navigation.ToolSettings;
import blackboard.persist.Id;
import blackboard.persist.navigation.NavigationApplicationDbLoader;
import blackboard.persist.navigation.NavigationApplicationDbPersister;
import blackboard.persist.navigation.NavigationItemDbLoader;
import blackboard.persist.navigation.NavigationItemDbPersister;
import blackboard.platform.persistence.PersistenceServiceFactory;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;

import com.spvsoftwareproducts.blackboard.utils.B2Context;


public class CourseTool {

  private B2Context b2Context = null;
  private Tool tool = null;
  private String toolId = null;
  private String toolSettingPrefix = null;
  private NavigationApplication navApplication = null;
  private NavigationItem navItem = null;
  private boolean toolChanged = false;
  private boolean navChanged = false;

  public CourseTool(B2Context b2Context, Tool tool, String id) throws Exception {

    this.b2Context = b2Context;
    this.tool = tool;
    this.toolId = tool.getId();
    this.toolSettingPrefix = Constants.TOOL_PARAMETER_PREFIX + "." + this.toolId + ".";
    if ((id != null) && (id.length() > 0)) {
      this.navApplication = this.getNavigationApplication();
      this.navItem = this.getNavigationItem();
    }
    if (this.navApplication == null) {
      this.navApplication = this.createNavigationApplication();
    }
    if ((this.navApplication != null) && (this.navItem == null)) {
      this.navItem = this.createNavigationItem();
    }
    this.persist();

  }

  public String getId() {

    String id = null;
    if (this.navItem != null) {
      id = this.navItem.getId().toExternalString();
    }

    return id;

  }

  public String getName() {

    String name = null;
    if (this.navItem != null) {
      name = this.navItem.getLabel();
    }

    return name;

  }

  public void setName(String name) {

    if ((this.navItem != null) && !this.navItem.getLabel().equals(name)) {
      this.navApplication.setName(name);
      this.navApplication.setLabel(name);
      this.navItem.setLabel(name);
      this.navChanged = true;
    }

  }

  public String getDescription() {

    String description = null;
    if (this.navItem != null) {
      description = this.navItem.getDescription();
    }

    return description;

  }

  public void setDescription(String description) {

    if ((this.navItem != null) && !this.navItem.getDescription().equals(description)) {
      this.navApplication.setDescription(description);
      this.navItem.setDescription(description);
      this.navChanged = true;
    }

  }

  public final void persist() {

    if (this.navChanged) {
      this.saveNavigation();
      this.navChanged = false;
    }
    if (this.toolChanged) {
      this.b2Context.persistSettings();
      this.toolChanged = false;
    }

  }

  public void delete() {

    if (this.navItem != null) {
      try {
        NavigationItemDbPersister niPersister = NavigationItemDbPersister.Default.getInstance();
        niPersister.deleteById(this.navItem.getId());
        this.navItem = null;
        NavigationApplicationDbPersister naPersister = NavigationApplicationDbPersister.Default.getInstance();
        naPersister.deleteById(this.navApplication.getId());
        this.navApplication = null;
        this.navChanged = false;
        this.b2Context.setSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOLAPP, null);
        this.b2Context.setSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOL, null);
        this.toolChanged = true;
        this.persist();
      } catch (PersistenceException e) {
        Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
      }
    }

  }

  private NavigationApplication getNavigationApplication() {

    NavigationApplication na = null;
    try {
      NavigationApplicationDbLoader navLoader = NavigationApplicationDbLoader.Default.getInstance();
      na = navLoader.loadById(Id.generateId(NavigationApplication.DATA_TYPE, this.b2Context.getSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOLAPP, "")));
    } catch (KeyNotFoundException e) {
      Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
    } catch (PersistenceException e) {
      Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
    }

    return na;

  }

  private NavigationItem getNavigationItem() {

    NavigationItem ni = null;
    try {
      NavigationItemDbLoader navLoader = NavigationItemDbLoader.Default.getInstance();
      ni = navLoader.loadByInternalHandle(this.b2Context.getVendorId() + "-" + this.b2Context.getHandle() + "-nav-" + this.toolId);
    } catch (KeyNotFoundException e) {
      Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
    } catch (PersistenceException e) {
      Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
    }

    return ni;

  }

  private NavigationApplication createNavigationApplication() {

    NavigationApplication na = new NavigationApplication();

    String appName = this.b2Context.getVendorId() + "-" + this.b2Context.getHandle();
    PlugIn plugIn = PlugInManagerFactory.getInstance().getPlugIn(b2Context.getVendorId(), b2Context.getHandle());

    na.setApplication(appName + "-" + this.tool.getId());
    na.setPlugInId(plugIn.getId());
    na.setIsCourseTool(true);
    na.setIsOrgTool(false);
    if (B2Context.getIsVersion(9, 1, 8)) {
      ToolSettings.Availability status = ToolSettings.Availability.valueOf(
         this.b2Context.getSetting(Constants.AVAILABILITY_PARAMETER, Constants.AVAILABILITY_DEFAULT_OFF));
      if (status == null) {
        status = ToolSettings.Availability.DefaultOff;
      }
      na.setCourseEnabledStatus(status);
    }
    na.setLargeIcon(this.b2Context.getPath() + "icon.jsp?course_id=@X@course.pk_string@X@&" + Constants.TOOL_ID + "=" + this.tool.getId());
    na.setLabel(this.tool.getName());
    na.setDescription(this.tool.getDescription());
    na.setName(this.tool.getName());

    this.navChanged = true;

    return na;

  }

  private NavigationItem createNavigationItem() {

    NavigationItem ni = new NavigationItem();

    String appName = this.b2Context.getVendorId() + "-" + this.b2Context.getHandle();

    ni.setInternalHandle(appName + "-nav-" + this.tool.getId());
    ni.setLabel(this.tool.getName());
    ni.setDescription(this.tool.getDescription());
    ni.setHref(this.b2Context.getPath() + "tool.jsp?course_id=@X@course.pk_string@X@&" + Constants.TOOL_ID + "=" + this.tool.getId());
    ni.setSrc(this.b2Context.getPath() + "icon.jsp?course_id=@X@course.pk_string@X@&" + Constants.TOOL_ID + "=" + this.tool.getId());
    ni.setApplication(appName + "-" + this.tool.getId());
    ni.setFamily("course_tools_area");
    ni.setSubGroup("");
    ni.setNavigationType(NavigationType.COURSE);
    ni.setComponentType(ComponentType.COURSE_ENTRY);
    ni.setIsEnabledMask(new Mask(3));
    ni.setEntitlementUid("course.tools.VIEW");

    this.navChanged = true;

    return ni;

  }

  private void saveNavigation() {

    try {
      this.navApplication.persist();
      String oldApplicationId = this.b2Context.getSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOLAPP, "");
      String applicationId = this.navApplication.getId().toExternalString();
      if (!oldApplicationId.equals(applicationId)) {
        this.b2Context.setSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOLAPP, applicationId);
        this.toolChanged = true;
      }
      this.navItem.persist();
      String oldItemId = this.b2Context.getSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOL, "");
      String itemId = this.navItem.getId().toExternalString();
      if (!oldItemId.equals(itemId)) {
        this.b2Context.setSetting(this.toolSettingPrefix + Constants.TOOL_COURSETOOL, itemId);
        this.toolChanged = true;
      }
      this.navChanged = false;
      PersistenceServiceFactory.getInstance().getDbPersistenceManager().refreshLoader("PlugInDbLoader");
    } catch (PersistenceException e) {
      Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
    } catch (ValidationException e) {
      Logger.getLogger(CourseTool.class.getName()).log(Level.SEVERE, null, e);
    }

  }

}
