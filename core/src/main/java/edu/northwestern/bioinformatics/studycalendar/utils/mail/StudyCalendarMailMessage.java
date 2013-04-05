/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.nwu.bioinformatics.commons.FreeMarkerMailMessage;

import static edu.northwestern.bioinformatics.studycalendar.configuration.Configuration.*;

import java.util.Map;

import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarMailMessage extends FreeMarkerMailMessage {
    private Configuration configuration;

    protected String getSubjectPrefix() {
        return '[' + configuration.get(DEPLOYMENT_NAME) + ']';
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
