package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.restlet.util.Template;

public class TemplateConfigurationPropertyTest extends StudyCalendarTestCase {
    private TemplateConfigurationProperty property;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        property = new TemplateConfigurationProperty("the.template");
    }

    public void testToStorageFormat() throws Exception {
        Template template = new Template("/a/long/{template}");
        assertEquals("/a/long/{template}", property.toStorageFormat(template));
    }
    
    public void testFromStorageFormat() throws Exception {
        assertEquals("/new/{pattern}", property.fromStorageFormat("/new/{pattern}").getPattern());
    }
}
