/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;

/**
 * Provides helper for treating beans created through java code as though
 * they were created by an application context.
 *
 * @author Rhett Sutphin
 */
public class SpringBeanConfigurationTools {
    private SpringBeanConfigurationTools() { }

    /**
     * Evaluate all the special spring interfaces on the given bean
     * as though it was created in the given application context.
     *
     * @return the same bean (for chaining)
     */
    public static <T> T prepareBean(ApplicationContext parent, T bean) {
        return initializeBean(makeAware(parent, bean));
    }

    /**
     * Apply all the basic <code>*Aware</code> interfaces to the given bean.
     *
     * @return the same bean (for chaining)
     *
     * @see org.springframework.context.ApplicationContextAware
     * @see org.springframework.context.ApplicationEventPublisherAware
     * @see org.springframework.context.MessageSourceAware
     * @see org.springframework.context.ResourceLoaderAware
     * @see org.springframework.beans.factory.BeanFactoryAware
     */
    public static <T> T makeAware(ApplicationContext applicationContext, T bean) {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(applicationContext);
        }

        // Copied from org.springframework.context.support.ApplicationContextAwareProcessor
        // which was made package-local in Spring 2.5.
        if (bean instanceof ResourceLoaderAware) {
            ((ResourceLoaderAware) bean).setResourceLoader(applicationContext);
        }
        if (bean instanceof ApplicationEventPublisherAware) {
            ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(applicationContext);
        }
        if (bean instanceof MessageSourceAware) {
            ((MessageSourceAware) bean).setMessageSource(applicationContext);
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
        }

        return bean;
    }

    /**
     * Call {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()} on
     * the bean, if necessary, and translate the exception.
     *
     * @return the same bean (for chaining)
     */
    public static <T> T initializeBean(T bean) {
        if (bean instanceof InitializingBean) {
            try {
                ((InitializingBean) bean).afterPropertiesSet();
            } catch (Exception e) {
                throw new StudyCalendarSystemException("Initializing instance of %s failed",
                    e, bean.getClass().getName());
            }
        }
        return bean;
    }
}
