package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.resource.TransformRepresentation;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author Nataliya Shurupova
 */
public class DocResourceTest extends ResourceTestCase<DocResource> {
    @Override
    protected DocResource createResource() {
        return (DocResource) getApiServletApplicationContext().getBean("docResource");
    }

    public void testGetWorks() throws Exception {
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testTemplateLoadable() throws Exception {
        Configuration conf
            = (Configuration) getApiServletApplicationContext().getBean("resourceFreemarkerConfiguration");
        assertNotNull("Could not load config", conf);
        Template template = conf.getTemplate(PscWadlRepresentation.PSC_WADL_PATH);
        assertContains(template.toString(), "<application");
        assertNotNull("Could not load template", template);
    }

    public void testGetContentIsHtml() throws Exception {
        doGet();
        String text = response.getEntity().getText();

        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Response is not HTML", MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertContains(text, "<html");
        assertContains(text, "Patient Study Calendar RESTful API"); // Title
    }
}

