package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;


/**
 * @author Padmaja Vedula
 */

public class AssignSiteCommand implements Validatable {
    private Integer studyId;
    private Boolean assign;
    private List<Site> assignedSites;
    private List<Site> availableSites;

    public void validate(Errors errors) {
        if (getAssign() && getAvailableSites().isEmpty()) {
            errors.reject("error.please.select.an.available.site");
        } else if (!getAssign() && getAssignedSites().isEmpty()) {
            errors.reject("error.please.select.an.assigned.site");
        }
    }

    ////// BOUND PROPERTIES

    @SuppressWarnings({"unchecked"})
    public List<Site> getAssignedSites() {
        return assignedSites != null ? assignedSites : Collections.EMPTY_LIST;
    }

    public void setAssignedSites(List<Site> assignedSites) {
        this.assignedSites = assignedSites;
    }

    @SuppressWarnings({"unchecked"})
    public List<Site> getAvailableSites() {
        return availableSites != null ? availableSites : Collections.EMPTY_LIST;
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
    
    public Boolean getAssign() {
        return assign != null ? assign : Boolean.FALSE;
    }

    public void setAssign(Boolean assign) {
        this.assign = assign;
    }
  
}
