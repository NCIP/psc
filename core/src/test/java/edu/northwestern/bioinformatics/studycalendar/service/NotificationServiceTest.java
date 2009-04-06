package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ScheduleNotificationMailMessage;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import org.easymock.classextension.EasyMock;
import org.springframework.mail.MailSender;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class NotificationServiceTest extends StudyCalendarTestCase {
    private NotificationService notificationService;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private Integer numberOfDays;
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    private UserService userService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        numberOfDays = 14;

        userService = registerMockFor(UserService.class);
        mailMessageFactory = registerMockFor(MailMessageFactory.class);
        mailSender = registerMockFor(MailSender.class);
        notificationService = new NotificationService();
        notificationService.setNumberOfDays(numberOfDays);
        notificationService.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        notificationService.setMailMessageFactory(mailMessageFactory);
        notificationService.setMailSender(mailSender);
        notificationService.setUserService(userService);
        notificationService.setApplicationSecurityManager(applicationSecurityManager);

        SecurityContextHolderTestHelper.setSecurityContext("user", "password");
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

    public void testNotifyUsersForNewScheduleNotifications() {
        User user = Fixtures.createUser("first name", Role.SUBJECT_COORDINATOR);
        String emailAddress = "user@email.com";
        EasyMock.expect(userService.getEmailAddresssForUser(user)).andReturn(emailAddress);
        ScheduleNotificationMailMessage notificationMailMessage = new ScheduleNotificationMailMessage();
        AdverseEvent ae = new AdverseEvent();
        Notification notification = new Notification(ae);
        EasyMock.expect(mailMessageFactory.createScheduleNotificationMailMessage(emailAddress, notification)).andReturn(notificationMailMessage);
        mailSender.send(notificationMailMessage);
        EasyMock.expect(userService.getUserByName("user")).andReturn(user);

        replayMocks();
        notificationService.notifyUsersForNewScheduleNotifications(notification);

        verifyMocks();
    }
}
