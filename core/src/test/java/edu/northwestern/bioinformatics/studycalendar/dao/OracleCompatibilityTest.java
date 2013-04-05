/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.mapping.PersistentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class OracleCompatibilityTest extends StudyCalendarTestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @SuppressWarnings({ "unchecked" })
    public void testAllConfiguredSequencesLessThan30Characters() throws Exception {
        AnnotationSessionFactoryBean sessionFactoryBean
            = (AnnotationSessionFactoryBean) getDeployedApplicationContext().getBean("&sessionFactory");
        Iterator<PersistentClass> mappedClasses = sessionFactoryBean.getConfiguration().getClassMappings();
        while (mappedClasses.hasNext()) {
            PersistentClass pc =  mappedClasses.next();
            Class<?> clazz = pc.getMappedClass();
            GenericGenerator generator = clazz.getAnnotation(GenericGenerator.class);
            if (generator == null) {
                log.debug("{} has no declared generator", clazz.getName());
                continue;
            }
            Parameter sequence = null;
            for (Parameter parameter : generator.parameters()) {
                if ("sequence".equals(parameter.name())) {
                    sequence = parameter; break;
                }
            }
            if (sequence == null) {
                fail("Generator for " + clazz.getName() + " has no sequence parameter");
            }
            assertTrue("Oracle sequence for " + clazz.getName() + " is too long: " + sequence.value() + " (" + sequence.value().length() + " chars). It must be no more than 30 characters. Bering will generate an appropriately named sequence; be sure to configure the same one in the class annotation.",
                sequence.value().length() <= 30);
        }
    }
}
