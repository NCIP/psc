package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Basic;
import javax.persistence.SequenceGenerator;
import javax.persistence.Column;
import javax.persistence.FetchType;

/**
 * @author Jaron Sampson
 */
@Entity
@Table (name = "activity_types")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_activities_id")
    }
)
public class ActivityType extends AbstractDomainObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
