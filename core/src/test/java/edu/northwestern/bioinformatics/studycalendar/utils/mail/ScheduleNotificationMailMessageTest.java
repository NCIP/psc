package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures;
import org.springframework.mail.SimpleMailMessage;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ScheduleNotificationMailMessageTest extends MailMessageTestCase<ScheduleNotificationMailMessage> {
    private static final List<String> MAILTO = Arrays.asList("whogets@thebadnews.com");
    private StudySubjectAssignment studySubjectAssignment;
    private Notification notification;
    private AmendmentApproval amendmentApproval;

    private Amendment amendment;
    private StudySite studySite;
    private Subject subject;
    private ScheduledActivity scheduledActivity;


    protected void setUp() throws Exception {
        super.setUp();
        getConfiguration().set(Configuration.MAIL_EXCEPTIONS_TO, MAILTO);

        amendment = createAmendment("amendment", new Date(), false);
        amendment.setId(2);

        amendmentApproval = new AmendmentApproval();
        Study study = createNamedInstance("Glancing", Study.class);
        Site site = createNamedInstance("Lake", Site.class);
        studySite = createStudySite(study, site);
        subject = createSubject("Alice", "Childress");

        studySubjectAssignment = new StudySubjectAssignment();
        studySubjectAssignment.setSubject(subject);
        studySubjectAssignment.setStudySite(studySite);
        subject.addAssignment(studySubjectAssignment);
        scheduledActivity = ServicedFixtures.createScheduledActivity("sch activity", 2008, 2, 3);


    }

    public void testMessageForCreateAe() {

        AdverseEvent ae = new AdverseEvent();
        ae.setDescription("Grade 4 adverse event");
        ae.setDetectionDate(new Date());

        notification = new Notification(ae);
        String msg = getMessageText();
        validateMessage(msg);


    }

    public void testMessageWhenAmendmentIsApproved() {
        amendmentApproval.setAmendment(amendment);
        amendmentApproval.setStudySite(studySite);
        notification = new Notification(amendmentApproval);
        String msg = getMessageText();
        validateMessage(msg);


    }

    public void testMessageForNonMandatoryAmendment() {
        amendmentApproval.setAmendment(amendment);
        amendmentApproval.setStudySite(studySite);
        StudySubjectAssignment studySubjectAssignment = ServicedFixtures.createAssignment(studySite.getStudy(), studySite.getSite(), subject);
        notification = Notification.createNotificationForNonMandatoryAmendments(studySubjectAssignment, amendment);
        String msg = getMessageText();
        validateMessage(msg);


    }

    public void testCrateMessageForReconsents() {
        scheduledActivity.setId(2);
        notification = new Notification(scheduledActivity);


        String msg = getMessageText();
        validateMessage(msg);

    }

    private void validateMessage(final String msg) {
        assertContains(msg, notification.getTitle());
        assertContains(msg, notification.getMessage());
        String actionRequired = notification.isActionRequired() ? "Yes" : "No";
        assertContains(msg, "actionRequired:  " + actionRequired);
    }

    public void testSubject() {
        String messageSubject = createMessage().getSubject();
        assertContains(messageSubject, "New schedule notifications");
    }

    public void testTo() {
        SimpleMailMessage msg = createMessage();
        assertEqualArrays(MAILTO.toArray(new String[MAILTO.size()]), msg.getTo());
    }

    protected ScheduleNotificationMailMessage createMessage() {

        return getMailMessageFactory().createScheduleNotificationMailMessage(MAILTO.get(0), notification);
    }

}

