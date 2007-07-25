package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;


/**
 * @author Nataliya Shurupova
 */

@Entity
@DiscriminatorValue(value="2")
public class DayOfTheWeek extends AbstractHolidayState {
    private String day_of_the_week;

    @Transient
    public String getDisplayName() {
        return getDayOfTheWeek();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DayOfTheWeek that = (DayOfTheWeek) o;

        if (day_of_the_week != null ? !day_of_the_week.equals(that.day_of_the_week) : that.day_of_the_week != null)
            return false;

        return true;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Id = ");
        sb.append(getId());
        sb.append(" DayOfTheWeek = ");
        sb.append(getDayOfTheWeek());
        sb.append(super.toString());
        return sb.toString();
    }    


    public String getDayOfTheWeek() {
        return this.day_of_the_week;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.day_of_the_week = dayOfTheWeek;
    }
}


