package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator.TEMPLATE_VALIDATOR_INSTANCE;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class StudyXmlWriterTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private Study study;
    private StudyXmlWriter witer;

    protected void setUp() throws Exception {
        super.setUp();

        witer = new StudyXmlWriter();

        study = Fixtures.createBasicTemplate();
    }

    public void testContainsRoot() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.ROOT);
    }

    public void testContainsPlannedCalendar() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.PLANNDED_CALENDAR);
    }

    public void testContainsAmendment() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.AMENDMENT);
    }

    public void testContainsDelta() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.PLANNED_CALENDAR_DELTA);
    }

    public void testContainsAddChange() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.ADD);
    }

    public void testContainsEpoch() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.EPOCH);
    }

    public void testContainsStudySegment() throws Exception {
        String output = createAndValidateXml(study);

        assertContainsTag(output, StudyXmlWriter.STUDY_SEGMENT);
    }

    /* Test Helpers */

    public String createAndValidateXml(Study study) throws Exception{
        String s = witer.createStudyXml(study);
        log.debug("XML: {}", s);
        
        validate(s.getBytes());

        return s;
    }

    private void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, StringUtils.EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }


    private void assertContainsTag(String output, String element) {
        assertContains(output, toTag(element));
    }

    private String toTag(String element) {
        return "<" + element;
    }

}
