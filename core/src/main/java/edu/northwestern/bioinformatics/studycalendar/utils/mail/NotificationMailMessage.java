/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class NotificationMailMessage extends StudyCalendarMailMessage{
    private String applicationPath;
    private String subjectHeader;
    private String message;

    @Override
    protected void onInitialization() {
        createSubjectHeader();
    }

    private void createSubjectHeader() {

        setSubject(getSubjectPrefix().concat(subjectHeader));
    }
    public String getTemplateName() {
        return "notification.ftl";
    }

    protected Map<String, Object> createTemplateContext() {
        validate();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", message);
        context.put("linkToPsc", applicationPath);
        return context;
    }

    private void validate() {
        if (message == null || message.length() == 0) {
            throw new NullPointerException("Message for mail must be set");
        }
        if (applicationPath == null) {
            throw new NullPointerException("applicationPath must be present");
        }
    }

    public void setSubjectHeader(String subjectHeader) {
        this.subjectHeader = subjectHeader;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
    }
}
