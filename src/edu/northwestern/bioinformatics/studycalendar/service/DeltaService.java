package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.dao.MutableDomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for calculating deltas and for applying them to PlannedCalendars.
 * Should also provide the methods for applying them to schedules, though I'm not sure
 * about that yet.
 *
 * @author Rhett Sutphin
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DeltaService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private MutatorFactory mutatorFactory;
    private DaoFinder daoFinder;
    private DeltaDao deltaDao;

    /**
     * Applies all the deltas in the given revision to the source study,
     * returning a new, transient Study.  The revision might be
     * and in-progress amendment or a customization.
     */
    public Study revise(Study source, Revision revision) {
        log.debug("Revising {} with {}", source, revision);
        Study revised = source.transientClone();
        apply(revised, revision);
        return revised;
    }

    /**
     * Applies all the deltas in the given revision directly to the given study.
     */
    public void apply(Study target, Revision revision) {
        log.debug("Applying {} to {}", revision, target);
        for (Delta<?> delta : revision.getDeltas()) {
            PlanTreeNode<?> affected = findNodeForDelta(target, delta);
            for (Change change : delta.getChanges()) {
                log.debug("Applying change {} on {}", change, affected);
                mutatorFactory.createMutator(affected, change).apply(affected);
            }
        }
    }

    public void revert(Study target, Revision revision) {
        log.debug("Reverting {} from {}", revision, target);
        for (Delta<?> delta : revision.getDeltas()) {
            PlanTreeNode<?> affected = findNodeForDelta(target, delta);
            for (Change change : delta.getChanges()) {
                log.debug("Rolling back change {} on {}", change, affected);
                mutatorFactory.createMutator(affected, change).revert(affected);
            }
        }
    }

    public void updateRevision(Revision target, PlanTreeNode<?> node, Change change) {
        log.debug("Updating {}", target);
        if (node.isDetached()) {
            log.debug("{} is detached; apply {} directly", node, change);
            mutateNode(node, change);
        } else {
            log.debug("{} is part of the live tree; track {} separately", node, change);
            Delta<?> existing = null;
            for (Delta<?> delta : target.getDeltas()) {
                if (isEquivalent(delta.getNode(), node)) {
                    existing = delta;
                    break;
                }
            }
            if (existing == null) {
                log.debug("  - this is the first change; create new delta", change, node);
                target.getDeltas().add(Delta.createDeltaFor(node, change));
            } else {
                log.debug("  - it has been changed before; merge into existing delta {}", existing);
                change.mergeInto(existing);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void mutateNode(PlanTreeNode<?> node, Change change) {
        mutatorFactory.createMutator(node, change).apply(node);
    }

    private PlanTreeNode<?> findNodeForDelta(Study revised, Delta<?> delta) {
        PlanTreeNode<?> affected = findEquivalentChild(revised.getPlannedCalendar(), delta.getNode());
        if (affected == null) {
            throw new StudyCalendarSystemException(
                "Could not find a node in the target study matching the node in %s", delta);
        }
        return affected;
    }

    private PlanTreeNode<?> findEquivalentChild(PlanTreeNode<?> tree, PlanTreeNode<?> deltaNode) {
        if (isEquivalent(tree, deltaNode)) return tree;
        if (tree instanceof PlanTreeInnerNode) {
            for (PlanTreeNode<?> child : ((PlanTreeInnerNode<?, PlanTreeNode<?>, ?>) tree).getChildren()) {
                PlanTreeNode<?> match = findEquivalentChild(child, deltaNode);
                if (match != null) return match;
            }
        }
        return null;
    }

    private boolean isEquivalent(PlanTreeNode<?> node, PlanTreeNode<?> toMatch) {
        return (toMatch == node) ||
            (sameClassIgnoringProxies(toMatch, node) && toMatch.getId().equals(node.getId()));
    }

    // This is not a general solution, but it will work for all PlanTreeNode subclasses
    private boolean sameClassIgnoringProxies(PlanTreeNode<?> toMatch, PlanTreeNode<?> node) {
        return toMatch.getClass().isAssignableFrom(node.getClass())
            || node.getClass().isAssignableFrom(toMatch.getClass());
    }

    public void saveRevision(Revision revision) {
        if (revision == null) {
            throw new NullPointerException("Can't save a null revision");
        }
        if (!(revision instanceof MutableDomainObject)) {
            throw new StudyCalendarSystemException("%s is not a MutableDomainObject",
                revision.getClass().getName());
        }
        findDaoAndSave((MutableDomainObject) revision);
        for (Delta<?> delta : revision.getDeltas()) {
            for (Change change : delta.getChanges()) {
                if (change.getAction() == ChangeAction.ADD) {
                    PlanTreeNode<?> child = ((Add) change).getChild();
                    if (child != null) {
                        findDaoAndSave(child);
                    }
                }
            }
            findDaoAndSave(delta.getNode());
            deltaDao.save(delta);
        }
    }

    private void findDaoAndSave(MutableDomainObject object) {
        DomainObjectDao<?> dao = daoFinder.findDao(object.getClass());
        if (!(dao instanceof MutableDomainObjectDao)) {
            throw new StudyCalendarSystemException("%s does not implement %s", dao.getClass().getName(),
                MutableDomainObjectDao.class.getName());
        }
        ((MutableDomainObjectDao) dao).save(object);
    }

    ////// CONFIGURATION

    @Required
    public void setMutatorFactory(MutatorFactory mutatorFactory) {
        this.mutatorFactory = mutatorFactory;
    }

    @Required
    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
