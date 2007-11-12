package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;

/**
 * @author Rhett Sutphin
 */
public class AddPlannedActivityMutator extends CollectionAddMutator {
    private ParticipantService participantService;
    private TemplateService templateService;

    public AddPlannedActivityMutator(
        Add change, PlannedActivityDao dao,
        ParticipantService participantService, TemplateService templateService
    ) {
        super(change, dao);
        this.participantService = participantService;
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
        Arm arm = templateService.findParent(period);

        for (ScheduledArm scheduledArm : calendar.getScheduledArmsFor(arm)) {
            participantService.schedulePlannedActivity(event, period, (Amendment) change.getDelta().getRevision(), scheduledArm);
        }
    }
}
