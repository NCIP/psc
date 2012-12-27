/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.validation.Errors;

import java.util.Collection;

/**
 * @author Moses Hohman
 */
public class NewPeriodCommand implements PeriodCommand, PscAuthorizedCommand {
    private StudySegment studySegment;
    private Period period;

    private final AmendmentService amendmentService;
    private final TemplateService templateService;

    public NewPeriodCommand(AmendmentService amendmentService, TemplateService templateService) {
        this.amendmentService = amendmentService;
        this.templateService = templateService;
        period = new Period();
    }

    public boolean apply() {
        amendmentService.updateDevelopmentAmendment(getStudySegment(), Add.create(getPeriod()));
        return false;
    }

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        return ResourceAuthorization.createTemplateManagementAuthorizations(
            getStudySegment() == null ? null : templateService.findStudy(getStudySegment()),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
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
