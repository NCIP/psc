package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;
import java.util.ArrayList;


/**
 * @author Jaron Sampson
 * @author Yufang Wang
 */

public class AssignSiteCoordinatorCommand {
    private Integer siteId;
    private String assign;
    private List<String> assignedCoordinators;
    private List<String> availableCoordinators;
    /*
    public AssignSiteCoordinatorCommand() {
    	assignedCoordinators = new ArrayList<String>();
    	availableCoordinators = new ArrayList<String>();    	
    }
   */
  
    ////// BOUND PROPERTIES

    public List<String> getAssignedCoordinators() {
    	return assignedCoordinators;
    }

    public void setAssignedCoordinators(List<String> assignedCoordinators) {
        this.assignedCoordinators = assignedCoordinators;
    }

    public List<String> getAvailableCoordinators() {
    	return availableCoordinators;
    }

    public void setAvailableCoordinators(List<String> availableCoordinators) {
        this.availableCoordinators = availableCoordinators;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getAssign() {
        return assign;
    }

    public void setAssign(String assign) {
        this.assign = assign;
    }
}
