/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jalpa Patel
 */
public class RemovePlannedActivityLabelMutator extends RemoveMutator {
    public RemovePlannedActivityLabelMutator(Remove change, DomainObjectDao<? extends Child<?>> dao) {
        super(change, dao);
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivityLabel paLabel = (PlannedActivityLabel) findChild();
        PlannedActivity plannedActivity = (PlannedActivity) change.getDelta().getNode();
        plannedActivity.removeChild(paLabel);
        Collection<ScheduledStudySegment> scheduledStudySegments = calendar.getScheduledStudySegmentsFor(plannedActivity.getPeriod().getStudySegment());
        for (ScheduledStudySegment scheduledStudySegment : scheduledStudySegments) {
            for (ScheduledActivity sa : scheduledStudySegment.getActivities()) {
                if (sa.getPlannedActivity().equals(plannedActivity)) {
                    if (paLabel.appliesToRepetition(sa.getRepetitionNumber())) {
                        sa.removeLabel(paLabel.getLabel());
                    }
                }
            }
        }
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }
}
