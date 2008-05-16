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

    // TODO: this is going to have to be an arbitrary revision at some point
    // (i.e. for Customizations)
    public void apply() {
        amendmentService.updateDevelopmentAmendment(getStudySegment(), Add.create(getPeriod()));
    }

    public void setPeriod(final Period period) {
        this.period = period;
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
