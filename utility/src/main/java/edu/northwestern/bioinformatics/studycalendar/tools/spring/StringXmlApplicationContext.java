package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;

/**
 * @author Rhett Sutphin
 */
public class StringXmlApplicationContext extends GenericApplicationContext {
    public StringXmlApplicationContext(String xml) {
        this(null, xml);
    }

    public StringXmlApplicationContext(ApplicationContext parent, String xml) {
        this(parent, xml, Thread.currentThread().getContextClassLoader());
    }

    public StringXmlApplicationContext(ApplicationContext parent, String xml, ClassLoader beanClassLoader) {
        super(parent);
        ByteArrayResource xmlResource = new ByteArrayResource(xml.getBytes());

        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(this);
        xmlReader.loadBeanDefinitions(xmlResource);
        xmlReader.setBeanClassLoader(beanClassLoader);
        this.setClassLoader(beanClassLoader);

        refresh();
    }
}
