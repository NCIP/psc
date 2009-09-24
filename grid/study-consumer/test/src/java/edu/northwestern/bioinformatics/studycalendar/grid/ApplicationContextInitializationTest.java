package edu.northwestern.bioinformatics.studycalendar.grid;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class ApplicationContextInitializationTest extends TestCase {
    public void testApplicationContextLoads() throws Exception {
        ApplicationContext loaded = new ClassPathXmlApplicationContext("applicationContext-studyConsumer-grid.xml");
        assertTrue("No beans loaded", loaded.getBeanDefinitionCount() > 0);
    }
}
