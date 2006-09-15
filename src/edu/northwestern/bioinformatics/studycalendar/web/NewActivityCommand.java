package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;

/**
 * @author Jaron Sampson
 */
public class NewActivityCommand {
    private Integer returnToPeriodId;
    private String activityName;
    private String activityDescription;
    private Integer activityTypeId;
    private ActivityTypeDao activityTypeDao;
    
    public NewActivityCommand(ActivityTypeDao activityTypeDao) {
    	this.activityTypeDao = activityTypeDao;
    }

    public Activity createActivity() {
        Activity activity = new Activity();
        activity.setName(getActivityName());
        activity.setDescription(getActivityDescription());
        activity.setType(activityTypeDao.getById(activityTypeId));        
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

    public Integer getReturnToPeriodId() {
        return returnToPeriodId;
    }

    public void setReturnToPeriodId(Integer returnToPeriodId) {
        this.returnToPeriodId = returnToPeriodId;
    }
}
