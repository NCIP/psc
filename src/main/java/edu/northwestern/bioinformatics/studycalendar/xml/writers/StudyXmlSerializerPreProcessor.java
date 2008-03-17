package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class StudyXmlSerializerPreProcessor {
    private AmendmentService amendmentService;

    public void process(Study study) {
        amendmentService.deleteDevelopmentAmendmentOnly(study);
    }

    /////// Bean Setters

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }
}
