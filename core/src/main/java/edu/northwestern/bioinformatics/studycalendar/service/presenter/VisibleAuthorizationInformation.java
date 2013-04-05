/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;

import java.util.Collections;
import java.util.List;

/**
 * Describes what authorization information a user is allowed to see about other users.
 * (All users can see their own authorization information.)  Does not describe _which_ users
 * a particular user may see.
 *
 * @author Rhett Sutphin
 * @see edu.northwestern.bioinformatics.studycalendar.service.PscUserService#getVisibleAuthorizationInformationFor(edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser)
 */
public class VisibleAuthorizationInformation {
    private List<SuiteRole> roles;
    private List<Site> sites;
    private List<Study> studiesForTemplateManagement;
    private List<Study> studiesForSiteParticipation;

    public VisibleAuthorizationInformation() {
        roles = Collections.emptyList();
        sites = Collections.emptyList();
        studiesForTemplateManagement = Collections.emptyList();
        studiesForSiteParticipation = Collections.emptyList();
    }

    public List<SuiteRole> getRoles() {
        return roles;
    }

    public void setRoles(List<SuiteRole> roles) {
        this.roles = roles;
    }

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    public List<Study> getStudiesForTemplateManagement() {
        return studiesForTemplateManagement;
    }

    public void setStudiesForTemplateManagement(List<Study> studiesForTemplateManagement) {
        this.studiesForTemplateManagement = studiesForTemplateManagement;
    }

    public List<Study> getStudiesForSiteParticipation() {
        return studiesForSiteParticipation;
    }

    public void setStudiesForSiteParticipation(List<Study> studiesForSiteParticipation) {
        this.studiesForSiteParticipation = studiesForSiteParticipation;
    }
}
