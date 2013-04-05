/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

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
    public AddPlannedActivityLabelMutator(Add change, DomainObjectDao<? extends Child<?>> dao) {
        super(change, dao);
    }
    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivityLabel paLabel = (PlannedActivityLabel) findChild();
        PlannedActivity plannedActivity = (PlannedActivity) change.getDelta().getNode();
        Collection<ScheduledActivity> allScheduledActivities = calendar.getScheduledActivitiesFor(plannedActivity);
        for (ScheduledActivity sa : allScheduledActivities) {
            if (sa.getPlannedActivity().equals(plannedActivity)) {
                if (paLabel.appliesToRepetition(sa.getRepetitionNumber())) {
                    sa.addLabel(paLabel.getLabel());
                }
            }
        }
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }
}
