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
@DiscriminatorValue(value="2")


// id | version | site_id | day | month | year | dayoftheweek |      status
//----+---------+---------+-----+-------+------+--------------+------------------
//  1 |       0 |       1 |   1 |     1 |      |              | New Year Day
//  2 |       0 |       1 |   2 |     4 |      |              | Independence Day
//  3 |       0 |       1 |  22 |    11 |      |              | Thanksgiving Day
//  4 |       0 |       1 |  25 |    12 |      |              | Christmas Day
//  5 |       0 |       1 |     |       |      | Monday       | Office is Closed
//  6 |       0 |       2 |  22 |    11 |      |              | Thanksgiving Day
//  7 |       0 |       2 |  25 |    12 |      |              | Christmas Day
//  8 |       0 |       2 |  12 |     3 | 2008 |              | Easter
//  9 |       0 |       2 |     |       |      | Saturday     | Office is Closed
// 10 |       0 |       2 |     |       |      | Sunday       | Office is Closed

//insert into holidays (discriminator_id, site_id, day, month, year, day_of_the_week, status)
// values (1, 1, 1, 1, null, null, 'New Year Day'),
// (1, 1, 2, 4, null, null, 'Independence Day'),
// (1, 1, 22, 11, null, null, 'Thanksgiving Day'),
// (1, 1, 25, 12, null, null, 'Christmas Day'),
// (2, 1, null, null, null, 'Monday', 'Office is Closed'),
// (1, 2, 22, 11, null, null, 'Thanksgiving Day'),
// (1, 2, 25, 12, null, null, 'Christmas Day'),
// (1, 2, 12, 3, 2008, null, 'Easter'),
// (2, 2, null, null, null, 'Saturday', 'Office is Closed'),
// (2, 2, null, null, null, 'Sunday', 'Office is Closed')


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


