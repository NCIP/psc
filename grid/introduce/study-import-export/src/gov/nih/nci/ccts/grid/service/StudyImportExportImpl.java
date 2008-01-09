package gov.nih.nci.ccts.grid.service;

import gov.nih.nci.ccts.grid.common.StudyImportExport;

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
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CLASSPATH_EXPRESSION);
//        this.studyImportExport = (StudyImportExport) applicationContext.getBean(gridServiceBeanName);

    }


    public java.lang.String exportStudyByCoordinatingCenterIdentifier(java.lang.String coordinatingCenterIdentifier) throws RemoteException {
        String studyXmlString = this.studyImportExport.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        return studyXmlString;
    }

}

