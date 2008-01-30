package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.service.ImportTemplateService;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImportTemplateXmlCommand implements Validatable {
    private static final Logger log = LoggerFactory.getLogger(ImportActivitiesCommand.class);

    MultipartFile studyXml;
    private ImportTemplateService service;

    public void apply() throws Exception {
        service.importTemplate(studyXml.getInputStream());
    }

    public void validate(Errors errors) {
        if (studyXml == null || studyXml.isEmpty()) {
            errors.reject("error.template.xml.not.specified");
            return;
        }

        try {
            invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, studyXml.getInputStream() , errors);
        } catch (IOException ioe) {
            errors.reject("error.problem.reading.file", Schema.template.title());
            log.debug("Error reading file {} because {}", Schema.template.title(), ioe.getMessage());
        }

    }

    ////// Setters and Getters
    public void setStudyXml(MultipartFile studyXml) {
        this.studyXml = studyXml;
    }

    public MultipartFile getStudyXml() {
        return studyXml;
    }

    public void setImportTemplateService(ImportTemplateService service) {
        this.service = service;
    }
}
