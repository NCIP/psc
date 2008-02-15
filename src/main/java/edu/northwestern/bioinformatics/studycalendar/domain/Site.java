package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table(name = "sites")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {@Parameter(name = "sequence", value = "seq_sites_id")}
)
public class Site extends AbstractMutableDomainObject implements Named, Serializable, NaturallyKeyed {
    private String name;

    private List<StudySite> studySites = new ArrayList<StudySite>();

    private String assignedIdentifier;

    private List<Holiday> holidaysAndWeekends = new ArrayList<Holiday>();

    ////// LOGIC

    public boolean hasAssignments() {
        for (StudySite studySite : getStudySites()) {
            if (studySite.getStudySubjectAssignments().size() > 0) return true;
        }
        return false;
    }

    public void addStudySite(final StudySite studySite) {
        getStudySites().add(studySite);
        studySite.setSite(this);
    }

    public StudySite getStudySite(final Study study) {
        for (StudySite studySite : getStudySites()) {
            if (studySite.getStudy().equals(study)) {
                return studySite;
            }
        }
        return null;
    }

    @Transient
    public String getNaturalKey() {
        return getAssignedIdentifier();
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setStudySites(final List<StudySite> studySites) {
        this.studySites = studySites;
    }

    @OneToMany(mappedBy = "site", fetch = FetchType.EAGER)
    @OrderBy
    // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<StudySite> getStudySites() {
        return studySites;
    }

    public void setHolidaysAndWeekends(final List<Holiday> holidaysAndWeekends) {
        this.holidaysAndWeekends = holidaysAndWeekends;
    }

    public String getAssignedIdentifier() {
        if (assignedIdentifier == null) {
            return getName();
        } else {
            return assignedIdentifier;
        }
    }

    public void setAssignedIdentifier(final String assignedIdentifier) {
        this.assignedIdentifier = assignedIdentifier;
    }

    @OneToMany
    @JoinColumn(name = "site_id", nullable = false)
    @OrderBy
    // order by ID for testing consistency
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<Holiday> getHolidaysAndWeekends() {
        return holidaysAndWeekends;
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Site site = (Site) o;

        if (name != null ? !name.equals(site.name) : site.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Transient
    public boolean checkIfHolidayExists(final Holiday holiday) {

        for (Holiday existingHoliday : this.getHolidaysAndWeekends()) {
            if (existingHoliday.getId() != null && holiday != null && existingHoliday.getId().equals(holiday.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public void removeHoliday(final Holiday holiday) {
        Holiday holidayToRemove = null;
        for (Holiday existingHoliday : this.getHolidaysAndWeekends()) {
            if (existingHoliday.getId() != null && existingHoliday.getId().equals(holiday.getId())) {
                holidayToRemove = existingHoliday;
                break;
            }
        }

        if (holidayToRemove != null) {
            getHolidaysAndWeekends().remove(holidayToRemove);
        }

    }
}
