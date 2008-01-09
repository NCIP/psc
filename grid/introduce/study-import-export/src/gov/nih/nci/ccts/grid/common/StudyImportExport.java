package gov.nih.nci.ccts.grid.common;

import java.rmi.RemoteException;

/**
 * @author Saurah Agrawal
 */
public interface StudyImportExport {
    /**
     * Search study by coordinating center identifier and returns xml string of corresponding study
     * @param coordinatingCenterIdentifier
     * @return  study xml string.
     * @throws RemoteException if no study found or for any other exception occured
     */
    public String exportStudyByCoordinatingCenterIdentifier(String coordinatingCenterIdentifier) throws RemoteException;

}
