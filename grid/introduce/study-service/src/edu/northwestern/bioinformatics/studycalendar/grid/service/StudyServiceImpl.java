package edu.northwestern.bioinformatics.studycalendar.grid.service;

import edu.northwestern.bioinformatics.studycalendar.grid.common.StudyService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RemoteException;

/**
 * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME
 *
 * @created by Introduce Toolkit version 1.0
 */
public class StudyServiceImpl extends StudyServiceImplBase {

    private static final String DEFAULT_SPRING_CLASSPATH_EXPRESSION = "classpath:applicationContext-study-import-export.xml";

    private StudyService studyService;
    private String gridServiceBeanName = "studyService";

    public StudyServiceImpl() throws RemoteException {
        super();
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CLASSPATH_EXPRESSION);
        this.studyService = (StudyService) applicationContext.getBean(gridServiceBeanName);

    }

	public edu.northwestern.bioinformatics.studycalendar.grid.Study retrieveStudyByAssignedIdentifier(java.lang.String assignedIdentifier) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException {
        return studyService.retrieveStudyByAssignedIdentifier(assignedIdentifier);
    }

	public edu.northwestern.bioinformatics.studycalendar.grid.Study createStudy(edu.northwestern.bioinformatics.studycalendar.grid.Study study) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyAlreadyExistsException {
        return studyService.createStudy(study);
    }

}

