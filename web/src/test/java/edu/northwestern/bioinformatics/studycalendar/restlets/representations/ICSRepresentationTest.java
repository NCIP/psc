package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import junit.framework.TestCase;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import net.fortuna.ical4j.model.Calendar;

/**
 * @author Saurabh Agrawal
 * @crated Jan 12, 2009
 */
public class ICSRepresentationTest extends TestCase {

    private Representation representation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructor() {
        String icsFileName = "test";
        representation = new ICSRepresentation(new Calendar(), icsFileName);
        assertNotNull("representation must not be null", representation);
        assertEquals("Result is not right content type", MediaType.TEXT_CALENDAR, representation.getMediaType());
        assertEquals("File name does not match", icsFileName+".ics", representation.getDownloadName());
    }
}
