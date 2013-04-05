/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Parses the HTML presented by as CAS server's login page.  The field names it reads are
 * specified by the <a href="http://www.jasig.org/cas/protocol">CAS protocol</a>.
 *
 * @author Rhett Sutphin
 */
public class LoginFormReader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String body;
    private Document document;

    public LoginFormReader(String body) {
        if (body == null) throw new NullPointerException("The body param is required");
        this.body = body;
    }

    public String getLoginTicket() {
        NodeList inputs = getDocument().getElementsByTagName("input");
        for (int i = 0 ; i < inputs.getLength() ; i++) {
            Element input = (Element) inputs.item(i);
            if ("lt".equals(input.getAttribute("name"))) {
                return input.getAttribute("value");
            }
        }
        throw new CasDirectException(
            "The CAS login form is missing the lt input which is required by the CAS protocol");
    }

    protected synchronized Document getDocument() {
        if (document == null) {
            InputSource source = new InputSource(new StringReader(body));
            try {
                document = new HtmlDocumentBuilder().parse(source);
            } catch (SAXException e) {
                log.error("Could not parse CAS login form", e);
                throw new CasDirectException("Could not parse CAS login form", e);
            } catch (IOException e) {
                log.error("Could not parse CAS login form", e);
                throw new CasDirectException("Could not parse CAS login form", e);
            }
        }
        return document;
    }
}
