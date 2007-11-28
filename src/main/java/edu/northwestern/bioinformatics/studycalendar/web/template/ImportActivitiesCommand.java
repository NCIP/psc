package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.utils.dataloaders.MultipartFileActivityLoader;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.Errors;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

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
        
        // 1. Specify you want a factory for XSD
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        URL schemaUrl = getClass().getClassLoader().getResource("activities.xsd");

        // 2. Load the specific schema you want.
        File schemaLocation = new File(schemaUrl.getFile());

        try {
            // 3. Compile the schema.
            Schema schema = factory.newSchema(schemaLocation);

            // 4. Get a validator from the schema.
            Validator validator = schema.newValidator();

            // 5. Parse the document you want to check.
            Source source = new StreamSource(getActivitiesFile().getInputStream());

            // 6. Check the document
            validator.validate(source);
            log.info("Activities file {} is valid.",  getActivitiesFile().getName());
        }
        catch (SAXException ex) {
            // TODO: get cause of SaxException and display in form other than cryptic SaxException message.
            errors.reject("error.activities.file.not.valid");
            log.debug("Activities file {} is not valid because ", getActivitiesFile().getName());
        }
        catch (IOException ioe) {
            errors.reject("error.problem.reading.activities.file");
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
