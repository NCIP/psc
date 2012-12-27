/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import java.util.Arrays;

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
    
    public void testExtractAllWhenNoQueryString() throws Exception {
        request.setResourceRef("http://something/foo");
        assertTrue(QueryParameters.Q.extractAllFrom(request).isEmpty());
    }

    public void testExtractAllWhenOne() throws Exception {
        request.setResourceRef("http://something/foo?q=bar+baz");
        assertEquals(Arrays.asList("bar baz"), QueryParameters.Q.extractAllFrom(request));
    }
    
    public void testExtractAllWhenMultiple() throws Exception {
        request.setResourceRef("http://something/foo?q=bar+baz&q=quux&study=EG");
        assertEquals(Arrays.asList("bar baz", "quux"), QueryParameters.Q.extractAllFrom(request));
    }

    public void testPutIn() throws Exception {
        request.setResourceRef("http://something/foo");
        QueryParameters.Q.putIn(request, "bar");
        assertEquals("http://something/foo?q=bar", request.getResourceRef().toString());
    }

    public void testPutInSeveral() throws Exception {
        request.setResourceRef("http://something/foo");
        QueryParameters.Q.putIn(request, "bar");
        QueryParameters.Q.putIn(request, "quux");
        assertEquals("http://something/foo?q=bar&q=quux", request.getResourceRef().toString());
    }

    public void testReplaceIn() throws Exception {
        request.setResourceRef("http://something/foo");
        QueryParameters.LABEL.putIn(request, "zip");
        QueryParameters.Q.replaceIn(request, "bar");
        QueryParameters.Q.replaceIn(request, "quux");
        assertEquals("http://something/foo?label=zip&q=quux", request.getResourceRef().toString());
    }

    public void testRemoveFrom() throws Exception {
        request.setResourceRef("http://something/foo");
        QueryParameters.LABEL.putIn(request, "zip");
        QueryParameters.Q.putIn(request, "bar");
        QueryParameters.Q.removeFrom(request);
        assertEquals("http://something/foo?label=zip", request.getResourceRef().toString());
    }
}
