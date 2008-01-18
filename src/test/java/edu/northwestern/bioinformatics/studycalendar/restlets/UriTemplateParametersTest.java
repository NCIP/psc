package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;
import org.restlet.data.Request;

/**
 * @author Rhett Sutphin
 */
public class UriTemplateParametersTest extends StudyCalendarTestCase {
    public void testAttributeName() throws Exception {
        assertEquals("study-identifier", STUDY_IDENTIFIER.attributeName());
    }

    public void testExtractFromRequest() throws Exception {
        Request request = new Request();
        request.getAttributes().put(SOURCE_NAME.attributeName(), "Ear");

        assertEquals("Ear", SOURCE_NAME.extractFrom(request));
    }
    
    public void testExtractFromRequestUrlDecodes() throws Exception {
        Request request = new Request();
        request.getAttributes().put(SOURCE_NAME.attributeName(), "Sinus%20cavity");

        assertEquals("Sinus cavity", SOURCE_NAME.extractFrom(request));
    }
}
