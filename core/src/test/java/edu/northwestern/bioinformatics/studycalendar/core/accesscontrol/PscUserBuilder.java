/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Rhett Sutphin
*/
public class PscUserBuilder {
    private String username;
    private SuiteRoleMembership current;
    private List<SuiteRoleMembership> memberships;
    private Map<String, Object> csmUserProperties;

    public PscUserBuilder() {
        this("josephine");
    }

    public PscUserBuilder(String username) {
        this.username = username;
        memberships = new ArrayList<SuiteRoleMembership>(PscRole.values().length);
        csmUserProperties = new HashMap<String, Object>();
    }

    public PscUser toUser() {
        pushCurrentMembership();
        PscUser pscUser = AuthorizationObjectFactory.createPscUser(username,
            memberships.toArray(new SuiteRoleMembership[memberships.size()]));
        if (csmUserProperties.size() > 0) {
            new BeanWrapperImpl(pscUser.getCsmUser()).setPropertyValues(csmUserProperties);
        }
        return pscUser;
    }

    public PscUserBuilder setCsmUserId(long id) {
        setCsmUserProperty("userId", id);
        return this;
    }

    public PscUserBuilder setCsmUserProperty(String property, Object value) {
        csmUserProperties.put(property, value);
        return this;
    }

    public PscUserBuilder add(PscRole role) {
        pushCurrentMembership();
        current = AuthorizationScopeMappings.createSuiteRoleMembership(role);
        return this;
    }

    private void pushCurrentMembership() {
        if (current == null) return;
        current.checkComplete();
        current.validate();
        memberships.add(current);
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
