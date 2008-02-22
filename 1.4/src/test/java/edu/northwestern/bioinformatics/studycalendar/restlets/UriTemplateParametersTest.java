package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class UriTemplateParametersTest extends StudyCalendarTestCase {
    public void testAttributeName() throws Exception {
        assertEquals("study_identifier", UriTemplateParameters.STUDY_IDENTIFIER.attributeName());
    }
}
