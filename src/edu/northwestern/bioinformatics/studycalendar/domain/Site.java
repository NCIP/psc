package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "site")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_site_id")
    }
)
public class Site extends AbstractDomainObject {
    private String name;
    
    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
