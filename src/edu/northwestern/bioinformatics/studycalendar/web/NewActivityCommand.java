package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

/**
 * @author Jaron Sampson
 */
public class NewActivityCommand {
    private String activityName;
    private String activityDescription;
    private Integer activityTypeId;

    public Activity createActivity() {
        Activity activity = new Activity();
//TODO: Get the activity type
        ActivityType activityType = new ActivityType();
//        
        activity.setName(getActivityName());
        activity.setDescription(getActivityDescription());
        activity.setType(activityType);        
        return activity;
    }

    // TODO: validation
    
    

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

    public Integer getActivityTypeId() {
    	return activityTypeId;
    } 

    public void setActivityTypeId(Integer id) {
    	this.activityTypeId = id;
    } 
}
