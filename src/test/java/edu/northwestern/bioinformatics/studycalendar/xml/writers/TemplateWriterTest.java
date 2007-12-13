package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static org.springframework.validation.ValidationUtils.invokeValidator;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;

public class TemplateWriterTest extends StudyCalendarTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testWriteTemplate() {
        String s =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<template xmlns=\"http://bioinformatics.northwestern.edu/ns/psc/template.xsd\"\n" +
                    "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "     xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc/template.xsd\">\n";

        s +=        "<amendment><delta><add></add></delta></amendment></template>";

        BindException errors = new BindException(s, StringUtils.EMPTY);
        invokeValidator(new XmlValidator(Schema.template), new ByteArrayInputStream(s.getBytes()), errors);


        assertFalse("Template xml should be error free", errors.hasErrors());
    }
}
