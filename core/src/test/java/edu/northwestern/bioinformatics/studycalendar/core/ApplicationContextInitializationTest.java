/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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
        ApplicationContext context = getDeployedApplicationContext();
        // no exceptions
        assertPositive("No bean definitions loaded", context.getBeanDefinitionCount());
    }

    public void testBuildInfoTimestampIsParsed() throws Exception {
        BuildInfo buildInfo = (BuildInfo) getDeployedApplicationContext().getBean("buildInfo");
        assertNotNull(buildInfo.getTimestamp());
    }
}
