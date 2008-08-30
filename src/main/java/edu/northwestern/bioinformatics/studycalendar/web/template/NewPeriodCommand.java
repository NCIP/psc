package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Moses Hohman
 */
public class NewPeriodCommand implements PeriodCommand {
    private StudySegment studySegment;
    private Period period;
    private AmendmentService amendmentService;

    public NewPeriodCommand(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
        period = new Period();
    }

    public boolean apply() {
        amendmentService.updateDevelopmentAmendment(getStudySegment(), Add.create(getPeriod()));
        return false;
    }

    public Period getPeriod() {
        return period;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }
}
