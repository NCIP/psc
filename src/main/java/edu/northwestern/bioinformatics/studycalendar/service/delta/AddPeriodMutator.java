package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
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
        for (ScheduledArm scheduledArm : findMatchingArms(calendar)) {
            subjectService.schedulePeriod(newPeriod,
                // TODO: make this cast unnecessary
                (Amendment) change.getDelta().getRevision(), scheduledArm);
        }
    }

    private Collection<ScheduledArm> findMatchingArms(ScheduledCalendar cal) {
        // Second cast works around a dumb javac bug
        return cal.getScheduledArmsFor((Arm) (PlanTreeNode) change.getDelta().getNode());
    }
}
