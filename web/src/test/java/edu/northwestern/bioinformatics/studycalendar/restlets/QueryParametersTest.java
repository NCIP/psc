package edu.northwestern.bioinformatics.studycalendar.restlets;

/**
 * @author Rhett Sutphin
 */
public class QueryParametersTest extends RestletTestCase {
    public void testExtractWhenPresent() throws Exception {
        request.setResourceRef("http://something/foo?q=bar+baz");
        assertEquals("bar baz", QueryParameters.Q.extractFrom(request));
    }

    public void testExtractWhenNotPresent() throws Exception {
        request.setResourceRef("http://something/foo?type-id=4");
        assertNull(QueryParameters.Q.extractFrom(request));
    }
    
    public void testExtractWhenNoQueryString() throws Exception {
        request.setResourceRef("http://something/foo");
        assertNull(QueryParameters.Q.extractFrom(request));
    }
    
    public void testPutIn() throws Exception {
        request.setResourceRef("http://something/foo");
        QueryParameters.Q.putIn(request, "bar");
        assertEquals("http://something/foo?q=bar", request.getResourceRef().toString());
    }
}
