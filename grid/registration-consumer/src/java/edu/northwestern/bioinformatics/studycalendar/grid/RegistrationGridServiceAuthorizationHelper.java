package edu.northwestern.bioinformatics.studycalendar.grid;

import org.globus.wsrf.security.SecurityManager;

public class RegistrationGridServiceAuthorizationHelper {
	
	public String getCurrentUsername() {
		String gridIdentity = SecurityManager.getManager().getCaller();
		if (gridIdentity == null){ 
			return null;
		}else {
			return gridIdentity.substring(gridIdentity.indexOf("/CN=")+4, gridIdentity.length());
		}
	}

}
