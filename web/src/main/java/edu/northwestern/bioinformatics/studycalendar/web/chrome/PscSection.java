package edu.northwestern.bioinformatics.studycalendar.web.chrome;

import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscSection extends Section {
    /**
     * Returns the configured tasks, less any conditional tasks which are not enabled.
     */
    @Override
    public List<Task> getTasks() {
        List<Task> enabled = new ArrayList<Task>(getConfiguredTasks().size());
        for (Task task : getConfiguredTasks()) {
            if (task instanceof ConditionalTask) {
                if (((ConditionalTask) task).isEnabled()) {
                    enabled.add(task);
                }
            } else {
                enabled.add(task);
            }
        }
        return enabled;
    }

    /**
     * Returns all the tasks set in #setTasks.
     */
    public List<Task> getConfiguredTasks() {
        return super.getTasks();
    }
}
