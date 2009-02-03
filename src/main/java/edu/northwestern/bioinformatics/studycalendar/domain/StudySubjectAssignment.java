package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.DomainObjectTools;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.*;


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
public class StudySubjectAssignment extends AbstractMutableDomainObject {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String studySubjectId;
    private StudySite studySite;
    private Subject subject;

    private Date startDateEpoch;
    private Date endDateEpoch;
    private User subjectCoordinator;

    private Amendment currentAmendment;
    private ScheduledCalendar scheduledCalendar;
    private List<Notification> notifications = new LinkedList<Notification>();
    private Set<Population> populations = new HashSet<Population>();

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_coordinator_id")
    public User getSubjectCoordinator() {
        return subjectCoordinator;
    }

    public void setSubjectCoordinator(User subjectCoordinator) {
        this.subjectCoordinator = subjectCoordinator;
    }

    public void setStartDateEpoch(Date startDateEpoch) {
        this.startDateEpoch = startDateEpoch;
    }

    @Column(name = "first_epoch_stdate")
    public Date getStartDateEpoch() {
        return startDateEpoch;
    }

    public void setEndDateEpoch(Date endDateEpoch) {
        this.endDateEpoch = endDateEpoch;
    }

    @Column(name = "last_epoch_enddate")
    public Date getEndDateEpoch() {
        return endDateEpoch;
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

    @Transient
    public boolean isExpired() {
        return (endDateEpoch != null);
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

        if (startDateEpoch != null ? !startDateEpoch.equals(that.startDateEpoch) : that.startDateEpoch != null)
            return false;
        if (studySite != null ? !studySite.equals(that.studySite) : that.studySite != null)
            return false;
        if (currentAmendment != null ? !currentAmendment.equals(that.currentAmendment) : that.currentAmendment != null)
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
        result = 29 * result + (startDateEpoch != null ? startDateEpoch.hashCode() : 0);
        result = 29 * result + (currentAmendment != null ? currentAmendment.hashCode() : 0);
        return result;
    }

    public void addNotification(final Notification notification) {
        getNotifications().add(notification);
        notification.setAssignment(this);

    }
}
