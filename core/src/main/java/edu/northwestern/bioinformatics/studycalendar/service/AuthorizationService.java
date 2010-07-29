package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyConsumer studyConsumer;

    private CsmHelper csmHelper;
    private AuthorizationManager csmAuthorizationManager;

    /**
     * Returns all the users have been designated to have the given role.
     * Be careful: for performance reasons, this method does not filter out scoped users
     * who have only been partially provisioned.
     */
    @SuppressWarnings({ "unchecked" })
    public Collection<gov.nih.nci.security.authorization.domainobjects.User> getCsmUsers(PscRole role) {
        try {
            Group roleGroup = csmHelper.getRoleCsmGroup(role.getSuiteRole());
            return csmAuthorizationManager.getUsers(roleGroup.getGroupId().toString());
        } catch (CSObjectNotFoundException e) {
            log.debug("CSM could not find the group on second load while resolving users for {}", role);
            return Collections.emptySet();
        }
    }

    public List<StudySubjectAssignment> filterAssignmentsForVisibility(List<StudySubjectAssignment> source, User visibleTo) {
        log.debug("Filtering {} assignments for visibility to {}", source.size(), visibleTo);
        List<StudySubjectAssignment> visible = new LinkedList<StudySubjectAssignment>();

        UserRole subjCoord = visibleTo.getUserRole(Role.SUBJECT_COORDINATOR);
        if (subjCoord != null) {
            log.debug(" - is a subject coordinator for {}", subjCoord.getStudySites());
            for (StudySubjectAssignment assignment : source) {
                if (subjCoord.getStudySites().contains(assignment.getStudySite())) {
                    log.debug(" - {} is visible", assignment);
                    visible.add(assignment);
                } else {
                    log.debug(" - {} is not visible", assignment);
                }
            }
        }

        return visible;
    }

    public List<Study> filterStudiesForVisibility(List<Study> studies, User visibleTo) {
        studyConsumer.refresh(studies);

        Set<Study> all = new LinkedHashSet<Study>();
        for (Study study : studies) {
            for (UserRole role : visibleTo.getUserRoles()) {
                if (isTemplateVisible(role, study)) all.add(study);
            }
        }
        return new ArrayList<Study>(all);
    }

    /**
     * Returns a copy of the given list of studies containing only those which should be
     * visible to the given user role.
     *
     * @param studies
     * @return
     * @throws Exception
     */
    public List<Study> filterStudiesForVisibility(List<Study> studies, UserRole visibleTo) {
        if (visibleTo == null) {
            return Collections.emptyList();
        }
        studyConsumer.refresh(studies);

        List<Study> filtered = new ArrayList<Study>(studies);
        for (Iterator<Study> it = filtered.iterator(); it.hasNext();) {
            Study study = it.next();
            if (!isTemplateVisible(visibleTo, study)) it.remove();
        }
        return filtered;
    }

    /**
     * Returns a copy of the given list of study subject assignmnents containing only those which should be
     * visible to the given user role.
     *
     * @param studySites
     * @param assignments
     * @return
     * @throws Exception
     */
    public List<StudySubjectAssignment> filterStudySubjectAssignmentsByStudySite(Collection<StudySite> studySites, Collection<StudySubjectAssignment> assignments) {
        List <StudySubjectAssignment> filtered = new ArrayList<StudySubjectAssignment>();
        for (StudySubjectAssignment ssa : assignments) {
            if (studySites.contains(ssa.getStudySite())){
                filtered.add(ssa);
            }
        }
        return filtered;
    }

    /**
     * Returns a copy of the given list of study sites containing only those which should be
     * visible to the given user role.
     *
     * @param studySites
     * @param visibleTo
     * @return
     * @throws Exception
     */
    public List<StudySite> filterStudySitesForVisibility (List<StudySite> studySites, UserRole visibleTo) {
        if (visibleTo == null) {
            return Collections.emptyList();
        }
        List<StudySite> filtered = new ArrayList<StudySite>();
        for (StudySite studySite : studySites) {
            if (visibleTo.getStudySites().contains(studySite)){
                filtered.add(studySite);
            }
        }
        return filtered;
    }

    /**
     * Returns a copy of the given list of studies containing only those which should be
     * visible to the given user role.
     *
     * @param studies
     * @param visibleTo
     * @return
     * @throws Exception
     */
    public List<StudySite> filterStudySitesForVisibilityFromStudiesList (List<Study> studies, UserRole visibleTo) {
        if (visibleTo == null) {
            return Collections.emptyList();
        }
        List<StudySite> filtered = new ArrayList<StudySite>();
        for (Study study : studies) {
            for (StudySite studySite: study.getStudySites()) {
                if (visibleTo.getStudySites().contains(studySite)){
                    filtered.add(studySite);
                }
            }
        }
        return filtered;
    }

    public boolean isTemplateVisible(UserRole userRole, Study study) {
        if (userRole.getRole() == SYSTEM_ADMINISTRATOR) {
            return false;
        } else if (!userRole.getRole().isSiteSpecific()) {
            return true;
        } else if (userRole.getRole() == SITE_COORDINATOR) {
            return isTemplateVisibleToSiteCoordinator(userRole, study);
        } else if (userRole.getRole() == SUBJECT_COORDINATOR ) {
            return isTemplateVisibleToStudySpecificRole(userRole, study);
        } else {
            throw new UnsupportedOperationException("Unexpected role in userRole: " + userRole.getRole());
        }
    }

    private boolean isTemplateVisibleToStudySpecificRole(UserRole studySpecificRole, Study study) {
        for (StudySite studySite : study.getStudySites()) {
            if (studySpecificRole.getStudySites().contains(studySite)) return true;
        }
        return false;
    }

    private boolean isTemplateVisibleToSiteCoordinator(UserRole siteCoordinator, Study study) {
        for (Site siteOnStudy : study.getSites()) {
            if (siteCoordinator.getSites().contains(siteOnStudy)) return true;
        }
        return false;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyConsumer(StudyConsumer studyConsumer) {
        this.studyConsumer = studyConsumer;
    }

    @Required
    public void setCsmHelper(CsmHelper csmHelper) {
        this.csmHelper = csmHelper;
    }

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }
}
