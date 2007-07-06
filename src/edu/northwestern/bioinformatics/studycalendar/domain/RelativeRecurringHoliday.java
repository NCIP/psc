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
@Table (name = "holidays")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_holidays_id")
    }
)
@DiscriminatorValue(value="3")
public class RelativeRecurringHoliday extends AbstractHolidayState {
    private Integer number_of_week;
    private String day_of_the_week;
    private Integer month;

    private final String SPACE = " ";

    private final String FIRST_WEEK = "First";
    private final String SECOND_WEEK = "Second";
    private final String THIRD_WEEK = "Third";
    private final String FORTH_WEEK = "Forth";
    private final String FIFTH_WEEK = "Fifth";

    @Transient
    public String getDisplayName(){
            return numberOfTheWeekString() + SPACE +
                    getDayOfTheWeek() + SPACE + "of " + monthString();
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("NumberOfWeek = ");
        sb.append(numberOfTheWeekString());
        sb.append("DayOfTheWeek = ");
        sb.append(getDayOfTheWeek());
        sb.append(" Month = ");
        sb.append(monthString());
        sb.append(super.toString());
        return sb.toString();
    }

    public String numberOfTheWeekString() {
        if (getNumberOfWeek() == 1){
            return FIRST_WEEK;
        } else if (getNumberOfWeek() == 2) {
            return SECOND_WEEK;
        } else if (getNumberOfWeek() == 3) {
            return THIRD_WEEK;
        } else if (getNumberOfWeek() == 4) {
            return FORTH_WEEK;
        } else if (getNumberOfWeek() == 5) {
            return FIFTH_WEEK;
        }
        return null;
    }

    public String monthString() {
        if (getMonth() == 0){
            return "January";
        } else if (getMonth() == 1) {
            return "February";
        } else if (getMonth() == 2) {
            return "March";
        } else if (getMonth() == 3) {
            return "April";
        } else if (getMonth() == 4) {
            return "May";
        } else if (getMonth() == 5) {
            return "June";
        } else if (getMonth() == 6) {
            return "July";
        } else if (getMonth() == 7) {
            return "August";
        } else if (getMonth() == 8) {
            return "September";
        } else if (getMonth() == 9) {
            return "October";
        } else if (getMonth() == 10) {
            return "November";
        } else if (getMonth() == 11) {
            return "December";
        }
        return null;
    }



    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelativeRecurringHoliday that = (RelativeRecurringHoliday) o;

        if (day_of_the_week != null ? !day_of_the_week.equals(that.day_of_the_week) : that.day_of_the_week != null)
            return false;
        if (month != null ? !month.equals(that.month) : that.month != null) return false;
        if (number_of_week != null ? !number_of_week.equals(that.number_of_week) : that.number_of_week != null)
            return false;

        return true;
    }


    public Integer getNumberOfWeek() {
        return number_of_week;
    }

    public void setNumberOfWeek(Integer number_of_week) {
        this.number_of_week = number_of_week;
    }

    public String getDayOfTheWeek() {
        return this.day_of_the_week;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.day_of_the_week = dayOfTheWeek;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
}


//insert into holidays(discriminator_id, site_id, day, month, year, day_of_the_week, status, number_of_week) values (3, 2, null, 9, null, 'Monday', 'Labor Day', 'First')
