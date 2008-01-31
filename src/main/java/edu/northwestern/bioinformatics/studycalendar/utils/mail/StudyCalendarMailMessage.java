package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.nwu.bioinformatics.commons.FreeMarkerMailMessage;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarMailMessage extends FreeMarkerMailMessage {
    private Configuration configuration;

    protected String getSubjectPrefix() {
        return '[' + configuration.get(Configuration.DEPLOYMENT_NAME) + ']';
    }

    protected void addCommonProperties(Map<String, Object> map) {
        map.put("configuration", getConfiguration());
    }

    // override to make visible to package-level collaborators
    protected void onInitialization() { }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
