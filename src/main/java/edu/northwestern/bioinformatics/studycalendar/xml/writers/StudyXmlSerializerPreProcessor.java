package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class StudyXmlSerializerPreProcessor {
    private AmendmentService amendmentService;
    private TemplateDevelopmentService templateDevelopmentService;

    public void process(Study study) {
//        amendmentService.deleteDevelopmentAmendmentOnly(study);
        templateDevelopmentService.deleteDevelopmentAmendmentOnly(study);
    }

    /////// Bean Setters

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setTemplateDevelopmentService(TemplateDevelopmentService templateDevelopmentService) {
        this.templateDevelopmentService = templateDevelopmentService;
    }
}
