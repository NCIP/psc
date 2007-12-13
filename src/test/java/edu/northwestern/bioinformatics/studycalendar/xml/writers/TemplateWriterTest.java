package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator.TEMPLATE_VALIDATOR_INSTANCE;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class TemplateWriterTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private Study study;
    private TemplateWriter witer;

    protected void setUp() throws Exception {
        super.setUp();

        witer = new TemplateWriter();

        study = Fixtures.createBlankTemplate();
    }

    public void testContainsRoot() throws Exception {
        String output = createAndValidateXml(study);

        assertContains(output, TemplateWriter.ROOT_START);
        assertContains(output, TemplateWriter.ROOT_END);
    }


    /* Test Helpers */

    public String createAndValidateXml(Study study) {
        byte[] byteOutput = witer.writeTemplate(study);

        validate(byteOutput);

        return convertToString(byteOutput);
    }

    private void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, StringUtils.EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }

    private String convertToString(byte[] byteOutput) {
        String output = new String(byteOutput);
        log.debug("XML: {}", output);
        return output;
    }
}
