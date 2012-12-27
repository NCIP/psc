/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author Rhett Sutphin
 */
public class ContextClassLoaderFactoryBean implements FactoryBean {
    public Object getObject() throws Exception {
        return Thread.currentThread().getContextClassLoader();
    }

    public Class getObjectType() {
        return ClassLoader.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
