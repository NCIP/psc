package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.web.MissingRequiredBoundProperty;

/**
 * @author Rhett Sutphin
 */
public class CopyPeriodCommand implements PeriodCommand {
    private TemplateDevelopmentService templateDevelopmentService;
    private Period selectedPeriod;
    private StudySegment studySegment;
    private Period copy;

    public CopyPeriodCommand(TemplateDevelopmentService templateDevelopmentService) {
        this.templateDevelopmentService = templateDevelopmentService;
    }

    ////// LOGIC

    public boolean apply() {
        if (studySegment == null) throw new MissingRequiredBoundProperty("studySegment");
        if (selectedPeriod == null) throw new MissingRequiredBoundProperty("selectedPeriod");
        copy = templateDevelopmentService.copyPeriod(selectedPeriod, studySegment);
        return true;
    }

    public Period getPeriod() {
        return copy;
    }

    ////// BOUND PROPERTIES

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setSelectedPeriod(Period selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }
}
