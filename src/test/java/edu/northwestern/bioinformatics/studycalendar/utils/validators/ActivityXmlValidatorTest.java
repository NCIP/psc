package edu.northwestern.bioinformatics.studycalendar.utils.validators;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;

import java.io.FileInputStream;
import java.io.InputStream;

public class ActivityXmlValidatorTest extends StudyCalendarTestCase {
    private InputStream valid, invalid;

    protected void setUp() throws Exception {
        super.setUp();

        valid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/utils/dataloaders/data/ActivityXmlReaderTest.xml");
        invalid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/utils/dataloaders/data/ActivityXmlReaderTest-Invalid.xml");
    }

    public void testValidate() {
        BindException errors = new BindException(valid, StringUtils.EMPTY);
        ValidationUtils.invokeValidator(new XmlValidator(Schema.activities), valid, errors);

        assertFalse(errors.hasErrors());
    }

    public void testInvalid() {
        BindException errors = new BindException(invalid, StringUtils.EMPTY);
        ValidationUtils.invokeValidator(new XmlValidator(Schema.activities), invalid, errors);

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.file.not.valid", errors.getGlobalError().getCode());
    }
}
