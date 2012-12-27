/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.grid.common.StudyService;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyCreationException;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import gov.nih.nci.cagrid.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.utils.StringBufferReader;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.XMLConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.rmi.RemoteException;

/**
 * @author Saurabh Agrawal
 */

@Transactional(readOnly = true)
public class PSCStudyService implements StudyService {

    private static final Log logger = LogFactory.getLog(PSCStudyService.class);


    private StudyXMLReader studyXMLReader;

    private edu.northwestern.bioinformatics.studycalendar.service.StudyService studyService;

    private StudyXMLWriter studyXMLWriter;

//    public void importStudy(String studyXml) throws RemoteException {
//
//        if (studyXml == null || StringUtils.isBlank(studyXml) || studyXml.getBytes() == null) {
//            String message = "Exception while importing study.studyXml string is either empty or null" + studyXml;
//            logger.debug(message);
//            throw new RemoteException(message);
//        }
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(studyXml.getBytes());
//        try {
//            Study study = studyXMLReader.readAndSave(byteArrayInputStream);
//            logger.info("imorted study:" + study.getId());
//
//        } catch (Exception exception) {
//            logger.error("errror while imprting study. message:" + exception.getMessage() + ". Expception:" + exception.getClass() + ". " +
//                    "cause:" + exception.getCause());
//            //exception.printStackTrace();
//            throw new RemoteException(exception.getMessage());
//        }
//
//    }


    public edu.northwestern.bioinformatics.studycalendar.grid.Study retrieveStudyByAssignedIdentifier(String coordinatingCenterIdentifier)
            throws RemoteException, StudyDoesNotExistsException {

        //first fetch the study for the coordinating center identifier
        Study study = studyService.getStudyByAssignedIdentifier(coordinatingCenterIdentifier);

        if (study == null) {
            String message = "Exception while exporting study..no study found with given identifier:" + coordinatingCenterIdentifier;
            logger.debug(message);
            StudyDoesNotExistsException studyDoesNotExistsException = new StudyDoesNotExistsException();
            studyDoesNotExistsException.setFaultReason(message);
            studyDoesNotExistsException.setFaultString(message);

            throw studyDoesNotExistsException;
        }

        try {
            String studyXml = studyXMLWriter.createStudyXML(study);
            edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = populateGridStudy(studyXml);


            logger.info("exporting study:assigned_identifier" + gridStudy.getAssignedIdentifier());
            return gridStudy;

        } catch (Exception exception) {
            logger.error("errror while exporting study.grid id:" + study.getId() + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }


    }


    private edu.northwestern.bioinformatics.studycalendar.grid.Study populateGridStudy(String studyXML) throws Exception {
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().
                    getResourceAsStream("edu/northwestern/bioinformatics/studycalendar/grid/client/client-config.wsdd");
            Reader reader = new StringBufferReader(new StringBuffer(studyXML));
            edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = (edu.northwestern.bioinformatics.studycalendar.grid.Study) Utils.deserializeObject(reader,
                    edu.northwestern.bioinformatics.studycalendar.grid.Study.class,
                    config);
            return gridStudy;

        }
        catch (Exception exception) {
            logger.error("errror while exporting study. genreated study xml:" + studyXML + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }
    }


@Transactional(readOnly = false)
    public edu.northwestern.bioinformatics.studycalendar.grid.Study createStudy(edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy)
            throws RemoteException, StudyCreationException {

        if (gridStudy == null) {
            String message = "method parameter  is null";
            logger.error(message);
            StudyCreationException studyCreationException = new StudyCreationException();
            studyCreationException.setFaultString(message);
            studyCreationException.setFaultReason(message);
            throw studyCreationException;
        }
        StringWriter studyXml = new StringWriter();

        String studyDocumentXml = "";
        try {

            Utils.serializeObject(gridStudy, new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "study", XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    , studyXml);

            logger.info("study xml:" + studyXml.toString());

            studyDocumentXml = studyXml.getBuffer().toString();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(studyDocumentXml.getBytes());

            studyXMLReader.readAndSave(byteArrayInputStream);

        } catch (StudyCalendarValidationException exception) {
            String message = "error while importing the study:studyXml-" + studyDocumentXml + " exception message:" + exception.getMessage();
            logger.error(message);
            StudyCreationException studyCreationException = new StudyCreationException();
            studyCreationException.setFaultString(message);
            studyCreationException.setFaultReason(message);
            throw studyCreationException;

        } catch (Exception e) {
            String message = "error while importing the study:assigned_identifier=" + gridStudy.getAssignedIdentifier() + " exception message:" + e.getMessage();
            logger.error(message);
            throw new RemoteException(message);

        }


        return null;
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
