package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.security.util.ObjectSetUtil;
import gov.nih.nci.cagrid.opensaml.artifact.ByteSizedSequence;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Ram Chilukuri
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "study_sites")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_study_sites_id")
    }
)
public class StudySite extends AbstractMutableDomainObject {
    private Site site;
    private Study study;
    private List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
    private List<UserRole> userRoles;
    private List<AmendmentApproval> amendmentApprovals = new ArrayList<AmendmentApproval>();

    ////// LOGIC

    /** Are there any assignments using this relationship? */
    @Transient
    public boolean isUsed() {
        return getStudySubjectAssignments().size() > 0;
    }

    public void approveAmendment(Amendment amendment, Date approvalDate) {
        // verify that the amendment applies
        Amendment test = getStudy().getAmendment();
        while (test != null && !test.equals(amendment)) {
            test = test.getPreviousAmendment();
        }
        if (test == null) {
            throw new StudyCalendarSystemException("The designated amendment (%s) is not part of this study", amendment.getDisplayName());
        }

        AmendmentApproval approval = new AmendmentApproval();
        approval.setStudySite(this);
        approval.setAmendment(amendment);
        approval.setDate(approvalDate);
        getAmendmentApprovals().add(approval);
    }

    @Transient
    public Amendment getCurrentApprovedAmendment() {
        Amendment candidate = getStudy().getAmendment();
        while (candidate != null) {
            for (AmendmentApproval approval : getAmendmentApprovals()) {
                if (approval.getAmendment().equals(candidate)) return candidate;
            }
            candidate = candidate.getPreviousAmendment();
        }
        return null;
    }

    public AmendmentApproval getAmendmentApproval(Amendment approved) {
        for (AmendmentApproval approval : getAmendmentApprovals()) {
            if (approval.getAmendment().equals(approved)) return approval;
        }
        return null;
    }

    @Transient
    public List<Amendment> getUnapprovedAmendments() {
        List<Amendment> unapproved = new LinkedList<Amendment>();
        Amendment candidate = getStudy().getAmendment();
        while (candidate != null) {
            if (getAmendmentApproval(candidate) == null) {
                unapproved.add(0, candidate);
            }
            candidate = candidate.getPreviousAmendment();
        }
        return unapproved;
    }

    @Transient
    @SuppressWarnings({ "unchecked" })
    public static StudySite findStudySite(Study study, Site site) {
        if (study != null && site != null) {
            Collection<StudySite> studySite = ObjectSetUtil.intersect(study.getStudySites(), site.getStudySites());
            if (studySite != null && studySite.size() > 0) {
                return studySite.iterator().next();
            }
        }
        return null;
    }

    ////// BEAN PROPERTIES

    public void setSite(Site site) {
        this.site = site;
    }

    @ManyToOne
    @JoinColumn(name = "site_id")
    public Site getSite() {
        return site;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    @ManyToOne
    @JoinColumn(name = "study_id")
    public Study getStudy() {
        return study;
    }

    public void setStudySubjectAssignments(List<StudySubjectAssignment> studySubjectAssignments) {
        this.studySubjectAssignments = studySubjectAssignments;
    }

    @OneToMany (mappedBy = "studySite")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudySubjectAssignment> getStudySubjectAssignments() {
        return studySubjectAssignments;
    }

    @ManyToMany(mappedBy = "studySites")
    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    @OneToMany(mappedBy = "studySite")
    @Cascade({ CascadeType.ALL })
    public List<AmendmentApproval> getAmendmentApprovals() {
        return amendmentApprovals;
    }

    public void setAmendmentApprovals(List<AmendmentApproval> amendmentApprovals) {
        this.amendmentApprovals = amendmentApprovals;
    }

    ////// OBJECT METHODS

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StudySite)) return false;
        final StudySite studySite = (StudySite) obj;
        Study study = studySite.getStudy();
        Site site = studySite.getSite();
        if (!getStudy().equals(study)) return false;
        if (!getSite().equals(site)) return false;
        return true;
    }

    public int hashCode() {
        int result;
        result = (site != null ? site.hashCode() : 0);
        result = 29 * result + (study != null ? study.hashCode() : 0);
        //result = 29 * result + (studySubjectAssignments != null ? studySubjectAssignments.hashCode() : 0);
        return result;
    }
}
