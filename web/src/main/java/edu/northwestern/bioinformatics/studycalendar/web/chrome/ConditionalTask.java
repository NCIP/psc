package edu.northwestern.bioinformatics.studycalendar.web.chrome;

import gov.nih.nci.cabig.ctms.web.chrome.Task;

/**
 * @author Rhett Sutphin
 */
public abstract class ConditionalTask extends Task {
    public abstract boolean isEnabled();
}
