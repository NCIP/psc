package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public boolean apply() {
        amendmentService.removePeriod(getPeriod(), getStudySegment());
        return false;
    }


    ////// CONFIGURATION

    public Period getPeriod() {
        return period;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }
}