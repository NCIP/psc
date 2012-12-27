/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * @author Jalpa Patel
 */
public class AddPlannedActivityLabelMutator extends CollectionAddMutator {
    private ScheduledActivityDao saDao;
    public AddPlannedActivityLabelMutator(Add change, DomainObjectDao<? extends Child<?>> dao, ScheduledActivityDao saDao) {
        super(change, dao);
        this.saDao = saDao;
    }
    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivityLabel paLabel = (PlannedActivityLabel) findChild();
        PlannedActivity plannedActivity = (PlannedActivity) change.getDelta().getNode();
        Collection<ScheduledActivity> scheduledActivities
            = new ArrayList<ScheduledActivity>(saDao.getEventsFromPlannedActivity(
                plannedActivity, calendar));
        for (Iterator<ScheduledActivity> it = scheduledActivities.iterator(); it.hasNext();) {
            ScheduledActivity sa = it.next();
            if (paLabel.appliesToRepetition(sa.getRepetitionNumber())) {
                    sa.addLabel(paLabel.getLabel());
            }
        }
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }
}
