package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class AmendmentMailMessage extends StudyCalendarMailMessage{
    private String applicationPath;
    private Study study;
    private Amendment amendment;

    @Override
    protected void onInitialization() {
        createSubjectHeader();
    }

    private void createSubjectHeader() {
        String subjectHeader = study.getAssignedIdentifier()+ " has been amended";
        setSubject(getSubjectPrefix() + subjectHeader);
    }
    public String getTemplateName() {
        return "amendment.ftl";
    }

    protected Map<String, Object> createTemplateContext() {
        validate();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("study", study);
        context.put("amendment", amendment);
        if (!amendment.isMandatory()) {
            context.put("nonMandatory", "true");
        }
        context.put("LinkToPsc", applicationPath);
        return context;
    }

    private void validate() {
        if (study == null) {
            throw new NullPointerException("study must be set");
        }
        if (amendment == null) {
            throw new NullPointerException("amendment must be set");
        }
        if (applicationPath == null) {
            throw new NullPointerException("applicationPath must be present");
        }
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
    }

    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
    }
}
