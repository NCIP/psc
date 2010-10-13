package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.DomainObjectTools;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Ram Chilukuri
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "subject_assignments")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_subject_assignments_id")
    }
)
public class StudySubjectAssignment extends AbstractMutableDomainObject implements Comparable<StudySubjectAssignment> {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String studySubjectId;
    private StudySite studySite;
    private Subject subject;

    private Date startDate;
    private Date endDate;
    private Integer managerCsmUserId;

    private Amendment currentAmendment;
    private ScheduledCalendar scheduledCalendar;
    private List<Notification> notifications = new LinkedList<Notification>();
    private Set<Population> populations = new HashSet<Population>();
    private gov.nih.nci.security.authorization.domainobjects.User resolvedManager;

    ////// LOGIC

    @Transient
    public List<Notification> getCurrentAeNotifications() {
        List<Notification> aeNotifications = new LinkedList<Notification>();
        for (Notification notification : getNotifications()) {
            if (!notification.isDismissed()) aeNotifications.add(notification);
        }
        return aeNotifications;
    }

    public void addAeNotification(Notification notification) {
        addNotification(notification);
    }

    /**
     * Provides a human-readable name for this assignment.  In general this will be only the
     * study name.  In cases where the subject is on the same study more than once, it will
     * include disambiguating information. 
     * @return
     */
    @Transient
    public String getName() {
        StringBuilder sb = new StringBuilder(getStudySite().getStudy().getName());

        List<StudySubjectAssignment> sameStudyAssignments = new LinkedList<StudySubjectAssignment>();
        for (StudySubjectAssignment assignment : getSubject().getAssignments()) {
            if (assignment.getStudySite().getStudy().equals(this.getStudySite().getStudy())) {
                sameStudyAssignments.add(assignment);
            }
        }
        if (sameStudyAssignments.size() == 1) return sb.toString();

        Map<Site, List<StudySubjectAssignment>> bySite = new LinkedHashMap<Site, List<StudySubjectAssignment>>();
        for (StudySubjectAssignment assignment : sameStudyAssignments) {
            Site site = assignment.getStudySite().getSite();
            if (!bySite.containsKey(site)) {
                bySite.put(site, new LinkedList<StudySubjectAssignment>());
            }
            bySite.get(site).add(assignment);
        }

        if (bySite.size() != 1) {
            sb.append(" at ").append(getStudySite().getSite().getName());
        }

        List<StudySubjectAssignment> sameSite = bySite.get(getStudySite().getSite());
        if (sameSite.size() != 1) {
            sb.append(" (").append(sameSite.indexOf(this) + 1).append(')');
        }
        return sb.toString();
    }

    @Transient
    public List<Amendment> getAvailableUnappliedAmendments() {
        List<Amendment> allAmendments = new ArrayList<Amendment>(getStudySite().getStudy().getAmendmentsList());
        Collections.reverse(allAmendments);
        log.trace("All amendments: {}", allAmendments);
        // remove all amendments up to and including the current applied one
        for (Iterator<Amendment> it = allAmendments.iterator(); it.hasNext();) {
            Amendment amendment = it.next();
            it.remove();
            if (amendment.equals(getCurrentAmendment())) break;
        }
        log.trace("After removing up to the current applied: {}", allAmendments);
        // remove all unapproved amendments
        for (Iterator<Amendment> it = allAmendments.iterator(); it.hasNext();) {
            Amendment amendment = it.next();
            if (getStudySite().getAmendmentApproval(amendment) == null) {
                it.remove();
            }
        }
        log.trace("After removing unapproved: {}", allAmendments);
        return allAmendments;
    }

    public void addPopulation(Population population) {
        getPopulations().add(population);
    }

    @Transient
    public boolean isOff() {
        return getEndDate() != null;
    }

    public int compareTo(StudySubjectAssignment o) {
        int result;

        result = getStudySite().getStudy().getAssignedIdentifier().compareTo(
            o.getStudySite().getStudy().getAssignedIdentifier());
        if (result != 0) return result;

        result = getSubject().getLastFirst().compareTo(o.getSubject().getLastFirst());
        if (result != 0) return result;

        result = getStudySite().getSite().getName().compareTo(o.getStudySite().getSite().getName());
        if (result != 0) return result;

        return 0;
    }

    /*
      TODO: I would prefer that this field's type be PscUser.  However, that would introduce
      a dependency on psc:authorization from psc:domain.  psc:authorization's legacy mode support
      forces a dependency on psc:domain, so the reverse dependency isn't possible until legacy
      mode can be completely removed.
     */

    @Transient
    public gov.nih.nci.security.authorization.domainobjects.User getStudySubjectCalendarManager() {
        if (resolvedManager != null) {
            return resolvedManager;
        } else if (getManagerCsmUserId() == null) {
            return null;
        } else {
            throw new StudyCalendarSystemException("The actual manager user object has not been externally resolved for this assignment");
        }
    }

    public void setStudySubjectCalendarManager(gov.nih.nci.security.authorization.domainobjects.User csmUser) {
        this.resolvedManager = csmUser;
        if (csmUser != null) {
            setManagerCsmUserId(csmUser.getUserId().intValue());
        } else {
            setManagerCsmUserId(null);
        }
    }

    ////// BEAN PROPERTIES

    public void setStudySite(StudySite studySite) {
        this.studySite = studySite;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_site_id")
    public StudySite getStudySite() {
        return studySite;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    public Subject getSubject() {
        return subject;
    }

    public Integer getManagerCsmUserId() {
        return managerCsmUserId;
    }

    public void setManagerCsmUserId(Integer managerCsmUserId) {
        if (managerCsmUserId == null) {
            resolvedManager = null;
        } else if ((resolvedManager != null) && (managerCsmUserId != resolvedManager.getUserId().intValue())) {
            resolvedManager = null;
        }
        this.managerCsmUserId = managerCsmUserId;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @OneToOne(mappedBy = "assignment")
    @Cascade(value = CascadeType.ALL)
    public ScheduledCalendar getScheduledCalendar() {
        return scheduledCalendar;
    }

    public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
        this.scheduledCalendar = scheduledCalendar;
        if (scheduledCalendar != null) {
            scheduledCalendar.setAssignment(this);
        }
    }

    @OneToMany(mappedBy = "assignment")
    @Cascade(CascadeType.ALL)
    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @ManyToOne
    public Amendment getCurrentAmendment() {
        return currentAmendment;
    }

    public void setCurrentAmendment(Amendment currentAmendment) {
        this.currentAmendment = currentAmendment;
    }

    public String getStudySubjectId() {
        return studySubjectId;
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    @ManyToMany
    @JoinTable(name = "subject_populations",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "population_id")
    )
    public Set<Population> getPopulations() {
        return populations;
    }

    public void setPopulations(Set<Population> populations) {
        this.populations = populations;
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final StudySubjectAssignment that = (StudySubjectAssignment) o;

        if (startDate != null ? !startDate.equals(that.getStartDate()) : that.getStartDate() != null)
            return false;
        if (studySite != null ? !studySite.equals(that.getStudySite()) : that.getStudySite() != null)
            return false;
        if (currentAmendment != null ? !currentAmendment.equals(that.getCurrentAmendment()) : that.getCurrentAmendment() != null)
            return false;
        // Subject#equals calls this method, so we can't use it here
        if (!DomainObjectTools.equalById(subject, that.subject)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (studySite != null ? studySite.hashCode() : 0);
        result = 29 * result + (subject != null ? subject.hashCode() : 0);
        result = 29 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 29 * result + (currentAmendment != null ? currentAmendment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[subject=").append(getSubject())
            .append("; study site=").append(getStudySite())
            .append(']').toString();
    }

    public void addNotification(final Notification notification) {
        getNotifications().add(notification);
        notification.setAssignment(this);

    }

    /////// COMPARATORS

    public static Comparator<StudySubjectAssignment> byOnOrOff() {
        return OnOffComparator.INSTANCE;
    }

    private static class OnOffComparator implements Comparator<StudySubjectAssignment> {
        public static final Comparator<StudySubjectAssignment> INSTANCE = new OnOffComparator();

        public int compare(StudySubjectAssignment o1, StudySubjectAssignment o2) {
            int result = o1.isOff() ? (o2.isOff() ? 0 : 1) : (o2.isOff() ? -1 : 0);
            if (result != 0) {
                return result;
            } else {
                return o1.compareTo(o2);
            }
        }
    }
}
