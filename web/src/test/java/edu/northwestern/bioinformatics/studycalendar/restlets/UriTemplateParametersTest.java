/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.*;
import org.restlet.Request;

/**
 * @author Rhett Sutphin
 */
public class UriTemplateParametersTest extends StudyCalendarTestCase {
    public void testAttributeName() throws Exception {
        assertEquals("study-identifier", STUDY_IDENTIFIER.attributeName());
    }

    public void testExtractFromRequest() throws Exception {
        Request request = new Request();
        request.getAttributes().put(ACTIVITY_SOURCE_NAME.attributeName(), "Ear");

        assertEquals("Ear", ACTIVITY_SOURCE_NAME.extractFrom(request));
    }

    public void testExtractFromRequestWhenNotPresent() throws Exception {
        assertNull(SITE_IDENTIFIER.extractFrom(new Request()));
    }
    
    public void testExtractFromRequestUrlDecodes() throws Exception {
        Request request = new Request();
        request.getAttributes().put(ACTIVITY_SOURCE_NAME.attributeName(), "Sinus%20cavity");

        assertEquals("Sinus cavity", ACTIVITY_SOURCE_NAME.extractFrom(request));
    }
    
    public void testExtractDecodesNullsIntoSlashes() throws Exception {
        Request request = new Request();
        request.getAttributes().put(ACTIVITY_SOURCE_NAME.attributeName(), "Sinus%04Cavity");

        assertEquals("Sinus/Cavity", ACTIVITY_SOURCE_NAME.extractFrom(request));
    }
}
