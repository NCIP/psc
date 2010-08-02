package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.ArrayList;
import java.util.List;

/**
* @author Rhett Sutphin
*/
public class PscUserBuilder {
    private String username;
    private SuiteRoleMembership current;
    private List<SuiteRoleMembership> memberships;

    public PscUserBuilder(String username) {
        this.username = username;
        memberships = new ArrayList<SuiteRoleMembership>(PscRole.values().length);
    }

    public PscUser toUser() {
        if (current != null) memberships.add(current);
        return AuthorizationObjectFactory.createPscUser(username,
            memberships.toArray(new SuiteRoleMembership[memberships.size()]));
    }

    public PscUserBuilder add(PscRole role) {
        if (current != null) memberships.add(current);
        current = AuthorizationScopeMappings.createSuiteRoleMembership(role);
        return this;
    }

    public PscUserBuilder forAllSites() {
        if (current == null) throw new StudyCalendarSystemException("You need to add a role before you can scope it");
        if (current.getRole().isSiteScoped()) {
            current.forAllSites();
        } else {
            throw new StudyCalendarSystemException(current.getRole() + " is not site scoped");
        }
        return this;
    }

    public PscUserBuilder forAllStudies() {
        if (current == null) throw new StudyCalendarSystemException("You need to add a role before you can scope it");
        if (current.getRole().isStudyScoped()) {
            current.forAllStudies();
        } else {
            throw new StudyCalendarSystemException(current.getRole() + " is not study scoped");
        }
        return this;
    }

    public PscUserBuilder forSites(Site... sites) {
        if (current == null) throw new StudyCalendarSystemException("You need to add a role before you can scope it");
        if (current.getRole().isSiteScoped()) {
            current.notForAllSites();
            current.forSites(sites);
        } else {
            throw new StudyCalendarSystemException(current.getRole() + " is not site scoped");
        }
        return this;
    }

    public PscUserBuilder forStudies(Study... studies) {
        if (current == null) throw new StudyCalendarSystemException("You need to add a role before you can scope it");
        if (current.getRole().isStudyScoped()) {
            current.notForAllStudies();
            current.forStudies(studies);
        } else {
            throw new StudyCalendarSystemException(current.getRole() + " is not study scoped");
        }
        return this;
    }
}
