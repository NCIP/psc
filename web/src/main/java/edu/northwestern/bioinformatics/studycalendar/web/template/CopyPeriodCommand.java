/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.MissingRequiredBoundProperty;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.validation.Errors;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class CopyPeriodCommand implements PeriodCommand {
    private TemplateService templateService;
    private TemplateDevelopmentService templateDevelopmentService;
    private Period selectedPeriod;
    private StudySegment studySegment;
    private Period copy;

    public CopyPeriodCommand(TemplateService templateService, TemplateDevelopmentService templateDevelopmentService) {
        this.templateService = templateService;
        this.templateDevelopmentService = templateDevelopmentService;
    }

    ////// LOGIC

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        return ResourceAuthorization.createTemplateManagementAuthorizations(
            getStudySegment() == null ? null : templateService.findStudy(studySegment),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

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
