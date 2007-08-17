package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class MutatorFactory {
    private DaoFinder daoFinder;

    @SuppressWarnings({ "unchecked" })
    public <T extends PlanTreeNode<?>, D extends Change> Mutator createMutator(T target, D change) {
        if (change.getAction() == ChangeAction.ADD) {
            Add add = (Add) change;
            PlanTreeInnerNode inner = (PlanTreeInnerNode) target;
            DomainObjectDao<? extends PlanTreeNode<?>> dao = daoFinder.findDao(inner.childClass());
            if (add.getIndex() == null) {
                return new CollectionAddMutator(add, dao);
            } else {
                return new ListAddMutator(add, dao);
            }
        }
        throw new UnsupportedOperationException("Could not construct mutator for " + change.getAction());
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
