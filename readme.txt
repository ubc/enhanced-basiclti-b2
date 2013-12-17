Name: BasicLTI.war
Author: Stephen P Vickers
Download: http://projects.oscelot.org/gf/project/bb-basiclti/
Documentation: http://www.spvsoftwareproducts.com/


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
    
    *******************
    
    Version 3.0.1:
    
    Modified to include the option to encrypt data sent to and decrypt data sent from tools that are not hosted locally.
    
    Any user information sent to the tool via LTI Membership Extension Request request is being encrypted. That way the external tool only receives and stores encrypted hashes.
    
    In case of an LTI Outcomes Extension Request, the user identification sent by the tool will be decrypted to compute the grade. The user will then be encrypted again before the return message is created and sent back to the external tool.

    Changes:
    - A new package (ca.ubc.ctlt.encryption), that includes two new classes, has been added to handle the de- and encryption.
    - Membership.java and Outcomes.java have been modified to include encryption.
    - tool.jsp has been modified to include an option to enable encryption for each tool individually.