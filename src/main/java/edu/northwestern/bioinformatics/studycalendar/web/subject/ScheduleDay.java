package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;

import java.util.Date;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Rhett Sutphin
 */
public class ScheduleDay implements Comparable<ScheduleDay> {
    private Date date;
    private List<ScheduledActivity> activities;
    private boolean hasHiddenActivities;

    public ScheduleDay(Date date) {
        this.date = date;
        activities = new LinkedList<ScheduledActivity>();
    }

    ////// LOGIC

    public int compareTo(ScheduleDay o) {
        return getDate().compareTo(o.getDate());
    }

    public boolean isEmpty() {
        return getActivities().isEmpty() && !getHasHiddenActivities();
    }

    ////// BEAN PROPERTIES

    public Date getDate() {
        return date;
    }

    public List<ScheduledActivity> getActivities() {
        return activities;
    }

    public boolean getHasHiddenActivities() {
        return hasHiddenActivities;
    }

    public void setHasHiddenActivities(boolean hasHiddenActivities) {
        this.hasHiddenActivities = hasHiddenActivities;
    }
}
