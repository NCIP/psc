package edu.northwestern.bioinformatics.studycalendar.grid.common;

import edu.northwestern.bioinformatics.studycalendar.grid.Study;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException;

import java.rmi.RemoteException;

/**
 * @author Saurah Agrawal
 */
public interface StudyService {

    Study retrieveStudyByAssignedIdentifier(String assignedIdentifier) throws RemoteException, StudyDoesNotExistsException;

    Study createStudy(Study study) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyCreationException;

}
