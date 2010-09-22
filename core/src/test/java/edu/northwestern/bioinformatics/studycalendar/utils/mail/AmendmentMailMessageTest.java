package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import java.util.Calendar;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Jalpa Patel
 */
public class AmendmentMailMessageTest extends MailMessageTestCase<AmendmentMailMessage> {
    private static final String APPLICATION_PATH = "http://psc.deployed.path";
    private Amendment amendment;
    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        amendment = createAmendment("testAmendment", DateTools.createSqlDate(2010, Calendar.SEPTEMBER, 22), true);
        study = createNamedInstance("testStudy", Study.class);
    }

    public void testSubject() {
        String messageSubject = createMessage().getSubject();
        assertEquals(messageSubject, "[Study Calendar]testStudy has been amended");
    }

    public void testMessageForAmendment() {
        String msg = getMessageText();
        assertContains("Message doesn't contain study name", msg, study.getAssignedIdentifier());
        assertContains("Message doesn't contain amendment name", msg, amendment.getName());
        assertContains("Message doesn't contain amendment date", msg, "Sep 22, 2010");
        assertContains("Message doesn't contain psc link", msg, APPLICATION_PATH);
    }

    public void testMessageForMandatoryAmendment() {
        String actualMsg = "One or more subject schedules on testStudy have been amended according to testAmendment as of Sep 22, 2010. For more information, please login to Patient Study Calendar.\n" +
                "http://psc.deployed.path";
        String msg = getMessageText();
        assertEquals("Message is not same", msg, actualMsg);
    }

    public void testMessageForNonMandatoryAmendment() throws Exception {
        amendment.setMandatory(false);
        String actualMsg = "One or more subject schedules on testStudy may have been amended according to testAmendment as of Sep 22, 2010. For more information or to apply this amendment, please login to Patient Study Calendar.\n" +
                "http://psc.deployed.path";
        String msg = getMessageText();
        assertEquals("Message is not same", msg, actualMsg);
    }

    @Override
    protected AmendmentMailMessage createMessage() {
        getMailMessageFactory().setApplicationPath(APPLICATION_PATH);
        return getMailMessageFactory().createAmendmentMailMessage(study, amendment);
    }
}
