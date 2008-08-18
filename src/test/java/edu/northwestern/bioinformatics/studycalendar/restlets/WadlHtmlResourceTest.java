package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public class WadlHtmlResourceTest extends ResourceTestCase<WadlHtmlResource> {
    @Override
    protected WadlHtmlResource createResource() {
        return (WadlHtmlResource) getApiServletApplicationContext().getBean("wadlHtmlResource");
    }
    
    public void testGetWorks() throws Exception {
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
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
