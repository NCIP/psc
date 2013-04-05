/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class ListAddMutator extends CollectionAddMutator {
    public ListAddMutator(Add change, DomainObjectDao<? extends Child<?>> dao) {
        super(change, dao);
        if (change.getIndex() == null) {
            throw new IllegalArgumentException("This mutator requires the index property to be set");
        }
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void apply(Changeable source) {
        Integer index = ((Add) change).getIndex();
        Child<?> child = findChild();
        if (source.isMemoryOnly()) {
            child = (Child<?>) child.transientClone();
        }
        log.debug("Adding {} to {} at {}", new Object[] { child, source, index });
        ((PlanTreeOrderedInnerNode) source).addChild((PlanTreeNode<?>) child, index);
    }
}
