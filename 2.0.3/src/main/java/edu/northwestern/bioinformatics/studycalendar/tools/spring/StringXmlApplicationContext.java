package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

/**
 * @author Rhett Sutphin
 */
public class StringXmlApplicationContext extends AbstractXmlApplicationContext {
    private Resource[] resource;

    public StringXmlApplicationContext(String xml, ApplicationContext parent) {
        super(parent);
        byte[] bytes = xml.getBytes();
        resource = new Resource[] { new ByteArrayResource(bytes) };
        refresh();
    }

    protected Resource[] getConfigResources() {
        return resource;
    }
}
