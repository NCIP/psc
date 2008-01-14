package edu.northwestern.bioinformatics.studycalendar.grid.service.globus;

import edu.northwestern.bioinformatics.studycalendar.grid.service.StudyServiceImpl;

import java.rmi.RemoteException;

/** 
 * DO NOT EDIT:  This class is autogenerated!
 *
 * This class implements each method in the portType of the service.  Each method call represented
 * in the port type will be then mapped into the unwrapped implementation which the user provides
 * in the StudyServiceImpl class.  This class handles the boxing and unboxing of each method call
 * so that it can be correclty mapped in the unboxed interface that the developer has designed and 
 * has implemented.  Authorization callbacks are automatically made for each method based
 * on each methods authorization requirements.
 * 
 * @created by Introduce Toolkit version 1.0
 * 
 */
public class StudyServiceProviderImpl{
	
	StudyServiceImpl impl;
	
	public StudyServiceProviderImpl() throws RemoteException {
		impl = new StudyServiceImpl();
	}
	

	public edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierResponse retrieveStudyByAssignedIdentifier(edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierRequest params) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException {
		StudyServiceAuthorization.authorizeRetrieveStudyByAssignedIdentifier();
		edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierResponse boxedResult = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.RetrieveStudyByAssignedIdentifierResponse();
		boxedResult.setStudy(impl.retrieveStudyByAssignedIdentifier(params.getAssignedIdentifier()));
		return boxedResult;
	}

	public edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyResponse createStudy(edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyRequest params) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyAlreadyExistsException {
		StudyServiceAuthorization.authorizeCreateStudy();
		edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyResponse boxedResult = new edu.northwestern.bioinformatics.studycalendar.grid.stubs.CreateStudyResponse();
		boxedResult.setStudy(impl.createStudy(params.getStudy().getStudy()));
		return boxedResult;
	}

}
