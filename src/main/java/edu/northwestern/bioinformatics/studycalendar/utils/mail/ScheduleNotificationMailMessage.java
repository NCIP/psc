package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Saurabh Agrawal
 */
public class ScheduleNotificationMailMessage extends StudyCalendarMailMessage {
    private static final String DISPLAY_NULL = "[null]";


    private Notification notification;

    @Override
    protected void onInitialization() {
        setSubject(getSubjectPrefix() + " New schedule notifications");
    }


    public String getTemplateName() {
        return "schedule_notification.ftl";
    }

    protected Map<String, Object> createTemplateContext() {
        validate();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("notification", notification);
        return context;
    }


    private void validate() {
        if (notification == null) {
            throw new NullPointerException("notification must be set");
        }
    }


    public void setNotification(final Notification notification) {
        this.notification = notification;
    }
}

