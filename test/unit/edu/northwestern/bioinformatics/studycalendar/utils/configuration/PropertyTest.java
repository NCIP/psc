package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class PropertyTest extends StudyCalendarTestCase {
    public void testGetName() throws Exception {
        assertEquals("Deployment name", Configuration.DEPLOYMENT_NAME.getName());
    }
    
    public void testGetDescription() throws Exception {
        assertEquals("The port on which to communicate with the SMTP server",
            Configuration.SMTP_PORT.getDescription());
    }
}
