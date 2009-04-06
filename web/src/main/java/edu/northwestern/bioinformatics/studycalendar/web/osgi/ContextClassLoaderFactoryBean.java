package edu.northwestern.bioinformatics.studycalendar.web.osgi;

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
