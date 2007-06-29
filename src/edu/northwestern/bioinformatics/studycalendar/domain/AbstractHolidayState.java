package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * @author Nataliya Shurupova
 */

@Entity
@Table (name = "holidays")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_holidays_id")
    }
)

@DiscriminatorColumn(name="discriminator_id", discriminatorType = DiscriminatorType.INTEGER)

public class AbstractHolidayState extends AbstractDomainObject {

    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(" Status = ");
        sb.append(getStatus());
        return sb.toString();
    }
}

