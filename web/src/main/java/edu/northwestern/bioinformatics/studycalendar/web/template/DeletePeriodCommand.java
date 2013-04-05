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
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.validation.Errors;

import java.util.Collection;

public class DeletePeriodCommand implements PeriodCommand {
    private StudySegment studySegment;
    private Period period;
    private AmendmentService amendmentService;
    private TemplateService templateService;

    public DeletePeriodCommand(
        Period p, StudySegment studySegment, AmendmentService amendmentService, TemplateService templateService
    ) {
        this.period = p;
        this.studySegment = studySegment;

        this.amendmentService = amendmentService;
        this.templateService = templateService;
    }

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        return ResourceAuthorization.createTemplateManagementAuthorizations(
            getStudySegment() == null ? null : templateService.findStudy(getStudySegment()),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
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