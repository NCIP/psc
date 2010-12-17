package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_actions")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {@Parameter(name = "sequence", value = "seq_user_actions_id")}
)
public class UserAction extends AbstractMutableDomainObject {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
