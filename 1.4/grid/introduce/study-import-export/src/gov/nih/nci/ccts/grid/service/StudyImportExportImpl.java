package gov.nih.nci.ccts.grid.service;

import gov.nih.nci.ccts.grid.common.StudyImportExport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RemoteException;

/**
 * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME
 *
 * @created by Introduce Toolkit version 1.0
 */
public class StudyImportExportImpl extends StudyImportExportImplBase {

    private static final String DEFAULT_SPRING_CLASSPATH_EXPRESSION = "classpath:applicationContext-study-import-export.xml";

    private StudyImportExport studyImportExport;
    private String gridServiceBeanName = "studyImportExport";

    public StudyImportExportImpl() throws RemoteException {
        super();
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CLASSPATH_EXPRESSION);
        this.studyImportExport = (StudyImportExport) applicationContext.getBean(gridServiceBeanName);

    }

    public java.lang.String exportStudyByCoordinatingCenterIdentifier(java.lang.String string) throws RemoteException {
        String studyXmlString = this.studyImportExport.exportStudyByCoordinatingCenterIdentifier(string);
        return studyXmlString;
    }

    public void importStudy(java.lang.String string) throws RemoteException {
        this.studyImportExport.importStudy(string);
    }

}

