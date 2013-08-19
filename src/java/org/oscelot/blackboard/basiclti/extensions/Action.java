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

import java.util.List;

import com.spvsoftwareproducts.blackboard.utils.B2Context;
import org.oscelot.blackboard.basiclti.Tool;

public interface Action {

  public abstract boolean execute(String actionName, B2Context b2Context, Tool tool,
     List<String> serviceData, Response response);

}
