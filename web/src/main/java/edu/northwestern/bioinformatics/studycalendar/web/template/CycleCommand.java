/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;


import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.springframework.validation.Errors;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.nwu.bioinformatics.commons.spring.Validatable;

/**
 * @author Jalpa Patel
 * Date: Aug 26, 2008
*/
public class CycleCommand implements Validatable {
    private TemplateService templateService;
    private Integer cycleLength;
    private StudySegment studySegment;
    private StudySegment oldStudySegment;
    private AmendmentService amendmentService;
    public CycleCommand(TemplateService templateService,AmendmentService amendmentService) {
        this.templateService = templateService;
        this.amendmentService = amendmentService;
    }

    public void apply() {
        oldStudySegment = (StudySegment) studySegment.transientClone();
        amendmentService.updateDevelopmentAmendmentAndSave(studySegment,
                PropertyChange.create("cycleLength", oldStudySegment.getCycleLength(), cycleLength));
    }


    ////// BOUND PROPERTIES

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }

    public Integer getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(Integer cycleLength) {
        this.cycleLength = cycleLength;
    }

    public void validate(Errors errors) {
        if (getCycleLength() != null && getCycleLength() <= 0) {
            errors.rejectValue("cycleLength","Cycle Length must be a positive number.");
        }
    }
}
