package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
public class Site extends AbstractDomainObject {
    public static final String DEFAULT_SITE_NAME = "default";

    private String name;
    private List<StudySite> studySites = new ArrayList<StudySite>();

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Site)) return false;
        final Site site = (Site) obj;
        if (!getName().equals(site.getName())) return false;
        return true;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public void setStudySites(List<StudySite> studySites) {
        this.studySites = studySites;
    }
    
    @OneToMany (mappedBy = "site",fetch = FetchType.EAGER)
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudySite> getStudySites() {
        return studySites;
    }
}

