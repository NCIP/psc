package edu.northwestern.bioinformatics.studycalendar.web.chrome;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.web.chrome.Task;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class PscSectionTest extends WebTestCase {
    private PscSection section;
    private final boolean[] tempEnabled = new boolean[] { true };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Task permTask = new Task();
        permTask.setDisplayName("Permanent task");
        Task tempTask = new ConditionalTask() {
            @Override
            public boolean isEnabled() {
                return tempEnabled[0];
            }
        };
        tempTask.setDisplayName("Sometimes task");

        section = new PscSection();
        section.setTasks(Arrays.asList(permTask, tempTask));
    }

    public void testGetTasksIncludesPermanentTasks() throws Exception {
        assertEquals("Permanent task not present",
            "Permanent task", section.getTasks().get(0).getDisplayName());
    }

    public void testGetTasksIncludesTemporaryTasksWhenEnabled() throws Exception {
        tempEnabled[0] = true;
        assertEquals("Wrong number of tasks", 2, section.getTasks().size());
        assertEquals("Sometimes task not present",
            "Sometimes task", section.getTasks().get(1).getDisplayName());
    }

    public void testGetTasksDoesNotIncludeTemporaryTasksWhenNotEnabled() throws Exception {
        tempEnabled[0] = false;
        assertEquals("Wrong number of tasks", 1, section.getTasks().size());
        assertNotEquals("Sometimes task is present",
            "Sometimes task", section.getTasks().get(0).getDisplayName());
    }
}
