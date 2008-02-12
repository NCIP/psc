package edu.northwestern.bioinformatics.studycalendar.web.admin;

import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationCommand {
    private BindableConfiguration conf;

    public ConfigurationCommand(Configuration configuration) {
        conf = new BindableConfiguration(configuration);
    }

    public BindableConfiguration getConf() {
        return conf;
    }
}
