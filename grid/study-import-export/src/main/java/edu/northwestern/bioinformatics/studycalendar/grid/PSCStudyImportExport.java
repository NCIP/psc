package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.ccts.grid.common.StudyImportExportI;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigInteger;
import java.rmi.RemoteException;

/**
 *@author Saurabh Agrawal
 */
public class PSCStudyImportExport
//        implements StudyImportExportI
{

    private static final Log logger = LogFactory.getLog(PSCStudyImportExport.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";


    private StudyDao studyDao;


    public Study exportStudyById(BigInteger integer) throws RemoteException {

        edu.northwestern.bioinformatics.studycalendar.domain.Study psc = studyDao.getByGridId("abc");

        StudyXMLWriter studyXMLWriter = new StudyXMLWriter();
        try {
            String studyXml = studyXMLWriter.createStudyXML(psc);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return null;


    }

//    public Study populateStudyDTO(edu.northwestern.bioinformatics.studycalendar.domain.Study pscStudy) throws Exception {
//        Study gridStudy = null;
//        try {
//            InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
//                    "gov/nih/nci/ccts/grid/client/client-config.wsdd");
//            Reader reader = new FileReader(regFile);
//            gridStudy = (Study) gov.nih.nci.cagrid.common.Utils.serializeObject(reader, Study.class, config);
//        }
//        catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            throw e;
//        }
//        return gridStudy;
//    }

    //
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


    public ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String exportStudyByCoordinatingCenterIdentifier(String string) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
