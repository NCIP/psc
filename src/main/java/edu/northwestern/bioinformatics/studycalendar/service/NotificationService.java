package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ScheduleNotificationMailMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

/**
 * This service will add Notification objects
 *
 * @author <a href="mailto:saurabh.agrawal@semanticbits.com">Saurabh Agrawal</a> Created-on : May 29, 2007
 * @version %I%, %G%
 * @since 1.0
 */
@Transactional(readOnly = true)

public class NotificationService {

    private static final Log logger = LogFactory.getLog(NotificationService.class);

    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private Integer numberOfDays;
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    private UserService userService;

    /**
     * This method will add notification when nothing is scheduled for a patient. This method is used by Cron
     * scheduling engine.
     */
    @Transactional(readOnly = false)
    public void addNotificationIfNothingIsScheduledForPatient() {

        Calendar calendar = Calendar.getInstance();

        logger.debug("executing scan on " + calendar.getTime());

        calendar.add(Calendar.DATE, numberOfDays);


        List<StudySubjectAssignment> studySubjectAssignments = studySubjectAssignmentDao.getAllAssignmenetsWhichHaveNoActivityBeyondADate(calendar.getTime());
        logger.debug("found  " + studySubjectAssignments.size() + " assignments");

        for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
            Notification notification = Notification.createNotificationForPatient(Calendar.getInstance().getTime(), numberOfDays);
            notification.setAssignment(studySubjectAssignment);
            //do not send the email
            studySubjectAssignment.getNotifications().add(notification);
            studySubjectAssignmentDao.save(studySubjectAssignment);

        }

    }

    public void notifyUsersForNewScheduleNotifications(final Notification notification) {
        //first find the email address of subject coordinators
        String userName = ApplicationSecurityManager.getUser();
        User user = userService.getUserByName(userName);

        String toAddress = userService.getEmailAddresssForUser(user);
        ScheduleNotificationMailMessage mailMessage = mailMessageFactory.createScheduleNotificationMailMessage(toAddress, notification);
        if (mailMessage != null) {
            try {
                mailSender.send(mailMessage);
                logger.debug("sending new schedule notification to:" + toAddress);
            } catch (MailException e) {
                logger.error("Can not send new schedule notification to:" + toAddress + " exception message:" + e.getMessage());
            } catch (Exception e) {
                logger.error("Can not send new schedule notification to:" + toAddress + "exception: " + e.toString() + " exception message:" + e.getMessage());
            }
        }

    }


    @Required
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }


    @Required
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setNumberOfDays(final Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    // @Required
    public void setMailSender(final MailSender mailSender) {
        this.mailSender = mailSender;
    }

    // @Required
    public void setMailMessageFactory(final MailMessageFactory mailMessageFactory) {
        this.mailMessageFactory = mailMessageFactory;
    }


}
