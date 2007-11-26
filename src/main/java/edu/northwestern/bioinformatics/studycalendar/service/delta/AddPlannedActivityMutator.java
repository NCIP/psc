package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;

/**
 * @author Rhett Sutphin
 */
public class AddPlannedActivityMutator extends CollectionAddMutator {
    private SubjectService subjectService;
    private TemplateService templateService;

    public AddPlannedActivityMutator(
        Add change, PlannedActivityDao dao,
        SubjectService subjectService, TemplateService templateService
    ) {
        super(change, dao);
        this.subjectService = subjectService;
        this.templateService = templateService;
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivity event = (PlannedActivity) findChild();
        // Second cast works around a dumb javac bug
        Period period = (Period) (PlanTreeNode) change.getDelta().getNode();
        StudySegment studySegment = templateService.findParent(period);

        for (ScheduledStudySegment scheduledStudySegment : calendar.getScheduledStudySegmentsFor(studySegment)) {
            subjectService.schedulePlannedActivity(event, period, (Amendment) change.getDelta().getRevision(), scheduledStudySegment);
        }
    }
}
