package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;


/**
 * @author Padmaja Vedula
 */

public class ParticipantCoordinatorCommand {
    private Integer studyId;
    private List<String> assignedCoordinators;
    private List<String> availableCoordinators;
   
  
    ////// BOUND PROPERTIES

    public List getAssignedCoordinators() {
        return assignedCoordinators;
    }

    public void setAssignedCoordinators(List assignedCoordinators) {
        this.assignedCoordinators = assignedCoordinators;
    }

    public List getAvailableCoordinators() {
        return availableCoordinators;
    }

    public void setAvailableCoordinators(List availableCoordinators) {
        this.availableCoordinators = availableCoordinators;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }
  
}
