package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * Variant of {@link MutatorFactory} suitable for use with all-in-memory revisions.
 * Does not provide DAOs to mutators, so they will have no opportunity to resolve
 * IDs.
 *
 * @see edu.northwestern.bioinformatics.studycalendar.domain.delta.Add#getChild
 * @see edu.northwestern.bioinformatics.studycalendar.domain.delta.Add#getChildId
 * @see edu.northwestern.bioinformatics.studycalendar.domain.Fixtures
 * @author Rhett Sutphin
 */
public class MemoryOnlyMutatorFactory extends MutatorFactory {
    @Override
    protected <T extends PlanTreeNode<?>> DomainObjectDao<?> findDao(Class<T> klass) {
        return null;
    }
}
