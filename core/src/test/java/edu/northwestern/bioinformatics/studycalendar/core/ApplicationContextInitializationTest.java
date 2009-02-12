package edu.northwestern.bioinformatics.studycalendar.core;

import gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions;
import gov.nih.nci.cabig.ctms.tools.BuildInfo;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 * These tests are intended to verify that the various application contexts will all load when
 * the application is deployed.
 *
 * @author Rhett Sutphin
 */
public class ApplicationContextInitializationTest extends StudyCalendarTestCase {
    public void testApplicationContextItself() throws Exception {
        ApplicationContext context = StudyCalendarApplicationContextTestHelper.getDeployedApplicationContext();
        // no exceptions
        assertPositive("No bean definitions loaded", context.getBeanDefinitionCount());
    }

    public void testBuildInfoTimestampIsParsed() throws Exception {
        BuildInfo buildInfo = (BuildInfo) StudyCalendarTestCase.getDeployedApplicationContext().getBean("buildInfo");
        assertNotNull(buildInfo.getTimestamp());
        // note that this assertion will fail if you build and then run the tests much later
        MoreJUnitAssertions.assertDatesClose("Build timestamp is not recent (this test will fail if you run it a long time after you build)",
            new Date(), buildInfo.getTimestamp(), 3 * 60 * 60 * 1000 /* 3 hours */);
    }
}
