package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.*;

/**
 * @author Nataliya Shurupova
 */


@Entity
@DiscriminatorValue(value="1")
public class MonthDayBlackoutDate extends BlackoutDate {

    private Integer day;
	private Integer month;
	private Integer year;

    @Transient
    public String getDisplayName(){
        if (year==null) {
            return (getMonth()+1)+"/" + getDay();
        } else {
            return (getMonth()+1) +"/" + getDay() +"/" + getYear();
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonthDayBlackoutDate other = (MonthDayBlackoutDate) o;

        if (day != null ? !day.equals(other.day) : other.day != null) return false;
        if (month != null ? !month.equals(other.month) : other.month != null) return false;
        if (year != null ? !year.equals(other.year) : other.year != null) return false;

        return true;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Id = ");
        sb.append(getId());
        sb.append(" Day = ");
        sb.append(getDay());
        sb.append(" Month = ");
        sb.append(getMonth());
        sb.append(" Year = ");
        sb.append(getYear());
        sb.append(super.toString());
        return sb.toString();
    }    

    public Integer getDay() {
        return this.day;
    }

    public Integer getMonth() {
        return this.month;
    }

    public Integer getYear() {
        return this.year;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}


