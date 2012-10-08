package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jalpa Patel
 */
public class RemovePlannedActivityLabelMutator extends RemoveMutator {
    private ScheduledActivityDao saDao;
    public RemovePlannedActivityLabelMutator(Remove change, DomainObjectDao<? extends Child<?>> dao, ScheduledActivityDao saDao) {
        super(change, dao);
        this.saDao = saDao;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivityLabel paLabel = (PlannedActivityLabel) findChild();
        PlannedActivity plannedActivity = (PlannedActivity) change.getDelta().getNode();
        plannedActivity.removeChild(paLabel);
        Collection<ScheduledActivity> scheduledActivities
            = new ArrayList<ScheduledActivity>(saDao.getEventsFromPlannedActivity(plannedActivity, calendar));
        for (Iterator<ScheduledActivity> it = scheduledActivities.iterator(); it.hasNext();) {
            ScheduledActivity sa = it.next();
            if (paLabel.appliesToRepetition(sa.getRepetitionNumber())) {
                sa.removeLabel(paLabel.getLabel());
            }
        }
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }
}
