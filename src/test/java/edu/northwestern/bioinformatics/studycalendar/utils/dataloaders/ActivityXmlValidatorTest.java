package edu.northwestern.bioinformatics.studycalendar.utils.dataloaders;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;

public class ActivityXmlValidatorTest extends StudyCalendarTestCase {
    private String schema;
    private FileInputStream valid, invalid;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        schema = getClass().getClassLoader().getResource("activities.xsd").getFile();

        valid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/utils/dataloaders/data/ActivityXmlReaderTest.xml");
        invalid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/utils/dataloaders/data/ActivityXmlReaderTest-Invalid.xml");
    }

    public void testValidateForValidXml() throws Exception {

        // 1. Specify you want a factory for XSD
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        // 2. Load the specific schema you want.
        File schemaLocation = new File(schema);

        // 3. Compile the schema.
        Schema schema = factory.newSchema(schemaLocation);

        // 4. Get a validator from the schema.
        Validator validator = schema.newValidator();

        // 5. Parse the document you want to check.
        Source source = new StreamSource(valid);

        // 6. Check the document
        try {
            validator.validate(source);
        }
        catch (SAXException ex) {
            fail("Validator should not throw exception since, input xml is valid: " + ex.getMessage());
        }

    }

    public void testValidateForInvalidXml() throws Exception {
        // 1. Specify you want a factory for XSD
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        // 2. Load the specific schema you want.
        File schemaLocation = new File(schema);

        // 3. Compile the schema.
        Schema schema = factory.newSchema(schemaLocation);

        // 4. Get a validator from the schema.
        Validator validator = schema.newValidator();

        // 5. Parse the document you want to check.
        Source source = new StreamSource(invalid);

        // 6. Check the document
        try {
            validator.validate(source);
            fail("Validator should throw exception since, input xml is invalid ");
        }
        catch (SAXException ex) {
            // ok
        }
    }

}
