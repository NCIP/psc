package edu.northwestern.bioinformatics.studycalendar.utils.dataloaders;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;

public class ActivityXmlVerifierTest extends StudyCalendarTestCase {
    private String schema;
    private String input;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        schema = getClass().getClassLoader().getResource("activities.xsd").getFile();
//        input = getClass().getClassLoader().getResource("default-activities.xml").getFile();
        input = getClass().getClassLoader().getResource("applicationContext-utils.xml").getFile();
    }

    public void testVerify() throws Exception {

        // 1. Specify you want a factory for XSD
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        // 2. Load the specific schema you want.
        File schemaLocation = new File(schema);

        // 3. Compile the schema.
        Schema schema = factory.newSchema(schemaLocation);

        // 4. Get a validator from the schema.
        Validator validator = schema.newValidator();

        // 5. Parse the document you want to check.
        Source source = new StreamSource(input);

        // 6. Check the document
        try {
            validator.validate(source);
            System.out.println(input + " is valid.");
        }
        catch (SAXException ex) {
            System.out.println(input + " is not valid because ");
            System.out.println(ex.getMessage());
        }

    }

}
