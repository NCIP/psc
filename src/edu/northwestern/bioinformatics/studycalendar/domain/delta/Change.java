package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "changes")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_changes_id")
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="action", discriminatorType = DiscriminatorType.STRING)
public abstract class Change extends AbstractMutableDomainObject {
    /**
     * Return the action used by this change.  It should match the discriminator value for the class.
     * @return
     */
    @Transient
    public abstract ChangeAction getAction();
}
