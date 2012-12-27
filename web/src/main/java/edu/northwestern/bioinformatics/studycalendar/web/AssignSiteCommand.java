/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Padmaja Vedula
 */

public class AssignSiteCommand implements Validatable {
    private Integer studyId;
    private Boolean assign;
    private List<Site> assignedSites;
    private List<Site> availableSites;
    private StudyDao studyDao;

    public AssignSiteCommand(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void validate(Errors errors) {
        if (getAssign() && getAvailableSites().isEmpty()) {
            errors.reject("error.please.select.an.available.site");
        } else if (!getAssign() && getAssignedSites().isEmpty()) {
            errors.reject("error.please.select.an.assigned.site");
        }

        if (getStudyId() != null) {
            Study study = studyDao.getById(getStudyId());

            List<StudySite> cantRemove = new ArrayList<StudySite>();
            for (Site site : getAssignedSites()) {
                StudySite found = StudySite.findStudySite(study, site);
                if (found != null && found.isUsed()) {
                    cantRemove.add(found);
                }
            }

            if (cantRemove.size() > 0) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<StudySite> it = cantRemove.iterator(); it.hasNext();) {
                    Site site = it.next().getSite();
                    msg.append(site.getName());
                    if (it.hasNext()) msg.append(", ");
                }

                errors.reject("error.cannot.remove.site.from.study.because.subjects.assigned", new Object[] {
                        cantRemove.size(),
                        cantRemove.size() > 1? "sites" : "site",
                        msg,
                        study.getName()},
                        "Error removing assigned sites."
                );
            }
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
