package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledActivityXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.PlannedCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Saurabh Agrawal
 */
public class DefaultScheduledCalendarServiceIntegrationTest
        extends DaoTestCase {


    public final Log logger = LogFactory.getLog(getClass());

    private ScheduledCalendarService scheduledCalendarService = (ScheduledCalendarService) getApplicationContext().getBean("scheduledCalendarService");
    private StudyService studyService = (StudyService) getApplicationContext().getBean("studyService");


    private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    private AmendmentService amendmentService = (AmendmentService) getApplicationContext().getBean("amendmentService");

    private ScheduledCalendar scheduledCalendar;
    private SubjectDao subjectDao = (SubjectDao) getApplicationContext().getBean("subjectDao");
    private Study study;
    private String fileLocation;

    private FileOutputStream fileOutputStream;
    private ScheduledCalendarXmlSerializer scheduledCalendarXmlSerializer = (ScheduledCalendarXmlSerializer) getApplicationContext().getBean("scheduledCalendarXmlSerializer");

    private ScheduledActivityXmlSerializer scheduledActivityXmlSerializer = (ScheduledActivityXmlSerializer) getApplicationContext().getBean("scheduledActivityXmlSerializer");
    private Site site;
    private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
    private Activity expectedActivity;
    private ActivityDao activityDao = (ActivityDao) getApplicationContext().getBean("activityDao");
    private Subject subject;


    public DefaultScheduledCalendarServiceIntegrationTest() throws IOException {
        try {
            fileLocation = "api-report/ScheduledCalendarServiceTest.txt";

            File f = new File(fileLocation);
            if (!f.exists()) {
                f.getParentFile().mkdir();
                f.createNewFile();
                logger.debug("Creating file - "+fileLocation);
            }
            fileOutputStream = new FileOutputStream(fileLocation, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
    }

    public void testAssignSubject() throws Exception {


        // Using the informed consent date as the calendar start date
        Date startDate = new Date();

        StudySegment loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        fileOutputStream.write("\n--------------------------------\n".getBytes());

        String message = "Executing ScheduledCalendarService#assignSubject method.\n Following is the output :\n";
        fileOutputStream.write(message.getBytes());

        scheduledCalendar = scheduledCalendarService.assignSubject(study, subject, site, loadedStudySegment,
                startDate, subject.getGridId());
        assertNotNull(scheduledCalendar);
        fileOutputStream.write(scheduledCalendarXmlSerializer.createDocumentString(scheduledCalendar).getBytes());
        message = "\n Sucessfully executed ScheduledCalendarService#assignSubject method.\n";
        fileOutputStream.write(message.getBytes());
        fileOutputStream.write("\n--------------------------------\n".getBytes());


    }

    public void testGetScheduledCalendar() throws Exception {


        Date startDate = new Date();

        StudySegment loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);

        //first create the calendar
        scheduledCalendar = scheduledCalendarService.assignSubject(study, subject, site, loadedStudySegment,
                startDate, subject.getGridId());

        //now retrieve the calendar
        fileOutputStream.write("\n--------------------------------\n".getBytes());

        String message = "Executing ScheduledCalendarService#getScheduledCalendar method.\n Following is the output :\n";
        fileOutputStream.write(message.getBytes());

        scheduledCalendar = scheduledCalendarService.getScheduledCalendar(study, subject, site);

        assertNotNull(scheduledCalendar);
        fileOutputStream.write(scheduledCalendarXmlSerializer.createDocumentString(scheduledCalendar).getBytes());
        message = "\nSucessfully executed ScheduledCalendarService#getScheduledCalendar method.\n";
        fileOutputStream.write(message.getBytes());
        fileOutputStream.write("\n--------------------------------\n".getBytes());

    }

    public void testGetScheduledActivities() throws Exception {

        subject = subjectDao.getById(-23);
        assertNotNull(subject);
        assertNotNull(subject.getGridId());

        fileOutputStream.write("\n--------------------------------\n".getBytes());

        String message = "Executing ScheduledCalendarService#getScheduledActivities method.\n Following is the output :\n";
        fileOutputStream.write(message.getBytes());


        Collection<ScheduledActivity> scheduledActivities = scheduledCalendarService.getScheduledActivities(study, subject, site, null, null);

        assertNotNull(scheduledActivities);
        assertFalse(scheduledActivities.isEmpty());
        fileOutputStream.write(scheduledActivityXmlSerializer.createDocumentString(scheduledActivities).getBytes());
        message = "\nSucessfully executed ScheduledCalendarService#getScheduledActivities method.\n";
        fileOutputStream.write(message.getBytes());
        fileOutputStream.write("\n--------------------------------\n".getBytes());

    }

    public void testChangeState() throws Exception {

        subject = subjectDao.getById(-23);
        assertNotNull(subject);
        assertNotNull(subject.getGridId());

        fileOutputStream.write("\n--------------------------------\n".getBytes());

        String message = "Executing ScheduledCalendarService#changeEventState method.\n Scheduled Activity in older state :\n";
        fileOutputStream.write(message.getBytes());


        List<ScheduledActivity> scheduledActivities = (List<ScheduledActivity>) scheduledCalendarService.getScheduledActivities(study, subject, site, null, null);

        assertNotNull(scheduledActivities);
        assertFalse(scheduledActivities.isEmpty());

        ScheduledActivity scheduledActivity = scheduledActivities.get(0);
        fileOutputStream.write(scheduledActivityXmlSerializer.createDocumentString(scheduledActivity).getBytes());

        Conditional conditional = new Conditional();
        conditional.setReason("conditional change");

        scheduledActivity = scheduledCalendarService.changeEventState(scheduledActivity, conditional);

        message = "\n Scheduled Activity in new state :\n";
        fileOutputStream.write(message.getBytes());

        fileOutputStream.write(scheduledActivityXmlSerializer.createDocumentString(scheduledActivity).getBytes());
        message = "\nSucessfully executed ScheduledCalendarService#changeEventState method.\n";
        fileOutputStream.write(message.getBytes());
        fileOutputStream.write("\n--------------------------------\n".getBytes());

    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/RegistrationConsumer"));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("joe", "pass");
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);


        study = studyDao.getById(-150);
        assertNotNull(study);
        assertNotNull(study.getGridId());

        amendmentService.amend(study);

        AmendmentApproval approvals = new AmendmentApproval();
        approvals.setAmendment(study.getAmendment());
        approvals.setDate(new Date());

        amendmentService.approve(study.getStudySites().get(0), approvals);


        site = siteDao.getById(-6);
        assertNotNull(site);

        subject = subjectDao.getById(-22);
        assertNotNull(subject);
        assertNotNull(subject.getGridId());


    }


    public void setScheduledActivityXmlSerializer(ScheduledActivityXmlSerializer scheduledActivityXmlSerializer) {
        this.scheduledActivityXmlSerializer = scheduledActivityXmlSerializer;
    }

    protected void onTearDownAfterTransaction() throws Exception {

        DataAuditInfo.setLocal(null);

    }


    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setScheduledCalendarXmlSerializer(ScheduledCalendarXmlSerializer scheduledCalendarXmlSerializer) {
        this.scheduledCalendarXmlSerializer = scheduledCalendarXmlSerializer;
    }

    public void setScheduledCalendarService(ScheduledCalendarService scheduledCalendarService) {
        this.scheduledCalendarService = scheduledCalendarService;
    }

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }


    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }


    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


}