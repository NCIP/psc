/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import org.globus.wsrf.security.SecurityManager;

public class AdverseEventGridServiceAuthorizationHelper {

	public String getCurrentUsername() {
		String gridIdentity = SecurityManager.getManager().getCaller();
		if (gridIdentity == null){ 
			return null;
		}else {
			return gridIdentity.substring(gridIdentity.indexOf("/CN=")+4, gridIdentity.length());
		}
	}

}
