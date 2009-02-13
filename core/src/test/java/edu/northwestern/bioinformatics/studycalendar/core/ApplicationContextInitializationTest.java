package edu.northwestern.bioinformatics.studycalendar.core;

import gov.nih.nci.cabig.ctms.tools.BuildInfo;
import org.springframework.context.ApplicationContext;

/**
 * These tests are intended to verify that the various application contexts will all load when
 * the application is deployed.
 *
 * @author Rhett Sutphin
 */
public class ApplicationContextInitializationTest extends StudyCalendarTestCase {
    public void testApplicationContextItself() throws Exception {
        ApplicationContext context = StudyCalendarApplicationContextBuilder.getDeployedApplicationContext();
        // no exceptions
        assertPositive("No bean definitions loaded", context.getBeanDefinitionCount());
    }

    public void testBuildInfoTimestampIsParsed() throws Exception {
        BuildInfo buildInfo = (BuildInfo) StudyCalendarTestCase.getDeployedApplicationContext().getBean("buildInfo");
        assertNotNull(buildInfo.getTimestamp());
    }
}
