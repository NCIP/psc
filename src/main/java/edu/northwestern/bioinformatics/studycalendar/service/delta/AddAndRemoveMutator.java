package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
abstract class AddAndRemoveMutator implements Mutator {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ChildrenChange change;
    protected DomainObjectDao<? extends PlanTreeNode<?>> dao;

    public AddAndRemoveMutator(ChildrenChange change, DomainObjectDao<? extends PlanTreeNode<?>> dao) {
        this.dao = dao;
        this.change = change;
        if (dao == null && this.change.getChild() == null) {
            log.warn("{} for {} has neither a DAO nor a concrete child.  Unless this changes by the time the change is applied, there will be an exception.",
                getClass().getName(), change);
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    protected PlanTreeNode findChild() {
        if (change.getChild() != null) {
            return change.getChild();
        } else {
            return dao.getById(change.getChildId());
        }
    }

    // accessor for testing
    DomainObjectDao<? extends PlanTreeNode<?>> getDao() { return dao; }

    protected void addTo(PlanTreeNode<?> source) {
        PlanTreeNode<?> child = findChild();
        if (source.isMemoryOnly()) {
            child = child.transientClone();
        }
        log.debug("Adding {} to {}", child, source);
        PlanTreeInnerNode.cast(source).addChild(child);
    }

    protected void removeFrom(PlanTreeNode<?> target) {
        PlanTreeInnerNode<?, PlanTreeNode<?>, ?> inner = PlanTreeInnerNode.cast(target);
        PlanTreeNode<?> toRemove = null;
        for (PlanTreeNode<?> child : inner.getChildren()) {
            if (change.isSameChild(child)) {
                log.debug("Removing {} from {}", child, target);
                toRemove = child;
                break;
            }
        }
        if (toRemove == null) {
            log.warn("The child referenced in {} was not found in {}", change, target);
            return;
        }
        inner.removeChild(toRemove);
    }

    public boolean appliesToExistingSchedules() {
        return false;
    }

    public void apply(ScheduledCalendar calendar) {
        throw new StudyCalendarSystemException("%s cannot be applied to an existing schedule", change);
    }
}
