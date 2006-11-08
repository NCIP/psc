package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.List;
import java.util.ArrayList;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;


/**
 * @author Yufang Wang
 */

public class AssignTemplatesToOneParticipantCoordinatorCommand {
    private String assign;
    private List<String> participantcoordinators;
    private List<Study> assignedTemplates;
    private List<Study> availableTemplates;
    
  
    ////// BOUND PROPERTIES

    public List<Study> getAssignedTemplates() {
    	return assignedTemplates;
    }

    public void setAssignedTemplates(List<Study> assignedTemplates) {
        this.assignedTemplates = assignedTemplates;
    }

    public List<Study> getAvailableTemplates() {
    	return availableTemplates;
    }

    public void setAvailableTemplates(List<Study> availableTemplates) {
        this.availableTemplates = availableTemplates;
    }

    public String getAssign() {
        return assign;
    }

    public void setAssign(String assign) {
        this.assign = assign;
    }
}
