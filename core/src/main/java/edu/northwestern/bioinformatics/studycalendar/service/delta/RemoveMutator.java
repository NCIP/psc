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
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class RemoveMutator extends AbstractAddAndRemoveMutator {
    public RemoveMutator(Remove remove, DomainObjectDao<? extends Child<?>> dao) {
        super(remove, dao);
    }

    public void apply(Changeable source) {
        if (!(source instanceof Parent)) {
            throw new StudyCalendarSystemException("You cannot apply a remove to a target which does not implement Parent");
        }
        removeFrom((Parent) source);
    }

    public void revert(Changeable target) {
        if (!(target instanceof Parent)) {
            throw new StudyCalendarSystemException("You cannot apply a remove to a target which does not implement Parent");
        }
        addTo((Parent) target);
    }
}
