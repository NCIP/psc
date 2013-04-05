/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

/**
 * @author Rhett Sutphin
 */
public class LiteralTextViewTest extends WebTestCase {
    public void testRender() throws Exception {
        LiteralTextView view = new LiteralTextView("foo bar");
        view.render(null, request, response);
        assertEquals("foo bar", response.getContentAsString());
    }
    
    public void testDefaultContentType() throws Exception {
        LiteralTextView view = new LiteralTextView("quux");
        assertEquals("text/plain", view.getContentType());
    }

    public void testContentTypeRespected() throws Exception {
        LiteralTextView view = new LiteralTextView("text/html", "<b>foo!</b>");
        assertEquals("text/html", view.getContentType());
        view.render(null, request, response);
        assertEquals("text/html", response.getContentType());
    }
}
