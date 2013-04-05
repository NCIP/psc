/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

/**
 * @author Nataliya Shurupova
 */


@Entity
@DiscriminatorValue(value="3")
public class RelativeRecurringBlackout extends BlackoutDate {
    private Integer weekNumber;
    private String dayOfTheWeek;
    private Integer month;

    private final String SPACE = " ";

    private final String FIRST_WEEK = "First";
    private final String SECOND_WEEK = "Second";
    private final String THIRD_WEEK = "Third";
    private final String FOURTH_WEEK = "Fourth";
    private final String FIFTH_WEEK = "Fifth";

    @Transient
    public String getDisplayName(){
            return numberOfTheWeekString() + SPACE +
                    getDayOfTheWeek() + SPACE + "of " + monthString();
    }

    @Transient
    public int getDayOfTheWeekInteger() {
        return mapDayNameToInteger(getDayOfTheWeek());
    }

    @Override
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
        if (getWeekNumber() == 1){
            return FIRST_WEEK;
        } else if (getWeekNumber() == 2) {
            return SECOND_WEEK;
        } else if (getWeekNumber() == 3) {
            return THIRD_WEEK;
        } else if (getWeekNumber() == 4) {
            return FOURTH_WEEK;
        } else if (getWeekNumber() == 5) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelativeRecurringBlackout that = (RelativeRecurringBlackout) o;

        if (dayOfTheWeek != null ? !dayOfTheWeek.equals(that.getDayOfTheWeek()) : that.getDayOfTheWeek() != null)
            return false;
        if (month != null ? !month.equals(that.getMonth()) : that.getMonth()!= null) return false;
        if (weekNumber != null ? !weekNumber.equals(that.getWeekNumber()) : that.getWeekNumber() != null)
            return false;

        return true;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer week_number) {
        this.weekNumber = week_number;
    }

    public String getDayOfTheWeek() {
        return this.dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
}


