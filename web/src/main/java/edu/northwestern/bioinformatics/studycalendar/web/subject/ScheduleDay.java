/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
 */
public class ScheduleDay implements Comparable<ScheduleDay> {
    private Date date;
    private Date today;
    private SortedSet<ScheduledActivity> activities;
    private boolean hasHiddenActivities;

    private static ThreadLocal<DateFormat> DATE_CLASS_FORMATTER = new ThreadLocal<DateFormat>();

    public ScheduleDay(Date date) {
        this(date, new Date());
    }

    public ScheduleDay(Date date, Date today) {
        this.date = date;
        this.today = today;
        activities = new TreeSet<ScheduledActivity>();
    }

    ////// LOGIC

    public int compareTo(ScheduleDay o) {
        return getDate().compareTo(o.getDate());
    }

    public boolean isToday() {
        return DateTools.daysEqual(getDate(), today);
    }

    public boolean isEmpty() {
        return getActivities().isEmpty() && !getHasHiddenActivities();
    }

    public String getDetailTimelineClasses() {
        Calendar queriableDate = Calendar.getInstance();
        queriableDate.setTime(getDate());

        List<String> classes = new ArrayList<String>(6);
        classes.add("day");
        classes.add(dateClass(getDate()));
        if (queriableDate.get(Calendar.DAY_OF_MONTH) == 1) classes.add("month-start");
        if (queriableDate.get(Calendar.DAY_OF_YEAR) == 1) classes.add("year-start");
        if (isToday()) classes.add("today");
        if (!isEmpty()) classes.add("has-activities");

        return StringUtils.join(classes.iterator(), ' ');
    }

    private static String dateClass(Date date) {
        if (DATE_CLASS_FORMATTER.get() == null) {
            DATE_CLASS_FORMATTER.set(new SimpleDateFormat("yyyy-MM-dd"));
        }
        return "date-" + DATE_CLASS_FORMATTER.get().format(date);
    }

    ////// BEAN PROPERTIES

    public Date getDate() {
        return date;
    }

    public SortedSet<ScheduledActivity> getActivities() {
        return activities;
    }

    public boolean getHasHiddenActivities() {
        return hasHiddenActivities;
    }

    public void setHasHiddenActivities(boolean hasHiddenActivities) {
        this.hasHiddenActivities = hasHiddenActivities;
    }
}
