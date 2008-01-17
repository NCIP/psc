package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.grid.common.StudyService;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyAlreadyExistsException;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import gov.nih.nci.cagrid.common.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.utils.StringBufferReader;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.rmi.RemoteException;

/**
 * @author Saurabh Agrawal
 */
public class PSCStudyService implements StudyService {
    private static final Log logger = LogFactory.getLog(PSCStudyService.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";


    private StudyXMLReader studyXMLReader;

    private edu.northwestern.bioinformatics.studycalendar.service.StudyService studyService;

    private StudyXMLWriter studyXMLWriter;


    public void importStudy(String studyXml) throws RemoteException {

        if (studyXml == null || StringUtils.isBlank(studyXml) || studyXml.getBytes() == null) {
            String message = "Exception while importing study.studyXml string is either empty or null" + studyXml;
            logger.debug(message);
            throw new RemoteException(message);
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(studyXml.getBytes());
        try {
            Study study = studyXMLReader.readAndSave(byteArrayInputStream);
            logger.info("imorted study:" + study.getId());

        } catch (Exception exception) {
            logger.error("errror while imprting study. message:" + exception.getMessage() + ". Expception:" + exception.getClass() + ". " +
                    "cause:" + exception.getCause());
            //exception.printStackTrace();
            throw new RemoteException(exception.getMessage());
        }

    }


    public edu.northwestern.bioinformatics.studycalendar.grid.Study retrieveStudyByAssignedIdentifier(String coordinatingCenterIdentifier)
            throws RemoteException, StudyDoesNotExistsException {

        //first fetch the study for the coordinating center identifier
        Study study = studyService.getStudyByAssignedIdentifier(coordinatingCenterIdentifier);

        if (study == null) {
            String message = "Exception while exporting study..no study found with given identifier:" + coordinatingCenterIdentifier;
            logger.debug(message);
            StudyAlreadyExistsException studyAlreadyExistsException = new StudyAlreadyExistsException();
            studyAlreadyExistsException.setFaultReason(message);
            studyAlreadyExistsException.setFaultString(message);

            throw studyAlreadyExistsException;
        }

        try {
            //covertStudyToGridStudy(study);
            String studyXml = studyXMLWriter.createStudyXML(study);
            edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = populateGridStudy(studyXml);
            //example of this study xml
//            <?xml version="1.0" encoding="UTF-8"?>
//            <study xmlns="http://bioinformatics.northwestern.edu/ns/psc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//          assigned-identifier="cc" id="96507ff5-06e7-4f45-bfc5-179b888e69f6"
//              xsi:schemaLocation="http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd">
//            <planned-calendar id="9a2ec400-ef49-4d7d-9917-5766193c8181"/>
//            </study>

            logger.info("exporting study:" + gridStudy.getId());
            return gridStudy;

        } catch (Exception exception) {
            logger.error("errror while exporting study.grid id:" + study.getId() + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }


    }

//    private void covertStudyToGridStudy(Study study) {
//        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = null;
//
//        gridStudy.setId(study.getGridId());
//
//        gridStudy.setAssignedIdentifier(study.getAssignedIdentifier());
//
//
//        List<Amendment> amendments = study.getAmendmentsList();
//        if (amendments != null && !amendments.isEmpty()) {
//            List<edu.northwestern.bioinformatics.studycalendar.grid.Amendment> gridAmendments = new ArrayList<edu.northwestern.bioinformatics.studycalendar.grid.Amendment>();
//
//            for (Amendment amendment : amendments) {
//                edu.northwestern.bioinformatics.studycalendar.grid.Amendment gridAmendment = new edu.northwestern.bioinformatics.studycalendar.grid.Amendment();
//                gridAmendment.setId(amendment.getGridId());
//                gridAmendment.setName(amendment.getName());
//                gridAmendment.set
//            }
//
//        }
//
//        edu.northwestern.bioinformatics.studycalendar.grid.PlannedCalendar gridPlannedCalendar = gridStudy.getPlannedCalendar();
//
//        edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar plannedCalendar = study.getPlannedCalendar();
//
//        if (plannedCalendar != null) {
//            assertNotNull(gridStudy.getPlannedCalendar());
//            assertEquals(plannedCalendar.getGridId(), gridPlannedCalendar.getId());
//        }
//
//
//    }

    private edu.northwestern.bioinformatics.studycalendar.grid.Study populateGridStudy(String studyXML) throws Exception {
        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = null;
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().
                    getResourceAsStream("edu/northwestern/bioinformatics/studycalendar/grid/client/client-config.wsdd");
            Reader reader = new StringBufferReader(new StringBuffer(studyXML));
            gridStudy = (edu.northwestern.bioinformatics.studycalendar.grid.Study) Utils.deserializeObject(reader, edu.northwestern.bioinformatics.studycalendar.grid.Study.class, config);
        }
        catch (Exception exception) {
            logger.error("errror while exporting study. genreated study xml:" + studyXML + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }
        return gridStudy;
    }


    public edu.northwestern.bioinformatics.studycalendar.grid.Study createStudy(edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy)
            throws RemoteException, StudyAlreadyExistsException {
        InputStream config = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("edu/northwestern/bioinformatics/studycalendar/grid/client/client-config.wsdd");
        // File file = new File("abc.xml");

        StringWriter studyXml = new StringWriter();
        try {
            Utils.serializeObject(gridStudy, new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "study")
                    , studyXml);

            logger.info("study xml:" + studyXml.toString());

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(studyXml.getBuffer().toString().getBytes());

            studyXMLReader.readAndSave(byteArrayInputStream);

        } catch (StudyCalendarValidationException exception) {
            String message = "error while importing the study:grid_id=" + gridStudy.getId() + " exception message:" + exception.getMessage();
            logger.error(message);
            //FIXME: Surabh: remove this exception..
            throw new StudyAlreadyExistsException();

        } catch (Exception e) {
            String message = "error while importing the study:grid_id=" + gridStudy.getId() + " exception message:" + e.getMessage();
            logger.error(message);
            e.printStackTrace();
            throw new RemoteException(message);

        }


        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Required
    public void setStudyXMLWriter(StudyXMLWriter studyXMLWriter) {
        this.studyXMLWriter = studyXMLWriter;
    }

    @Required
    public void setStudyXMLReader(StudyXMLReader studyXMLReader) {
        this.studyXMLReader = studyXMLReader;
    }


    @Required
    public void setStudyService(edu.northwestern.bioinformatics.studycalendar.service.StudyService studyService) {
        this.studyService = studyService;

    }

}
