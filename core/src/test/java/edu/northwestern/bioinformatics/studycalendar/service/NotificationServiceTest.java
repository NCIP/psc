package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.NotificationMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ScheduleNotificationMailMessage;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.easymock.classextension.EasyMock;
import org.springframework.mail.MailSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class NotificationServiceTest extends StudyCalendarTestCase {
    private NotificationService notificationService;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        mailMessageFactory = registerMockFor(MailMessageFactory.class);
        mailSender = registerMockFor(MailSender.class);
        notificationService = new NotificationService();
        notificationService.setNumberOfDays(14);
        notificationService.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        notificationService.setMailMessageFactory(mailMessageFactory);
        notificationService.setMailSender(mailSender);
    }

    public void testAddNotificationIfNothingIsScheduledForPatient() {
        List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();

        StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();
        studySubjectAssignments.add(studySubjectAssignment);
        EasyMock.expect(studySubjectAssignmentDao.getAllAssignmenetsWhichHaveNoActivityBeyondADate(EasyMock.isA(Date.class))).andReturn(studySubjectAssignments);
        studySubjectAssignmentDao.save(studySubjectAssignment);
        
        replayMocks();
        notificationService.addNotificationIfNothingIsScheduledForPatient();
        verifyMocks();
        assertEquals("assignment must have one notification", 1, studySubjectAssignment.getNotifications().size());
    }

    public void testSendNotificationMailToUsers() {
        String subjectHeader = "testSubject";
        String message = "This is the test message for email notification";
        String address = "test@email.com";
        NotificationMailMessage mailMessage =  new NotificationMailMessage();
        EasyMock.expect(mailMessageFactory.createNotificationMailMessage(subjectHeader, message)).andReturn(mailMessage);
        mailMessage.setTo(address);
        mailSender.send(mailMessage);

        replayMocks();
        notificationService.sendNotificationMailToUsers(subjectHeader, message, Arrays.asList(address));
        verifyMocks();
    }

    public void testNotifyUsersForNewScheduleNotifications() {
        String emailAddress = "user@email.com";
        User manager = AuthorizationObjectFactory.createCsmUser(63L, "newguy");
        manager.setEmailId(emailAddress);
        ScheduleNotificationMailMessage notificationMailMessage = new ScheduleNotificationMailMessage();
        AdverseEvent ae = new AdverseEvent();
        Notification notification = createNotification(manager, ae);
        EasyMock.expect(mailMessageFactory.createScheduleNotificationMailMessage(emailAddress, notification)).andReturn(notificationMailMessage);
        mailSender.send(notificationMailMessage);

        replayMocks();
        notificationService.notifyUsersForNewScheduleNotifications(notification);

        verifyMocks();
    }

    private Notification createNotification(User manager, AdverseEvent ae) {
        StudySubjectAssignment a = new StudySubjectAssignment();
        a.setStudySubjectCalendarManager(manager);
        Notification notification = new Notification(ae);
        notification.setAssignment(a);
        return notification;
    }
}
