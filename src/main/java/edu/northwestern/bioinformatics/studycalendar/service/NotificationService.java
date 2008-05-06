package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

/**
 * This service will add Notification objects , using Cron
 * scheduling engine.
 *
 * @author <a href="mailto:saurabh.agrawal@semanticbits.com">Saurabh Agrawal</a> Created-on : May 29, 2007
 * @version %I%, %G%
 * @since 1.0
 */
public class NotificationService {

    private static final Log logger = LogFactory.getLog(NotificationService.class);

    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private Integer numberOfDays;

    /**
     * This method will add notification when nothing is scheduled for a patient
     */
    @Transactional(readOnly = false)
    public void addNotificationIfNothingIsScheduledForPatient() {

        Calendar calendar = Calendar.getInstance();

        logger.debug("executing scan on " + calendar.getTime());

        calendar.add(Calendar.DATE, numberOfDays);


        List<StudySubjectAssignment> studySubjectAssignments = studySubjectAssignmentDao.getAllAssignmenetsWhichHaveNoActivityBeyondADate(calendar.getTime());
        logger.debug("found  " + studySubjectAssignments.size() + " assignments");

        for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
            Notification notification = new Notification();
            //Notification.createNotificationForPatient();
            studySubjectAssignment.addNotification(notification);
            studySubjectAssignmentDao.save(studySubjectAssignment);

        }

    }

    @Required
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setNumberOfDays(final Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
}
