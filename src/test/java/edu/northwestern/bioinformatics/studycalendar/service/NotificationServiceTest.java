package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ScheduleNotificationMailMessage;
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

        StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();

        User user = Fixtures.createUser("first name", Role.SUBJECT_COORDINATOR);
        String emailAddress = "user@email.com";
        EasyMock.expect(userService.getEmailAddresssForUser(user)).andReturn(emailAddress);
        ScheduleNotificationMailMessage notificationMailMessage = new ScheduleNotificationMailMessage();
        EasyMock.expect(mailMessageFactory.createScheduleNotificationMailMessage(emailAddress, studySubjectAssignment)).andReturn(notificationMailMessage);
        mailSender.send(notificationMailMessage);
        replayMocks();
        notificationService.notifyUsersForNewScheduleNotifications(user, studySubjectAssignment);

        verifyMocks();
    }
}
