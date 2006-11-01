package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;


/**
 * @author Padmaja Vedula
 */

public class AssignSiteCommand {
    private Integer studyId;
    private String assign;
    private List<String> assignedSites;
    private List<String> availableSites;
   
  
    ////// BOUND PROPERTIES

    public List getAssignedSites() {
        return assignedSites;
    }

    public void setAssignedSites(List assignedSites) {
        this.assignedSites = assignedSites;
    }

    public List getAvailableSites() {
        return availableSites;
    }

    public void setAvailableSites(List availableSites) {
        this.availableSites = availableSites;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }
    
    public String getAssign() {
        return assign;
    }

    public void setAssign(String assign) {
        this.assign = assign;
    }
  
}
