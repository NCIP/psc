package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.restlets.representations.PscWadlRepresentation;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import java.util.List;

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
        assertContains(template.toString(), "http://research.sun.com/wadl/2006/10"); // WADL namespace
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

    public void testResourceVariants() throws Exception {
        doGet();
        List<Variant> variants = getResource().getVariants();
        assertEquals("Quantity of the variants is incorrect ", 3, variants.size());
    }


    public void testResourceRepresentationIsHtml() throws Exception {
        doGet();
        List<Variant> variants = getResource().getVariants();
        Representation represent = getResource().represent(variants.get(0));
        assertEquals("Response is not HTML", MediaType.TEXT_HTML, represent.getMediaType());
        assertContains(represent.getText(), "<html");
        assertContains(represent.getText(), "Patient Study Calendar RESTful API");
    }


    public void testResourceRepresentationIsXsd() throws Exception {
        doGet();
        List<Variant> variants = getResource().getVariants();
        Representation represent = getResource().represent(variants.get(1));
        assertNotNull("Could not get the representation ", represent);
        assertEquals("Response is not HTML", MediaType.APPLICATION_W3C_SCHEMA_XML, represent.getMediaType());
        assertContains(represent.getText(), "<xsd:schema");
    }

   public void testResourceRepresentationIsWadl() throws Exception {
        doGet();
        List<Variant> variants = getResource().getVariants();
        Representation represent = getResource().represent(variants.get(2));
        assertNotNull("Could not get the representation ", represent);
        assertEquals("Response is not HTML", MediaType.APPLICATION_WADL_XML, represent.getMediaType());
        assertContains(represent.getText(), "<application");
        assertContains(represent.getText(), "http://research.sun.com/wadl/2006/10"); // WADL namespace
        assertContains(represent.getText(), String.format("<resources base=\"%s\">", ROOT_URI)); // WADL root element
    }
}

