package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import gov.nih.nci.ccts.grid.common.StudyImportExportI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import java.rmi.RemoteException;

/**
 * @author Saurabh Agrawal
 */
public class PSCStudyImportExport implements StudyImportExportI {

    private static final Log logger = LogFactory.getLog(PSCStudyImportExport.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";


    private StudyService studyService;
    private DaoFinder daoFinder;


    public ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException {
        return null;
    }

    public String exportStudyByCoordinatingCenterIdentifier(String coordinatingCenterIdentifier) throws RemoteException {
        //first fetch the study for the coordinating center identifier
        Study study = studyService.getStudyByAssignedIdentifier(coordinatingCenterIdentifier);

        if (study == null) {
            String message = "Exception while exporting study..no study found with given identifier:" + coordinatingCenterIdentifier;
            throw new RemoteException(message);
        }

        StudyXMLWriter studyXMLWriter = new StudyXMLWriter(daoFinder);
        try {
            String studyXml = studyXMLWriter.createStudyXML(study);
            logger.info("exporitng study:"+studyXml);
            return studyXml;
        } catch (Exception exception) {
            logger.error("errror while exporting study.grid id:" + study.getGridId() + " message:" + exception.getMessage());
            throw new RemoteException(exception.getMessage());
        }


    }


    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
