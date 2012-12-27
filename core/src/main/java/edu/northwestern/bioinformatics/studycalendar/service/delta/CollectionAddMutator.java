/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class CollectionAddMutator extends AbstractAddAndRemoveMutator {
    public CollectionAddMutator(Add change, DomainObjectDao<? extends Child<?>> dao) {
        super(change, dao);
    }

    @SuppressWarnings({ "unchecked" })
    public void apply(Changeable source) {
        if (!(source instanceof Parent)) {
            throw new StudyCalendarSystemException("You cannot apply a remove to a target which does not implement Parent");
        }
        addTo((Parent) source);
    }

    public void revert(Changeable target) {
        if (!(target instanceof Parent)) {
            throw new StudyCalendarSystemException("You cannot apply a remove to a target which does not implement Parent");
        }
        removeFrom((Parent) target);
    }
}
