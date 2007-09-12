package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class CollectionAddMutator extends AddRemoveMutator {
    public CollectionAddMutator(Add change, DomainObjectDao<? extends PlanTreeNode<?>> dao) {
        super(change, dao);
    }

    @SuppressWarnings({ "unchecked" })
    public void apply(PlanTreeNode<?> source) {
        addTo(source);
    }

    public void revert(PlanTreeNode<?> target) {
        removeFrom(target);
    }
}
