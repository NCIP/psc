package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

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
@DiscriminatorValue(value="1")

public class Holiday extends AbstractHolidayState {

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

        Holiday holiday = (Holiday) o;

        if (day != null ? !day.equals(holiday.day) : holiday.day != null) return false;
        if (month != null ? !month.equals(holiday.month) : holiday.month != null) return false;
        if (year != null ? !year.equals(holiday.year) : holiday.year != null) return false;

        return true;
    }

    public String toString(){
        String status = super.toString();
        StringBuffer sb = new StringBuffer();
        sb.append("Id = ");
        sb.append(getId());
        sb.append(" Day = ");
        sb.append(getDay());
        sb.append(" Month = ");
        sb.append(getMonth());
        sb.append(" Year = ");
        sb.append(getYear());
        sb.append(status);
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


