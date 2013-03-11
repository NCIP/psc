/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityActivityMutator extends ChangePlannedActivitySimplePropertyMutator {
    private ActivityDao activityDao;

    public ChangePlannedActivityActivityMutator(
        PropertyChange change, ActivityDao activityDao
    ) {
        super(change);
        this.activityDao = activityDao;
    }

    @Override
    protected Object getAssignableValue(String value) {
        return activityDao.getByUniqueKey(value);
    }
}
