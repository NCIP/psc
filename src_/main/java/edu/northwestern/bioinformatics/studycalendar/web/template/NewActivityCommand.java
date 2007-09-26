package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

/**
 * @author Jaron Sampson
 */
public class NewActivityCommand {
    private Integer returnToPeriodId;
    private String activityName;
    private String activityDescription;
    private ActivityType activityType;

    ////// LOGIC

    public Activity createActivity() {
        Activity activity = new Activity();
        activity.setName(getActivityName());
        activity.setDescription(getActivityDescription());
        activity.setType(getActivityType());
        return activity;
    }

    ////// BOUND PROPERTIES

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public Integer getReturnToPeriodId() {
        return returnToPeriodId;
    }

    public void setReturnToPeriodId(Integer returnToPeriodId) {
        this.returnToPeriodId = returnToPeriodId;
    }
}
