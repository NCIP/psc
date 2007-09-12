package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public abstract class AddRemoveMutator implements Mutator {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ChildrenChange change;
    protected DomainObjectDao<? extends PlanTreeNode<?>> dao;

    public AddRemoveMutator(ChildrenChange change, DomainObjectDao<? extends PlanTreeNode<?>> dao) {
        this.dao = dao;
        this.change = change;
        if (dao == null && this.change.getChild() == null) {
            log.warn("{} for {} has neither a DAO nor a concrete child.  Unless this changes by the time the change is applied, there will be an exception.",
                getClass().getName(), change);
        }
    }

    protected PlanTreeNode<?> findChild() {
        if (change.getChild() != null) {
            return change.getChild();
        } else {
            return dao.getById(change.getChildId());
        }
    }

    public void apply(ScheduledCalendar calendar) {
        throw new UnsupportedOperationException("TODO");
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
        for (Iterator<? extends PlanTreeNode<?>> it = inner.getChildren().iterator(); it.hasNext();) {
            PlanTreeNode<?> child = it.next();
            if (change.isSameChild(child)) {
                log.debug("Removing {} from {}", child, target);
                it.remove();
                break;
            }
        }
    }
}
