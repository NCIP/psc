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

    private String description;
    private static final String SUNDAY = "sunday";
    private static final String MONDAY = "monday";
    private static final String TUESDAY = "tuesday";
    private static final String WEDNESDAY = "wednesday";
    private static final String THURSDAY = "thursday";
    private static final String FRIDAY = "friday";
    private static final String SATURDAY = "saturday";

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int mapDayStringToInt (String day_of_the_week) {
        if (day_of_the_week.toLowerCase().equals(SUNDAY)){
            return 1;
        } else if (day_of_the_week.toLowerCase().equals(MONDAY)) {
            return 2;
        } else if (day_of_the_week.toLowerCase().equals(TUESDAY)) {
            return 3;
        } else if (day_of_the_week.toLowerCase().equals(WEDNESDAY)) {
            return 4;
        } else if (day_of_the_week.toLowerCase().equals(THURSDAY)) {
            return 5;
        } else if (day_of_the_week.toLowerCase().equals(FRIDAY)) {
            return 6;
        } else if (day_of_the_week.toLowerCase().equals(SATURDAY)) {
            return 7;
        }
        return -1;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(" Description = ");
        sb.append(getDescription());
        return sb.toString();
    }
}

