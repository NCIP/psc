package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;


/**
 * @author Padmaja Vedula
 */

public class AssignSiteCommand {
    private Integer studyId;
    private String assign;
    private List<Site> assignedSites;
    private List<Site> availableSites;
   
  
    ////// BOUND PROPERTIES

    public List<Site> getAssignedSites() {
        return assignedSites;
    }

    public void setAssignedSites(List<Site> assignedSites) {
        this.assignedSites = assignedSites;
    }

    public List getAvailableSites() {
        return availableSites;
    }

    public void setAvailableSites(List<Site> availableSites) {
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
