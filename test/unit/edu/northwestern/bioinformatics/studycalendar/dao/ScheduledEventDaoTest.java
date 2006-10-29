package edu.northwestern.bioinformatics.studycalendar.dao;

import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.*;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventDaoTest extends ContextDaoTestCase<ScheduledEventDao> {
    protected String getTestDataFileName() {
        return "testdata/ScheduledCalendarDaoTest.xml";
    }
    
    public void testGetById() throws Exception {
        ScheduledEvent loaded = getDao().getById(-10);

        assertEquals("Wrong planned event", -6, (int) loaded.getPlannedEvent().getId());
        assertEquals("Wrong scheduled arm", -22, (int) loaded.getScheduledArm().getId());
        assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, loaded.getIdealDate());
        assertEquals("Wrong notes", "Boo!", loaded.getNotes());

        assertEquals("Wrong current reason", "Success", loaded.getCurrentState().getReason());
        assertEquals("Wrong current mode", ScheduledEventMode.OCCURRED, loaded.getCurrentState().getMode());

        assertEquals("Wrong number of previous states", 3, loaded.getPreviousStates().size());
    }
}
