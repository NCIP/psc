/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.configuration.MockConfiguration;
import static edu.northwestern.bioinformatics.studycalendar.configuration.Configuration.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockServletContext;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;

/**
 * @author rsutphin
 */
public abstract class MailMessageTestCase<M extends SimpleMailMessage> extends StudyCalendarTestCase {
    protected static final MockServletContext servletContext = new MockServletContext();

    private MailMessageFactory mailMessageFactory;
    private MockConfiguration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mailMessageFactory = (MailMessageFactory) getDeployedApplicationContext().getBean("mailMessageFactory");
        configuration = new MockConfiguration();
        mailMessageFactory.setConfiguration(configuration);
        mailMessageFactory.setServletContext(servletContext);
    }

    protected abstract M createMessage();

    protected String getMessageText() {
        return createMessage().getText();
    }

    public void testReplyTo() {
        String expected = "John Q. Developer";
        getConfiguration().set(MAIL_REPLY_TO, expected);
        assertEquals(expected, createMessage().getReplyTo());
    }

    public MailMessageFactory getMailMessageFactory() {
        return mailMessageFactory;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
