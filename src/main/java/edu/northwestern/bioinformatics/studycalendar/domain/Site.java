package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Padmaja Vedula
 */
@Entity
@Table (name = "sites")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_sites_id")
    }
)
public class Site extends AbstractMutableDomainObject implements Named {
    public static final String DEFAULT_SITE_NAME = "default";

    private String name;
    private List<StudySite> studySites = new ArrayList<StudySite>();

    private List<Holiday> holidaysAndWeekends = new ArrayList<Holiday>();

    ////// LOGIC

    public void addStudySite(StudySite studySite) {
        getStudySites().add(studySite);
        studySite.setSite(this);
    }

    public StudySite getStudySite(Study study) {
        for (StudySite studySite : getStudySites()) {
            if (studySite.getStudy().equals(study)) return studySite;
        }
        return null;
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStudySites(List<StudySite> studySites) {
        this.studySites = studySites;
    }

    @OneToMany (mappedBy = "site",fetch = FetchType.EAGER)
    @OrderBy // order by ID for testing consistency
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudySite> getStudySites() {
        return studySites;
    }

    public void setHolidaysAndWeekends (List<Holiday> holidaysAndWeekends) {
        this.holidaysAndWeekends = holidaysAndWeekends;
    }

    @OneToMany
    @JoinColumn(name = "site_id", nullable = false)
    @OrderBy // order by ID for testing consistency
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<Holiday> getHolidaysAndWeekends() {
        return holidaysAndWeekends;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Site site = (Site) o;

        if (name != null ? !name.equals(site.name) : site.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }
}

