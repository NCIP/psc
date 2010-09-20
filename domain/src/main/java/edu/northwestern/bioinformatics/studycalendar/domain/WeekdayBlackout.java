package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;


/**
 * @author Nataliya Shurupova
 */

@Entity
@DiscriminatorValue(value="2")
public class WeekdayBlackout extends BlackoutDate {
    // TODO: This ought to be the java.util.Calendar constant for the day, or a custom enum
    private String dayOfTheWeek;

    @Transient
    public String getDisplayName() {
        return getDayOfTheWeek();
    }

    @Transient
    public int getDayOfTheWeekInteger() {
        return mapDayNameToInteger(getDayOfTheWeek());
    }

    public String getDayOfTheWeek() {
        return this.dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeekdayBlackout that = (WeekdayBlackout) o;

        if (dayOfTheWeek != null ? !dayOfTheWeek.equals(that.getDayOfTheWeek()) : that.getDayOfTheWeek() != null)
            return false;

        return true;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Id = ");
        sb.append(getId());
        sb.append(" DayOfTheWeek = ");
        sb.append(getDayOfTheWeek());
        sb.append(super.toString());
        return sb.toString();
    }
}


