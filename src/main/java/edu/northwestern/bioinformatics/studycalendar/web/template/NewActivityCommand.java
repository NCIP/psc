package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jaron Sampson
 */
public class NewActivityCommand implements Validatable {

    private static final Logger log = LoggerFactory.getLogger(NewActivityCommand.class.getName());

    private Integer returnToPeriodId;
    private String activityName;
    private String activityDescription;
    private ActivityType activityType;
    private Source activitySource;
    private String activityCode;

    private ActivityDao activityDao;

    ////// LOGIC

    public NewActivityCommand(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public Activity createActivity() {
        Activity activity = new Activity();
        activity.setName(getActivityName());
        activity.setDescription(getActivityDescription());
        activity.setType(getActivityType());
        activity.setSource(getActivitySource());
        activity.setCode(getActivityCode());
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

    public Source getActivitySource() {
        return activitySource;
    }

    public void setActivitySource(Source activitySource) {
        this.activitySource = activitySource;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public ActivityDao getActivityDao() {
        return activityDao;
    }

    public void validate(Errors errors) {
        if (getActivityName() != null && getActivityName().length()>0) {
            if (getActivityDao().getByName(getActivityName()) != null){
                errors.rejectValue("activityName", "error.activity.name.already.exists");
            }
        }
        else {
            errors.rejectValue("activityName", "error.activity.name.is.empty");
        }
    }

}
