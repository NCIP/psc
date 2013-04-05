/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
public class StudySite extends AbstractProvidableDomainObject implements Serializable, Named {
    private Site site;
    private Study study;
    private List<StudySubjectAssignment> studySubjectAssignments;
    private List<AmendmentApproval> amendmentApprovals;

    public StudySite() {
        studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
        amendmentApprovals = new ArrayList<AmendmentApproval>();
    }

    public StudySite(Study study, Site site) {
        this();
        this.study = study;
        this.site = site;
    }

    ////// LOGIC

    @Transient
    public String getName() {
        return new StringBuilder()
            .append(getStudy() == null ? "<none>" : getStudy().getName())
            .append(": ")
            .append(getSite() == null ? "<none>" : getSite().getName())
            .toString();
    }

    public void setName(String name) {
        throw new UnsupportedOperationException("Name is computed");
    }

    public void addStudySubjectAssignment(StudySubjectAssignment assignment) {
        getStudySubjectAssignments().add(assignment);
        assignment.setStudySite(this);
    }

    /** Are there any assignments using this relationship? */
    @Transient
    public boolean isUsed() {
        return getStudySubjectAssignments().size() > 0;
    }

    /**
     * Note that higher-level code should generally use
     * {@link edu.northwestern.bioinformatics.studycalendar.service.AmendmentService#approve(StudySite, edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval[])}
     * and not this method.  This method updates the internal structures
     * to record the fact that the amendment was approved, but does not
     * do any of the logic associated with approving amendments (e.g., applying
     * mandatory ones to existing schedules)
     */
    public void approveAmendment(Amendment amendment, Date approvalDate) {
        AmendmentApproval approval = new AmendmentApproval();
        approval.setStudySite(this);
        approval.setAmendment(amendment);
        approval.setDate(approvalDate);
        addAmendmentApproval(approval);
    }

    public void addAmendmentApproval(AmendmentApproval approval) {
        // verify that the amendment applies
        Amendment test = getStudy().getAmendment();
        while (test != null && !test.equals(approval.getAmendment())) {
            test = test.getPreviousAmendment();
        }
        if (test == null) {
            throw new StudyCalendarSystemException("The designated amendment (%s) is not part of this study",
                    approval.getAmendment().getDisplayName());
        }

        approval.setStudySite(this);
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
    public static StudySite findStudySite(Study study, Site site) {
        if (study != null && site != null) {
            Collection<StudySite> studySite
                = CollectionUtils.intersection(study.getStudySites(), site.getStudySites());
            if (studySite != null && studySite.size() > 0) {
                return studySite.iterator().next();
            }
        }
        return null;
    }

    @Transient
    public List<StudySubjectAssignment> getOnStudyAssignments() {
        List<StudySubjectAssignment> matches
            = new ArrayList<StudySubjectAssignment>(getStudySubjectAssignments());
        for (Iterator<StudySubjectAssignment> it = matches.iterator(); it.hasNext();) {
            if (it.next().isOff()) it.remove();
        }
        return matches;
    }

    @Transient
    public List<StudySubjectAssignment> getOffStudyAssignments() {
        List<StudySubjectAssignment> matches
            = new ArrayList<StudySubjectAssignment>(getStudySubjectAssignments());
        for (Iterator<StudySubjectAssignment> it = matches.iterator(); it.hasNext();) {
            if (!it.next().isOff()) it.remove();
        }
        return matches;
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

    @OneToMany(mappedBy = "studySite")
    @Cascade({ CascadeType.ALL })
    @OrderBy(clause = "approval_date, amendment_id") // amendment_id is used as a proxy for amendment.date
    public List<AmendmentApproval> getAmendmentApprovals() {
        return amendmentApprovals;
    }

    public void setAmendmentApprovals(List<AmendmentApproval> amendmentApprovals) {
        this.amendmentApprovals = amendmentApprovals;
    }

    ////// OBJECT METHODS

    @Override
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

    @Override
    public int hashCode() {
        int result;
        result = (site != null ? site.hashCode() : 0);
        result = 29 * result + (study != null ? study.hashCode() : 0);
        //result = 29 * result + (studySubjectAssignments != null ? studySubjectAssignments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[study=").append(getStudy())
            .append("; site=").append(getSite())
            .append(']').toString();
    }
}
