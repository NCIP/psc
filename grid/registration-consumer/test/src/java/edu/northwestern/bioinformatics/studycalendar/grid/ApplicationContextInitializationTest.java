/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Jalpa Patel
 */
public class ApplicationContextInitializationTest extends TestCase {
    public void testApplicationContextLoads() throws Exception {
        ApplicationContext loaded = new ClassPathXmlApplicationContext("applicationContext-grid.xml");
        assertTrue("No beans loaded", loaded.getBeanDefinitionCount() > 0);
    }
}
