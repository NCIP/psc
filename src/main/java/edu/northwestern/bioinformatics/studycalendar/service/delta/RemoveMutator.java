package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class RemoveMutator extends AbstractAddAndRemoveMutator {
    public RemoveMutator(Remove remove, DomainObjectDao<? extends PlanTreeNode<?>> dao) {
        super(remove, dao);
    }

    public void apply(PlanTreeNode<?> source) {
        removeFrom(source);
    }

    public void revert(PlanTreeNode<?> target) {
        addTo(target);
    }
}
