/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.NotificationMailMessage;
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
}
