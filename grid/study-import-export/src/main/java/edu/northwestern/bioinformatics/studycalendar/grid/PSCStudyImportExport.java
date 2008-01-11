package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import gov.nih.nci.ccts.grid.common.StudyImportExport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

/**
 * @author Saurabh Agrawal
 */
public class PSCStudyImportExport implements StudyImportExport {

    private static final Log logger = LogFactory.getLog(PSCStudyImportExport.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";


    private StudyXMLReader studyXMLReader;

    private StudyService studyService;

    private StudyXMLWriter studyXMLWriter;


    public String exportStudyByCoordinatingCenterIdentifier(String coordinatingCenterIdentifier) throws RemoteException {
        //first fetch the study for the coordinating center identifier
        Study study = studyService.getStudyByAssignedIdentifier(coordinatingCenterIdentifier);

        if (study == null) {
            String message = "Exception while exporting study..no study found with given identifier:" + coordinatingCenterIdentifier;
            logger.debug(message);
            throw new RemoteException(message);
        }

        try {
            String studyXml = studyXMLWriter.createStudyXML(study);
            logger.info("exporitng study:" + studyXml);
            return studyXml;
        } catch (Exception exception) {
            logger.error("errror while exporting study.grid id:" + study.getGridId() + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }


    }

    public void importStudy(String studyXml) throws RemoteException {

        if (studyXml == null || StringUtils.isBlank(studyXml) || studyXml.getBytes() == null) {
            String message = "Exception while importing study.studyXml string is either empty or null" + studyXml;
            logger.debug(message);
            throw new RemoteException(message);
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(studyXml.getBytes());
        try {
            Study study = studyXMLReader.read(byteArrayInputStream);
            logger.info("imorted study:" + study.getGridId());

        } catch (Exception exception) {
            logger.error("errror while imprting study. message:" + exception.getMessage() + ". Expception:" + exception.getClass() + ". " +
                    "cause:" + exception.getCause());
            //exception.printStackTrace();
            throw new RemoteException(exception.getMessage());
        }

    }


    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setStudyXMLWriter(StudyXMLWriter studyXMLWriter) {
        this.studyXMLWriter = studyXMLWriter;
    }

    @Required
    public void setStudyXMLReader(StudyXMLReader studyXMLReader) {
        this.studyXMLReader = studyXMLReader;
    }
}
