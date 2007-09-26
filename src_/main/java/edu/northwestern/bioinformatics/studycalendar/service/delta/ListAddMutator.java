package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

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
        Integer index = ((Add) change).getIndex();
        PlanTreeNode<?> child = findChild();
        if (source.isMemoryOnly()) {
            child = child.transientClone();
        }
        log.debug("Adding {} to {} at {}", new Object[] { child, source, index });
        PlanTreeOrderedInnerNode.cast(source).addChild(child, index);
    }

}
