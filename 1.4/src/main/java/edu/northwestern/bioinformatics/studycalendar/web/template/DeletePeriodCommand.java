package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

public class DeletePeriodCommand implements PeriodCommand {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudySegment studySegment;
    private Period period;
    private AmendmentService amendmentService;

    public DeletePeriodCommand(Period p, StudySegment studySegment, AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
        this.studySegment = studySegment;
        this.period = p;
    }


    public void apply() {
        removePlannedActivities();
        removePeriod();
    }

    private void removePlannedActivities() {
        for (PlannedActivity toRemove : new ArrayList<PlannedActivity>(getPeriod().getPlannedActivities())) {
            amendmentService.updateDevelopmentAmendment(getPeriod(), Remove.create(toRemove));
        }
    }

    private void removePeriod(){
        amendmentService.updateDevelopmentAmendment(getStudySegment(), Remove.create(getPeriod()));
    }

    ////// CONFIGURATION

    public Period getPeriod() {
        return period;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }
}