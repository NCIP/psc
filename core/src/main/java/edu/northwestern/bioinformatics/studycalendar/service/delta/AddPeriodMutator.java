/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class AddPeriodMutator extends CollectionAddMutator {
    private SubjectService subjectService;

    public AddPeriodMutator(Add change, PeriodDao dao, SubjectService subjectService) {
        super(change, dao);
        this.subjectService = subjectService;
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        Period newPeriod = (Period) findChild();
        Revision revision = change.getDelta().getRevision();
        for (ScheduledStudySegment scheduledStudySegment : findMatchingStudySegments(calendar)) {
            subjectService.schedulePeriod(newPeriod,
                // TODO: make this cast unnecessary
                (Amendment) revision, "Period added in amendment " + revision.getDisplayName(),
                scheduledStudySegment);
        }
    }

    private Collection<ScheduledStudySegment> findMatchingStudySegments(ScheduledCalendar cal) {
        return cal.getScheduledStudySegmentsFor((StudySegment) change.getDelta().getNode());
    }
}
      