/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import static org.springframework.validation.ValidationUtils.invokeValidator;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.*;
import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImportActivitiesCommand implements Validatable {
    private static final Logger log = LoggerFactory.getLogger(ImportActivitiesCommand.class);

    private ImportActivitiesService service;
    private MultipartFile activitiesFile;
    private String activitiesType;

    public Source apply() throws Exception {
        if (getActivitiesType().toLowerCase().trim().equals("xml")) {
            return service.loadAndSave(activitiesFile.getInputStream());
        } else if (getActivitiesType().toLowerCase().trim().equals("csv")) {
            return service.loadAndSaveCSVFile(activitiesFile.getInputStream());
        }
        return null;
    }

    public void validate(Errors errors) {
        if (activitiesFile.isEmpty()) {
            errors.reject("error.activities.file.not.specified");
            return;
        }

        try {
            if (getActivitiesType().toLowerCase().trim().equals("xml")) {
                invokeValidator(SPRING_ACTIVITY_VALIDATOR_INSTANCE, activitiesFile.getInputStream(), errors);
            }
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

    public String getActivitiesType() {
        return activitiesType;
    }

    public void setActivitiesType(String activitiesType) {
        this.activitiesType = activitiesType;
    }

    public void setImportActivitiesService(ImportActivitiesService service) {
        this.service = service;
    }


}
