/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

/**
 * @author rsutphin
 */
public class ExceptionMailMessageTest extends MailMessageTestCase<ExceptionMailMessage> {
    private static final List<String> MAILTO = Arrays.asList("whogets@thebadnews.com");
    private MockHttpServletRequest request;
    private Throwable exception;

    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest(servletContext);
        exception = new RuntimeException("Trouble");
        getConfiguration().set(Configuration.MAIL_EXCEPTIONS_TO, MAILTO);
    }

    public void testStackTraceIncluded() {
        String msg = getMessageText();

        assertContains(msg, "Stack trace:");
        assertContains(msg, "RuntimeException > Trouble");
    }

    public void testContentWhenMapsEmpty() {
        String msg = getMessageText();
        assertContains(msg, "\nNo request parameters\n");
        assertContains(msg, "\nNo request headers\n");
        assertContains(msg, "\nNo request attributes\n");
        assertContains(msg, "\nNo session attributes\n");
        assertContains(msg, "\nNo context init parameters\n");
        assertContains(msg, "\nNo cookies\n");

        // Due to their natures, the following are unclearable
        assertContains(msg, "\nRequest properties:\n");
        assertContains(msg, "\nApplication attributes:\n");
    }

    public void testFailingStringificationDoesNotPreventRendering() throws Exception {
        request.setAttribute("bad_one", new ToStringFailer("I have bad news"));

        String msg = getMessageText();
        assertContains(msg, "\n    bad_one\n        [error extracting value: java.lang.RuntimeException: I have bad news]\n");
    }

    public void testFailingStringificationOfCollectionElementDoesNotPreventRendering() throws Exception {
        request.setAttribute("bad_one", Arrays.asList("happy", new ToStringFailer("I refuse"), "happy"));
        String msg = getMessageText();
        assertContains(msg, "\n    bad_one\n        happy\n        [error extracting value: java.lang.RuntimeException: I refuse]\n        happy\n");
    }

    public void testParametersIncluded() {
        request.addParameter("Lethem", "Fortress of Solitude");
        request.addParameter("Dick", new String[] { "Ubik", "Valis", "The Man in the High Castle" });

        String msg = getMessageText();

        assertContains(msg, "\nRequest parameters:\n");
        assertContains(msg, "\n    Lethem\n        Fortress of Solitude");
        assertContains(msg, "\n    Dick\n        Ubik\n        Valis\n        The Man in the High Castle");
    }

    public void testHeadersIncluded() {
        request.addHeader("Lethem", "Fortress of Solitude");
        request.addHeader("Dick", new String[] { "Ubik", "Valis", "The Man in the High Castle" });

        String msg = getMessageText();

        assertContains(msg, "\nRequest headers:\n");
        assertContains(msg, "\n    Lethem\n        Fortress of Solitude");
        assertContains(msg, "\n    Dick\n        Ubik\n        Valis\n        The Man in the High Castle");
    }

    public void testRequestAttributesIncluded() {
        request.setAttribute("Little Ghost", "apparition");
        request.setAttribute("Life like", "weeds");

        String msg = getMessageText();

        assertContains(msg, "\nRequest attributes:\n");
        assertContains(msg, "\n    Little Ghost\n        apparition");
        assertContains(msg, "\n    Life like\n        weeds");
    }

    public void testSessionAttributesIncluded() {
        request.getSession(true).setAttribute("Little Ghost", "apparition");
        request.getSession(true).setAttribute("Life like", "weeds");
        request.getSession(true).setAttribute("Figure", 8);

        String msg = getMessageText();

        assertContains(msg, "\nSession attributes:\n");
        assertContains(msg, "\n    Little Ghost\n        apparition");
        assertContains(msg, "\n    Life like\n        weeds");
        assertContains(msg, "\n    Figure\n        8");
    }

    public void testApplicationAttributesIncluded() {
        servletContext.setAttribute("Little Ghost", "apparition");
        servletContext.setAttribute("Life like", "weeds");

        String msg = getMessageText();

        assertContains(msg, "\nApplication attributes:\n");
        assertContains(msg, "\n    Little Ghost\n        apparition");
        assertContains(msg, "\n    Life like\n        weeds");
    }

    public void testCookiesIncluded() {
        request.setCookies(new Cookie[] {
            new Cookie("ElliotSmith", "XO"),
            new Cookie("TheyMightBeGiants", "They Got Lost")
        });

        String msg = getMessageText();

        assertContains(msg, "\nCookies:\n");
        assertContains(msg, "\n    ElliotSmith\n        XO");
        assertContains(msg, "\n    TheyMightBeGiants\n        They Got Lost");
    }

    public void testRequestPropertiesIncluded() {
        request.setMethod("GET");
        String msg = getMessageText();

        assertContains(msg, "\nRequest properties:\n");
        assertContains(msg, "\n    method\n        GET");
        assertContains(msg, "\n    userPrincipal\n        [null]");
    }

    public void testContextInitParamsIncluded() {
        servletContext.addInitParameter("Start", "with a bang");
        servletContext.addInitParameter("Another", "piece");

        String msg = getMessageText();

        assertContains(msg, "\nContext init parameters:\n");
        assertContains(msg, "\n    Start\n        with a bang");
        assertContains(msg, "\n    Another\n        piece");
    }

    public void testPasswordFiltered() {
        request.addParameter("j_password", "schloessel");

        String msg = getMessageText();
        assertContains(msg, "\nRequest parameters:\n");
        assertContains(msg, "\n    j_password\n        [PASSWORD]\n");
        assertNotContains(msg, "schloessel");
    }

    public void testTo() {
        replayMocks();
        SimpleMailMessage msg = createMessage();
        assertEqualArrays(MAILTO.toArray(new String[MAILTO.size()]), msg.getTo());
        verifyMocks();
    }

    protected ExceptionMailMessage createMessage() {
        return getMailMessageFactory().createExceptionMailMessage(exception, request);
    }

    private static class ToStringFailer {
        private String msg;

        private ToStringFailer(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            throw new RuntimeException(msg);
        }
    }
}
