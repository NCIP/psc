package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Status;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author Rhett Sutphin
 */
public class WadlResourceTest extends ResourceTestCase<WadlResource> {
    @Override
    protected WadlResource createResource() {
        return (WadlResource) getApiServletApplicationContext().getBean("wadlResource");
    }
    
    public void testGetWorks() throws Exception {
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetContentIsWadl() throws Exception {
        doGet();
        String text = response.getEntity().getText();
        assertResponseStatus(Status.SUCCESS_OK);
        assertContains(text, "<application"); // WADL root element
        assertContains(text, "http://research.sun.com/wadl/2006/10"); // WADL namespace
    }
    
    public void testGetContentContainsBaseUri() throws Exception {
        doGet();
        String text = response.getEntity().getText();
        assertResponseStatus(Status.SUCCESS_OK);
        assertContains(text, String.format("<resources base=\"%s\">", ROOT_URI)); // WADL root element
    }

    public void testTemplateLoadable() throws Exception {
        Configuration conf
            = (Configuration) getApiServletApplicationContext().getBean("resourceFreemarkerConfiguration");
        assertNotNull("Could not load config", conf);
        Template template = conf.getTemplate(PscWadlRepresentation.PSC_WADL_PATH);
        assertNotNull("Could not load template", template);
    }
}
