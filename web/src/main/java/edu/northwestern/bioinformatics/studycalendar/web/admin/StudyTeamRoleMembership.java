package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

/**
* @author Rhett Sutphin
*/
public class StudyTeamRoleMembership {
    private String studyIdentifier;
    private PscRole role;
    private SuiteRoleMembership membership;
    private PscUser user;

    public StudyTeamRoleMembership(String studyIdentifier, PscUser user, PscRole role) {
        this.studyIdentifier = studyIdentifier;
        this.role = role;
        this.user = user;
        membership = this.user.getMembership(this.role);
    }

    public String getStudyIdentifier() {
        return studyIdentifier;
    }

    public PscUser getUser() {
        return user;
    }

    public PscRole getRole() {
        return role;
    }

    public boolean getHasRole() {
        return membership != null;
    }

    public boolean isScopeIncluded() {
        if (!getHasRole()) return false;
        boolean isByAll = membership.isAllStudies() &&
            studyIdentifier.equals(BaseUserProvisioningCommand.JSON_ALL_SCOPE_IDENTIFIER);
        boolean isBySpecific = !membership.isAllStudies() &&
            membership.getStudyIdentifiers().contains(studyIdentifier);
        return isByAll || isBySpecific;
    }

    public boolean isAllStudiesForRole() {
        return membership != null && membership.isAllStudies();
    }
}
