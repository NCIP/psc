package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.xml.readers.MultipartFileActivityLoader;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImportActivitiesCommand implements Validatable {
    private static Logger log = LoggerFactory.getLogger(ImportActivitiesCommand.class);

    private MultipartFileActivityLoader activityLoader;
    private MultipartFile activitiesFile;
    private Integer returnToPeriodId;

    public void apply() throws Exception {
        activityLoader.loadData(activitiesFile);
    }

    public void validate(Errors errors) {

        if (activitiesFile.isEmpty()) {
            errors.reject("error.activities.file.not.specified");
            return;
        }

        try {
            ValidationUtils.invokeValidator(new XmlValidator(Schema.activities), activitiesFile.getInputStream() , errors);
        } catch (IOException ioe) {
            errors.reject("error.problem.reading.file", Schema.activities.title());
            log.debug("Error reading file {} because {}", Schema.activities.title(), ioe.getMessage());
        }
        
    }

    // Field setters and getters
    public void setActivitiesFile(MultipartFile activitiesFile) {
        this.activitiesFile = activitiesFile;
    }

    public MultipartFile getActivitiesFile() {
        return activitiesFile;
    }

    public void setActivityLoader(MultipartFileActivityLoader activityLoader) {
        this.activityLoader = activityLoader;
    }

    public Integer getReturnToPeriodId() {
        return returnToPeriodId;
    }

    public void setReturnToPeriodId(Integer returnToPeriodId) {
        this.returnToPeriodId = returnToPeriodId;
    }
}
