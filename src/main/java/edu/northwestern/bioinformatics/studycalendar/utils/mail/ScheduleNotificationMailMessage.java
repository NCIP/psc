package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Saurabh Agrawal
 */
public class ScheduleNotificationMailMessage extends StudyCalendarMailMessage {
    private static final String DISPLAY_NULL = "[null]";

    private StudySubjectAssignment studySubjectAssignment;

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
        context.put("studySubjectAssignment", studySubjectAssignment);
        return context;
    }


    private void validate() {
        if (studySubjectAssignment == null) {
            throw new NullPointerException("studySubjectAssignment must be set");
        }
    }

    public StudySubjectAssignment getStudySubjectAssignment() {
        return studySubjectAssignment;
    }

    public void setStudySubjectAssignment(final StudySubjectAssignment studySubjectAssignment) {
        this.studySubjectAssignment = studySubjectAssignment;
    }

   

}

