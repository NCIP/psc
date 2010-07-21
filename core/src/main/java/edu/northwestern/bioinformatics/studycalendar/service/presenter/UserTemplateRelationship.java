package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class UserTemplateRelationship {
    private PscUser user;
    private Study study;

    public UserTemplateRelationship(PscUser pscUser, Study study) {
        this.user = pscUser;
        this.study = study;
    }

    /**
     * The user can introduce a new development amendment.  False if there
     * already is a development amendment regardless of role.
     */
    public boolean getCanStartAmendment() {
        return !getStudy().isInDevelopment() && isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    /**
     * The user can make changes to the current development amendment.  False if no
     * development amendment regardless of role.
     */
    public boolean getCanDevelop() {
        return getStudy().isInDevelopment() && isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    /**
     * The user can see the current development amendment.  False if no
     * development amendment regardless of role.
     */
    public boolean getCanSeeDevelopmentVersion() {
        return getStudy().isInDevelopment() &&
            isManagingAsOneOf(STUDY_QA_MANAGER, DATA_READER, STUDY_CALENDAR_TEMPLATE_BUILDER, STUDY_CREATOR) ||
            hasGlobalRole(DATA_IMPORTER);
    }

    /**
     * The user can change or clear the set of managing sites.
     */
    public boolean getCanChangeManagingSites() {
        return isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER, STUDY_QA_MANAGER);
    }

    /**
     * The user may release the template for use.  False if there is
     * no development amendment regardless of role.
     */
    public boolean getCanRelease() {
        return study.isInDevelopment() && isManagingAsOneOf(STUDY_QA_MANAGER);
    }

    /**
     * The user may designate sites to participate in the study.  False if there is
     * no released amendment regardless of role.
     */
    public boolean getCanSetParticipation() {
        return study.isReleased() && isManagingAsOneOf(STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
    }

    /**
     * The user may approve the template or an amendment for use at one or more
     * sites.  False if there are no unapproved amendments regardless or role.
     */
    public boolean getCanApprove() {
        Collection<StudySite> candidates = getParticipatingStudySites(STUDY_QA_MANAGER);
        for (StudySite candidate : candidates) {
            if (!candidate.getUnapprovedAmendments().isEmpty()) return true;
        }
        return false;
    }

    // TODO: maybe getApprovableStudySitesAndAmendments ?

    /**
     * The template is released and approved at at at least one site for which the
     * user is allowed to assign new subjects.
     */
    public boolean getCanAssignSubjects() {
        return !getSubjectAssignableStudySites().isEmpty();
    }

    /**
     * The list of StudySites for which the user may assign new subjects and
     * which are released and approved for this template.
     */
    public Collection<StudySite> getSubjectAssignableStudySites() {
        Collection<StudySite> subjectAssignable =
            getParticipatingStudySites(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        for (Iterator<StudySite> it = subjectAssignable.iterator(); it.hasNext();) {
            StudySite candidate = it.next();
            if (candidate.getAmendmentApprovals().isEmpty()) it.remove();
        }
        return subjectAssignable;
    }

    /**
     * Indicates whether a user can see the released version of this template at all.
     * Always false if there are no released versions.
     */
    public boolean getCanSeeReleasedVersions() {
        return study.isReleased() &&
            (isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER, STUDY_QA_MANAGER, STUDY_SITE_PARTICIPATION_ADMINISTRATOR, DATA_READER, STUDY_CREATOR) ||
                hasGlobalRole(DATA_IMPORTER) ||
                isParticipatingAsOneOf(STUDY_QA_MANAGER, DATA_READER, STUDY_TEAM_ADMINISTRATOR) ||
                getCanAssignSubjects()); 
    }

    public PscUser getUser() {
        return user;
    }

    public Study getStudy() {
        return study;
    }

    ////// HELPERS

    /**
     * Indicates that the user has at least one of the given roles, scoped appropriately for
     * "managing" actions. This is not intended to be used for authorization -- the specific
     * action authorizations should be used instead. It's exposed so that it can be tested in
     * isolation.
     */
    protected boolean isManagingAsOneOf(PscRole... roles) {
        for (PscRole role : roles) {
            SuiteRoleMembership manager = getUser().getMembership(role);
            if (manager == null) continue;
            if (isManagingMembership(manager)) return true;
        }
        return false;
    }

    /**
     * Indicates that the user has at least one of the given roles, scoped appropriately for
     * "participating" actions. This is not intended to be used for authorization -- the specific
     * action authorizations should be used instead. It's exposed so that it can be tested in 
     * isolation.
     */
    protected boolean isParticipatingAsOneOf(PscRole... roles) {
        for (PscRole role : roles) {
            if (!getParticipatingStudySites(role).isEmpty()) return true;
        }
        return false;
    }

    private Collection<StudySite> getParticipatingStudySites(PscRole role) {
        if (!role.getUses().contains(PscRoleUse.SITE_PARTICIPATION)) {
            throw new StudyCalendarError("%s is not a site participation role", role);
        }
        SuiteRoleMembership participant = getUser().getMembership(role);
        if (participant == null || !hasMatchingStudyScope(participant)) return Collections.emptySet();
        if (participant.isAllSites()) return study.getStudySites();

        Set<StudySite> matches = new LinkedHashSet<StudySite>();
        for (StudySite studySite : study.getStudySites()) {
            if (participant.getSites().contains(studySite.getSite())) matches.add(studySite);
        }
        return matches;
    }

    private boolean isManagingMembership(SuiteRoleMembership membership) {
        if (!PscRole.valueOf(membership.getRole()).getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT)) {
            throw new StudyCalendarError("%s is not a template management role", membership.getRole());
        }
        if (!hasMatchingStudyScope(membership)) return false;
        if (!study.isManaged()) return true;
        if (membership.isAllSites()) return true;
        for (Site site : getStudy().getManagingSites()) {
            if (membership.getSites().contains(site)) return true;
        }
        return false;
    }

    private boolean hasMatchingStudyScope(SuiteRoleMembership membership) {
        return !membership.getRole().isStudyScoped() ||
            membership.isAllStudies() ||
            membership.getStudyIdentifiers().contains(
                AuthorizationScopeMappings.STUDY_MAPPING.getSharedIdentity(study));
    }

    private boolean hasGlobalRole(PscRole role) {
        if (role.isScoped()) {
            throw new StudyCalendarSystemException("Use a scoped accessor for %s", role.getDisplayName());
        }
        return getUser().getMembership(role) != null;
    }
}
