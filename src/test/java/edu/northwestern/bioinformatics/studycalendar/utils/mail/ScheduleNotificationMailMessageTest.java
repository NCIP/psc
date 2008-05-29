package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import org.springframework.mail.SimpleMailMessage;

import java.util.Arrays;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ScheduleNotificationMailMessageTest extends MailMessageTestCase<ScheduleNotificationMailMessage> {
    private static final List<String> MAILTO = Arrays.asList("whogets@thebadnews.com");
    private StudySubjectAssignment studySubjectAssignment;

    protected void setUp() throws Exception {
        super.setUp();
        getConfiguration().set(Configuration.MAIL_EXCEPTIONS_TO, MAILTO);
    }

    public void testMessage() {
        String msg = getMessageText();

        String studyName = studySubjectAssignment.getStudySite().getStudy().getName();
        String siteName = studySubjectAssignment.getStudySite().getSite().getName();
        assertContains(msg, "A new schedule has been created on study: " + studyName + " at site: " + siteName + ".");
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

        Study study = createNamedInstance("Glancing", Study.class);
        Site site = createNamedInstance("Lake", Site.class);
        StudySite studySite = createStudySite(study, site);
        Subject subject = createSubject("Alice", "Childress");

        studySubjectAssignment = new StudySubjectAssignment();
        studySubjectAssignment.setSubject(subject);
        studySubjectAssignment.setStudySite(studySite);
        subject.addAssignment(studySubjectAssignment);


        return getMailMessageFactory().createScheduleNotificationMailMessage(MAILTO.get(0), studySubjectAssignment);
    }

}

