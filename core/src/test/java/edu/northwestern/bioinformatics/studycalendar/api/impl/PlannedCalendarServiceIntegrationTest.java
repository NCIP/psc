package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.PlannedCalendarService;
import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.PlannedCalendarXmlSerializer;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Saurabh Agrawal
 */
public class PlannedCalendarServiceIntegrationTest extends DaoTestCase {
    private PlannedCalendarService plannedCalendarService = (DefaultPlannedCalendarService) getApplicationContext().getBean("plannedCalendarService");

    private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Study parameterStudy;
    private Study loadedStudy;
    private Site site;
    private String fileLocation;


    private PlannedCalendarXmlSerializer plannedCalendarXmlSerializer = (PlannedCalendarXmlSerializer) getApplicationContext().getBean("plannedCalendarXmlSerializer");
    private FileOutputStream fileOutputStream;
    private Study study;


    protected void setUp() throws Exception {
        super.setUp();


        parameterStudy = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance("S1", Study.class);
        parameterStudy.setGridId("UNIQUE!");
        // addSite(parameterStudy, 1);

        loadedStudy = Fixtures.createNamedInstance("S1", Study.class);
        loadedStudy.setGridId("UNIQUE!");
        loadedStudy.setPlannedCalendar(new PlannedCalendar());
        //addSite(loadedStudy, 1);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("joe", "pass");
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        fileLocation = "api-report/PlannedCalendarServiceTest.txt";
        File f = new File(fileLocation);
        if (!f.exists()) {
            f.getParentFile().mkdir();
            f.createNewFile();
            logger.debug("Creating file - " + fileLocation);
        }

        fileOutputStream = new FileOutputStream(fileLocation, true);

        study = studyDao.getById(-150);
        assertNotNull(study);
        assertNotNull(study.getGridId());
        plannedCalendarXmlSerializer.setSerializeEpoch(true);
    }


    public void testRegister() throws Exception {
        PlannedCalendar plannedCalendar = plannedCalendarService.registerStudy(study);
        assertNotNull(plannedCalendar);
        assertNotNull(plannedCalendar.getId());

        fileOutputStream.write("\n--------------------------------\n".getBytes());

        String message = "Executing plannedCalendarService.registerStudy method.\n Following is the output :\n";
        fileOutputStream.write(message.getBytes());


        fileOutputStream.write(plannedCalendarXmlSerializer.createDocumentString(plannedCalendar).getBytes());
        message = "\nSucessfully executed plannedCalendarService.registerStudy method.\n";
        fileOutputStream.write(message.getBytes());
        fileOutputStream.write("\n--------------------------------\n".getBytes());

    }

    public void testGetPlannedCalendar() throws Exception {

        PlannedCalendar plannedCalendar = plannedCalendarService.getPlannedCalendar(study);
        assertNotNull(plannedCalendar);
        assertNotNull(plannedCalendar.getId());

        fileOutputStream.write("\n--------------------------------\n".getBytes());

        String message = "Executing plannedCalendarService.getPlannedCalendar method.\n Following is the output :\n";
        fileOutputStream.write(message.getBytes());


        fileOutputStream.write(plannedCalendarXmlSerializer.createDocumentString(plannedCalendar).getBytes());
        message = "\nSucessfully executed plannedCalendarService.getPlannedCalendar method.\n";
        fileOutputStream.write(message.getBytes());
        fileOutputStream.write("\n--------------------------------\n".getBytes());

    }


}