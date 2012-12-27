/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.configuration;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.restlet.routing.Template;

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
