package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class ListAddMutator extends CollectionAddMutator {
    public ListAddMutator(Add change, DomainObjectDao<? extends PlanTreeNode<?>> dao) {
        super(change, dao);
        if (change.getIndex() == null) {
            throw new IllegalArgumentException("This mutator requires the index property to be set");
        }
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void apply(PlanTreeNode<?> source) {
        castToOrdered(source).addChild(loadChild(), add.getIndex());
    }

    private PlanTreeOrderedInnerNode<? extends DomainObject, PlanTreeNode<?>> castToOrdered(PlanTreeNode<?> source) {
        return ((PlanTreeOrderedInnerNode<? extends DomainObject, PlanTreeNode<?>>) source);
    }
}
