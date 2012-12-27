/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCoreApplicationContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

/**
 * This is a crappy one-off.  It would be better to shift initial setup into
 * a ruby script and support automating all the setup steps.
 *
 * @author Rhett Sutphin
 */
public class OneTimeSetup  {
    private static final Logger log = LoggerFactory.getLogger(OneTimeSetup.class);

    public static void main(String[] args) {
        String pscRoot = args[0];
        log.info("Using basedir {}", pscRoot);

        ApplicationContext applicationContext = null;
        try {
            applicationContext = new GenericApplicationContext(
                new XmlBeanFactory(new FileSystemResource(
                    new File(pscRoot, "test/restful-api/target/spec/resources/applicationContext.xml"))),
                StudyCalendarTestCoreApplicationContextBuilder.getApplicationContext()
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }

        RestfulApiTestInitializer initializer = (RestfulApiTestInitializer) applicationContext.getBean("databaseInitializer");
        initializer.oneTimeSetup();
        System.exit(0);
    }
}
