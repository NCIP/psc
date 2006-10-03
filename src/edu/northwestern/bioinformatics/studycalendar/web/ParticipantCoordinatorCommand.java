package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;


/**
 * @author Padmaja Vedula
 */

public class ParticipantCoordinatorCommand {
    private Integer studyId;
    private List assignedUsers;
    private List availableUsers;
   
  
    ////// BOUND PROPERTIES

    public List getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(List assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public List getAvailableUsers() {
        return availableUsers;
    }

    public void setAvailableUsers(List availableUsers) {
        this.availableUsers = availableUsers;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }
  
}
