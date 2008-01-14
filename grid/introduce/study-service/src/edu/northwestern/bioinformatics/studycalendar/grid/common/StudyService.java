package edu.northwestern.bioinformatics.studycalendar.grid.common;

import edu.northwestern.bioinformatics.studycalendar.grid.Study;

import java.rmi.RemoteException;

/**
 * @author Saurah Agrawal
 */
public interface StudyService {

    Study retrieveStudyByAssignedIdentifier(String assignedIdentifier) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExists;

    Study createStudy(Study study) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyAlreadyException;

}
