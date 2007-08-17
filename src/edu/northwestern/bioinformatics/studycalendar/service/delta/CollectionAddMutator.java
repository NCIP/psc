package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

import java.util.Iterator;
import java.util.Collection;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class CollectionAddMutator implements Mutator {
    protected Add add;
    protected DomainObjectDao<? extends PlanTreeNode<?>> dao;

    public CollectionAddMutator(Add change, DomainObjectDao<? extends PlanTreeNode<?>> dao) {
        this.add = change;
        this.dao = dao;
    }

    @SuppressWarnings({ "unchecked" })
    public void apply(PlanTreeNode<?> source) {
        castToInner(source).addChild(loadChild());
    }

    protected PlanTreeNode<?> loadChild() {
        return dao.getById(add.getNewChildId());
    }

    public void revert(PlanTreeNode<?> target) {
        PlanTreeInnerNode<?, PlanTreeNode<?>, ?> inner = castToInner(target);
        for (Iterator<? extends PlanTreeNode<?>> it = inner.getChildren().iterator(); it.hasNext();) {
            DomainObject child = it.next();
            if (add.getNewChildId().equals(child.getId())) {
                it.remove();
                break;
            }
        }
    }

    public void apply(ScheduledCalendar calendar) {
        throw new UnsupportedOperationException("TODO");
    }

    // for cleanliness
    private PlanTreeInnerNode<?, PlanTreeNode<?>, ?> castToInner(PlanTreeNode<?> source) {
        return (PlanTreeInnerNode<?, PlanTreeNode<?>, ?>) source;
    }

    // accessor for testing
    DomainObjectDao<? extends PlanTreeNode<?>> getDao() { return dao; }
}
