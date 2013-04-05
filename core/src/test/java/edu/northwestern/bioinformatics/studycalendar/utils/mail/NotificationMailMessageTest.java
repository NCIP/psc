/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

/**
 * @author Jalpa Patel
 */
public class NotificationMailMessageTest extends MailMessageTestCase<NotificationMailMessage> {
    private static final String APPLICATION_PATH = "http://psc.deployed.path";
    private static final String SUBJECT = "Test subject for test e-mail";
    private static final String MESSAGE = "This message contains test message for e-mail for testing";

    protected void setUp() throws Exception {
        super.setUp();
        getMailMessageFactory().setApplicationPath(APPLICATION_PATH);
    }

    public void testSubject() {
        String messageSubject = createMessage().getSubject();
        assertEquals(messageSubject, "[Study Calendar]".concat(SUBJECT));
    }

    public void testMessageContents() {
        String msg = getMessageText();
        assertContains("Message doesn't contain email message", msg, MESSAGE);
        assertContains("Message doesn't contain psc link", msg, APPLICATION_PATH);
    }

    @Override
    protected NotificationMailMessage createMessage() {
        return getMailMessageFactory().createNotificationMailMessage(SUBJECT, MESSAGE);
    }
}
