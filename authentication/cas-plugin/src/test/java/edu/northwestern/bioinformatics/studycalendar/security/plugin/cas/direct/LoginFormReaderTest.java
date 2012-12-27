/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rhett Sutphin
 */
public class LoginFormReaderTest extends TestCase {
    public void testCanExtractLoginTicketFromRubyCASServerLoginPage() throws Exception {
        assertLoginTicketIn("rubycas-login.html", "LT-1250704450r475348D359F974A670");
    }

    public void testCanExtractLoginTicketFromJasigCASServerLoginPage() throws Exception {
        assertLoginTicketIn("jasig-login.html",
            "_c8B00714F-26E8-B6FA-4E45-731B3FDB1440_k825D6395-3658-E499-B959-FC04976460BF");
    }

    public void testCanExtractLoginTicketFromUamsCASServerLoginPage() throws Exception {
        assertLoginTicketIn("uams-login.html",
            "_c6B21E859-BBD1-23A3-FCA6-89E5122F9014_k394073A9-6E82-B9E1-06EE-1F3CE2543CB6");
    }

    public void testCannotGetLTFromNonLoginPage() throws Exception {
        try {
            readerFor("random.html").getLoginTicket();
            fail("Exception not thrown");
        } catch (CasDirectException cde) {
            assertEquals("The CAS login form is missing the lt input which is required by the CAS protocol",
                cde.getMessage());
        }
    }

    private void assertLoginTicketIn(String htmlFile, String expectedLT) throws IOException {
        LoginFormReader reader = readerFor(htmlFile);
        assertEquals("Login ticket not found", expectedLT, reader.getLoginTicket());
    }

    private LoginFormReader readerFor(String filename) throws IOException {
        InputStream stream = getClass().getResourceAsStream('/' + filename);
        return new LoginFormReader(IOUtils.toString(stream));
    }
}
