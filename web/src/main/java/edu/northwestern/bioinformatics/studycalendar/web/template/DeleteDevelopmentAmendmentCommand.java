/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;

/**
 * @author Rhett Sutphin
 */
public class DeleteDevelopmentAmendmentCommand {
    private AmendmentService amendmentService;
    private TemplateDevelopmentService templateDevelopmentService;

    private Study study;

    public DeleteDevelopmentAmendmentCommand(AmendmentService amendmentService, TemplateDevelopmentService templateDevelopmentService) {
        this.amendmentService = amendmentService;
        this.templateDevelopmentService = templateDevelopmentService;
    }

    public void apply() {
//        amendmentService.deleteDevelopmentAmendment(study);
        templateDevelopmentService.deleteDevelopmentAmendment(study);
    }

    ////// BOUND PROPERTIES

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
