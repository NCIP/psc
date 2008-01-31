package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.MockConfiguration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockServletContext;

/**
 * @author rsutphin
 */
public abstract class MailMessageTestCase<M extends SimpleMailMessage> extends StudyCalendarTestCase {
    protected static final MockServletContext servletContext = new MockServletContext();

    private MailMessageFactory mailMessageFactory;
    private MockConfiguration configuration;

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
        getConfiguration().set(Configuration.MAIL_REPLY_TO, expected);
        assertEquals(expected, createMessage().getReplyTo());
    }

    public MailMessageFactory getMailMessageFactory() {
        return mailMessageFactory;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
