package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;

/**
 * This service provides methods for:
 *   - analyzing the plan node tree in the presence of deltas
 *   - performing security-related operations on templates
 *
 * This is a fairly low-level service, with no dependencies on other services.
 *
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class TemplateService {
    private DeltaDao deltaDao;
    private UserRoleDao userRoleDao;
    private StudyDao studyDao;
    private WorkflowService workflowService;
    private DaoFinder daoFinder;

    public static final String USER_IS_NULL = "User is null";
    public static final String SITE_IS_NULL = "Site is null";
    public static final String STUDY_IS_NULL = "Study is null";

    public edu.northwestern.bioinformatics.studycalendar.domain.User assignTemplateToSubjectCoordinator(
            Study study, Site site, edu.northwestern.bioinformatics.studycalendar.domain.User user
    ) {
        if (study == null) {
            throw new IllegalArgumentException(STUDY_IS_NULL);
        }
        if (site == null) {
            throw new IllegalArgumentException(SITE_IS_NULL);
        }
        if (user == null) {
            throw new IllegalArgumentException(USER_IS_NULL);
        }

        UserRole userRole = user.getUserRole(SUBJECT_COORDINATOR);
        if (!userRole.getStudySites().contains(findStudySite(study, site))) {
            userRole.addStudySite(findStudySite(study, site));
            userRoleDao.save(userRole);
        }

        return user;
    }

    public edu.northwestern.bioinformatics.studycalendar.domain.User removeAssignedTemplateFromSubjectCoordinator(
            Study study, Site site, edu.northwestern.bioinformatics.studycalendar.domain.User user
    ) {
        if (study == null) {
            throw new IllegalArgumentException(STUDY_IS_NULL);
        }
        if (site == null) {
            throw new IllegalArgumentException(SITE_IS_NULL);
        }
        if (user == null) {
            throw new IllegalArgumentException(USER_IS_NULL);
        }
        UserRole userRole = user.getUserRole(SUBJECT_COORDINATOR);
        StudySite studySite = study.getStudySite(site);
        if (user.hasAssignment(studySite)) {
            throw new StudyCalendarValidationException("%s is still responsible for one or more subjects on %s at %s.  Please reassign those subjects before removing %s from that study and site.",
                    user.getName(), study.getAssignedIdentifier(), site.getName(), user.getName());
        }
        if (userRole.getStudySites().contains(studySite)) {
            userRole.removeStudySite(studySite);
            userRoleDao.save(userRole);
        }

        return user;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    @Transactional(propagation = Propagation.SUPPORTS)
    public <P extends Parent> P findParent(Child<P> node) {
        if (node.getParent() != null) {
            return node.getParent();
        } else {
            Delta<P> delta = deltaDao.findDeltaWhereAdded(node);
            if (delta != null) {
                return delta.getNode();
            }

            delta = deltaDao.findDeltaWhereRemoved(node);
            if (delta != null) {
                return delta.getNode();
            }

            throw new StudyCalendarSystemException("Could not locate delta where %s was added or where it was removed", node);
        }
    }

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T extends Child> T findAncestor(Child node, Class<T> klass) {
        if (node == null) throw new NullPointerException("node must be specified");
        boolean moreSpecific = DomainObjectTools.isMoreSpecific(node.getClass(), klass);
        boolean parentable = PlanTreeNode.class.isAssignableFrom(node.parentClass());
        if (moreSpecific && parentable) {
            Parent parent = findParent(node);
            if (klass.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            } else {
                if (parent instanceof Child){
                    return findAncestor((Child) parent, klass);
                }
            }
        } else {
            throw new StudyCalendarSystemException("%s is not a descendant of %s",
                    node.getClass().getSimpleName(), klass.getSimpleName());
        }
        return null;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public <T extends Child> Collection<T> findChildren(Parent node, Class<T> childClass) {
        List<T> children = new LinkedList<T>();
        findChildren(node, childClass, children);
        return children;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    private <T extends Child> void findChildren(Parent node, Class<T> childClass, Collection<T> target) {
        if (childClass.isAssignableFrom(node.childClass())) {
            target.addAll(node.getChildren());
        } else {
            for (Object o : node.getChildren()) {
                if (o instanceof Parent) {
                    findChildren((Parent) o, childClass, target);
                }
            }
        }
    }

    // this is PlanTreeNode instead of PlanTreeNode<?> due to a javac bug
    @SuppressWarnings({"RawUseOfParameterizedType"})
    @Transactional(propagation = Propagation.SUPPORTS)
    public Study findStudy(PlanTreeNode node) {
        if (node instanceof PlannedCalendar) return ((PlannedCalendar) node).getStudy();
        else return findAncestor(node, PlannedCalendar.class).getStudy();
    }

    /**
     * Finds the node in the given study which matches the type and id of parameter node.
     */
    @SuppressWarnings({ "unchecked" })
    @Transactional(propagation = Propagation.SUPPORTS)
    public <C extends Changeable> C findEquivalentChild(Study study, C node) {
        if (isEquivalent(node, study)) {
            return (C)study;
        }
        if (node instanceof Population) {
            for (Population pop : study.getPopulations()) {
                if (isEquivalent(pop, node)) {
                    return (C) pop;
                }
            }
        }
        return findEquivalentChild(study.getPlannedCalendar(), node);
    }

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    @Transactional(propagation = Propagation.SUPPORTS)
    public <C extends Changeable> C findEquivalentChild(MutableDomainObject tree, C parameterNode) {
        if (isEquivalent(tree, parameterNode)) return (C) tree;
        if (tree instanceof Parent) {
            for (Object child : ((Parent) tree).getChildren()) {
                C match = findEquivalentChild((Child) child, parameterNode);
                if (match != null) return match;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean isEquivalent(MutableDomainObject node, MutableDomainObject toMatch) {
        return (toMatch == node) ||
                (sameClassIgnoringProxies(toMatch, node) && identifiersMatch(toMatch, node));
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T extends PlanTreeNode> T findCurrentNode(T transientNode) {
        if (!transientNode.isMemoryOnly()) {
            return transientNode;
        }
        GridIdentifiableDao<T> dao
            = (GridIdentifiableDao<T>) daoFinder.findDao(transientNode.getClass());
        return dao.getByGridId(transientNode.getGridId());
    }

    // This is not a general solution, but it will work for all PlanTreeNode subclasses
    private boolean sameClassIgnoringProxies(Object toMatch, Object node) {
        return toMatch.getClass().isAssignableFrom(node.getClass())
                || node.getClass().isAssignableFrom(toMatch.getClass());
    }

    private boolean identifiersMatch(MutableDomainObject toMatch, MutableDomainObject node) {
        boolean idMatch, gridIdMatch;
        idMatch = gridIdMatch = false;

        if (toMatch.getId() != null && node.getId() != null) {
            idMatch = toMatch.getId().equals(node.getId());
        }
        if (toMatch.getGridId() != null && node.getGridId() != null) {
            gridIdMatch = toMatch.getGridId().equals(node.getGridId());
        }
        return (idMatch || gridIdMatch);
    }

    /**
     * Returns all the templates the user can see, sorted by workflow status.  A template may
     * show up in more than one status for the same user.  (E.g., a template can both be in
     * development [for the next amendment] and available [for the current one].)
     */
    public Map<TemplateAvailability, List<StudyWorkflowStatus>> getVisibleTemplates(PscUser user) {
        return searchVisibleTemplates(user, null);
    }

    /**
     * Returns all the templates the user can see, sorted by workflow status.  A template may
     * show up in more than one status for the same user.  (E.g., a template can both be in
     * development [for the next amendment] and available [for the current one].)
     */
    public Map<TemplateAvailability, List<StudyWorkflowStatus>> searchVisibleTemplates(PscUser user, String term) {
        Map<TemplateAvailability, List<StudyWorkflowStatus>> results =
            new MapBuilder<TemplateAvailability, List<StudyWorkflowStatus>>().
                put(TemplateAvailability.IN_DEVELOPMENT, new LinkedList<StudyWorkflowStatus>()).
                put(TemplateAvailability.PENDING, new LinkedList<StudyWorkflowStatus>()).
                put(TemplateAvailability.AVAILABLE, new LinkedList<StudyWorkflowStatus>()).
                toMap();

        for (Study visible : studyDao.searchVisibleStudies(user.getVisibleStudyParameters(), term)) {
            StudyWorkflowStatus sws = workflowService.build(visible, user);
            for (TemplateAvailability availability : sws.getTemplateAvailabilities()) {
                results.get(availability).add(sws);
            }
        }

        for (Map.Entry<TemplateAvailability, List<StudyWorkflowStatus>> entry : results.entrySet()) {
            Comparator<StudyWorkflowStatus> comparator;
            if (entry.getKey() == TemplateAvailability.IN_DEVELOPMENT) {
                comparator = StudyWorkflowStatus.byDevelopmentDisplayName();
            } else {
                comparator = StudyWorkflowStatus.byReleaseDisplayName();
            }

            Collections.sort(entry.getValue(), comparator);
        }

        return results;
    }

    protected <T extends Changeable> void delete(Collection<T> collection) {
        for (T t : collection) {
            delete(t);
        }
    }

    // TODO: this should be in a more generic service, perhaps
    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    public <T extends MutableDomainObject> void delete(T object) {
        if (object != null) {
            DomainObjectDao<T> dao = (DomainObjectDao<T>) daoFinder.findDao(object.getClass());
            if (!(dao instanceof DeletableDomainObjectDao)) {
                throw new StudyCalendarSystemException(
                        "DAO for %s (%s) does not implement the deletable interface",
                        object.getClass().getSimpleName(), dao.getClass().getName()
                );
            }
            DeletableDomainObjectDao<T> deleter = (DeletableDomainObjectDao) dao;
            if (object instanceof Parent) {
                Parent innerNode = (Parent) object;
                delete(innerNode.getChildren());
                innerNode.getChildren().clear();
            }
            deleter.delete(object);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    @Required
    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
