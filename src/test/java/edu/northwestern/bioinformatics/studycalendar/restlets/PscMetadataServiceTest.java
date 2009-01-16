package edu.northwestern.bioinformatics.studycalendar.restlets;

import junit.framework.TestCase;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;

/**
 * @author Saurabh Agrawal
 * @crated Jan 16, 2009
 */
public class PscMetadataServiceTest extends TestCase {

    private PscMetadataService metadataService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        metadataService = new PscMetadataService();

    }

    public void testConstructor() {
        Metadata metadata = metadataService.getMetadata("ics");
        assertNotNull("must support ics extention", metadata);
        assertEquals("must be of type calendar", MediaType.TEXT_CALENDAR, metadata);


    }
}
