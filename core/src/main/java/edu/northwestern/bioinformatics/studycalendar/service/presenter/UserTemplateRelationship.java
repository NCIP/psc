package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class UserTemplateRelationship {
    private UserRelationshipTools tools;

    private PscUser user;
    private Study study;
    private Configuration configuration;

    public UserTemplateRelationship(PscUser pscUser, Study study, Configuration configuration) {
        tools = new UserRelationshipTools(pscUser, study);
        this.user = pscUser;
        this.study = study;
        this.configuration = configuration;
    }

    /**
     * The user can introduce a new development amendment.  False if there
     * already is a development amendment regardless of role.
     */
    public boolean getCanStartAmendment() {
        return !getStudy().isInDevelopment() && tools.isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    /**
     * The user can make changes to the current development amendment.  False if no
     * development amendment regardless of role.
     */
    public boolean getCanDevelop() {
        return getStudy().isInDevelopment() && tools.isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    /**
     * The user can see the current development amendment.  False if no
     * development amendment regardless of role.
     */
    public boolean getCanSeeDevelopmentVersion() {
        return getStudy().isInDevelopment() &&
            (tools.isManagingAsOneOf(STUDY_QA_MANAGER, DATA_READER, STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_CREATOR, DATA_IMPORTER));
    }

    /**
     * The user can change or clear the set of managing sites.
     */
    public boolean getCanChangeManagingSites() {
        return tools.isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER, STUDY_QA_MANAGER);
    }

    /**
     * The user may release the template for use.  False if there is
     * no development amendment regardless of role.
     */
    public boolean getCanRelease() {
        return study.isInDevelopment() && tools.isManagingAsOneOf(STUDY_QA_MANAGER);
    }

    /**
     * The user may designate sites to participate in the study.  False if there is
     * no released amendment regardless of role.
     */
    public boolean getCanSetParticipation() {
        return study.isReleased() && tools.isManagingAsOneOf(STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
    }

    /**
     * The user may approve the template or an amendment for use at one or more
     * sites.  False if there are no unapproved amendments regardless or role.
     */
    public boolean getCanApprove() {
        Collection<StudySite> candidates = tools.getParticipatingStudySites(STUDY_QA_MANAGER);
        for (StudySite candidate : candidates) {
            if (!candidate.getUnapprovedAmendments().isEmpty()) return true;
        }
        return false;
    }

    /**
     * The user may schedule a reconsent, and there is at least one assignment which
     * will receive it.
     */
    public boolean getCanScheduleReconsent() {
        if (!tools.isManagingAsOneOf(PscRole.STUDY_QA_MANAGER)) return false;
        for (StudySite ss : getStudy().getStudySites()) {
            if (ss.isUsed()) return true;
        }
        return false;
    }

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
    public Collection<UserStudySiteRelationship> getSubjectAssignableStudySites() {
        Collection<StudySite> sscmStudySites =
            tools.getParticipatingStudySites(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        Collection<UserStudySiteRelationship> subjectAssignable
            = new ArrayList<UserStudySiteRelationship>(sscmStudySites.size());
        for (StudySite sscmStudySite : sscmStudySites) {
            UserStudySiteRelationship candidate =
                new UserStudySiteRelationship(getUser(), sscmStudySite, configuration);
            if (candidate.getCanAssignSubjects()) subjectAssignable.add(candidate);
        }
        return subjectAssignable;
    }

    /**
     * Indicates whether a user can see the released version of this template at all.
     * Always false if there are no released versions.
     */
    public boolean getCanSeeReleasedVersions() {
        return study.isReleased() &&
            (tools.isManaging() ||
                tools.isParticipatingAsOneOf(STUDY_QA_MANAGER, DATA_READER, STUDY_TEAM_ADMINISTRATOR) ||
                getCanAssignSubjects()); 
    }

    public Collection<UserStudySiteRelationship> getVisibleStudySites() {
        List<UserStudySiteRelationship> visible = new LinkedList<UserStudySiteRelationship>();
        for (StudySite ss : study.getStudySites()) {
            UserStudySiteRelationship rel = new UserStudySiteRelationship(getUser(), ss, configuration);
            if (rel.isVisible()) visible.add(rel);
        }
        return visible;
    }

    /**
     * The user can assign identifiers to study in the current development amendment.  False if no
     * development amendment regardless of role.
     */
    public boolean getCanAssignIdentifiers() {
        return getStudy().isInDevelopment() && tools.isManagingAsOneOf(STUDY_CREATOR);
    }

    /**
     * The user can purge the study.  False if no managing role of Study QA Manager exist.
     */
    public boolean getCanPurge() {
        return tools.isManagingAsOneOf(STUDY_QA_MANAGER);
    }

    ////// ACCESSORS

    public PscUser getUser() {
        return user;
    }

    public Study getStudy() {
        return study;
    }

    ////// OBJECT METHODS

    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTemplateRelationship)) return false;

        UserTemplateRelationship that = (UserTemplateRelationship) o;

        if (study != null ? !study.equals(that.study) : that.study != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (study != null ? study.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[user=").append(user).
            append("; template=").append(study.getAssignedIdentifier()).
            append("]").
            toString();
    }
}
