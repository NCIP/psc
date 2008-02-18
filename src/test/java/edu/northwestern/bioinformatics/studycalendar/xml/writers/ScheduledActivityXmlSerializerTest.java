package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledActivityXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledActivity activity;
    private ScheduledActivityXmlSerializer serializer;
    private ScheduledActivityState state;
    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateSerializer;
    private PreviousScheduledActivityStateXmlSerializer previousScheduledActivityStateSerializer;
    private ScheduledActivityState prevState0, prevState1;

    protected void setUp() throws Exception {
        super.setUp();

        currentScheduledActivityStateSerializer = registerMockFor(CurrentScheduledActivityStateXmlSerializer.class);
        previousScheduledActivityStateSerializer = registerMockFor(PreviousScheduledActivityStateXmlSerializer.class);

        serializer = new ScheduledActivityXmlSerializer();
        serializer.setCurrentScheduledActivityStateXmlSerializer(currentScheduledActivityStateSerializer);
        serializer.setPreviousScheduledActivityStateXmlSerializer(previousScheduledActivityStateSerializer);

        PlannedActivity plannedActivity = setGridId("planned-activity-grid0", new PlannedActivity());

        prevState0 = new Scheduled();
        prevState1 = new Canceled();
        state = new Scheduled();

        activity = setGridId("activity-grid0", new ScheduledActivity());
        activity.setIdealDate(DateUtils.createDate(2008, Calendar.JANUARY, 15));
        activity.setNotes("some notes");
        activity.setDetails("some details");
        activity.setRepetitionNumber(3);
        activity.setPlannedActivity(plannedActivity);
        activity.changeState(prevState0);
        activity.changeState(prevState1);
        activity.changeState(state);
    }

    public void testCreateElement() {
        expectSerializePreviousScheduledActivityStates();
        expectSerializeCurrentScheduledActivityState();
        replayMocks();
        
        Element actual = serializer.createElement(activity);
        verifyMocks();

        assertEquals("Wrong element name", "scheduled-activity", actual.getName());
        assertEquals("Wrong id", "activity-grid0", actual.attributeValue("id"));
        assertEquals("Wrong ideal date", "2008-01-15", actual.attributeValue("ideal-date"));
        assertEquals("Wrong notes", "some notes", actual.attributeValue("notes"));
        assertEquals("Wrong details", "some details", actual.attributeValue("details"));
        assertEquals("Wrong repitition number", "3", actual.attributeValue("repitition-number"));
        assertEquals("Wrong planned activity id", "planned-activity-grid0", actual.attributeValue("planned-activity-id"));
        assertNotNull("Scheduled activity state is null", actual.element("current-scheduled-activity-state"));
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("scheduled-activity"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a scheduled activity element does not exist", success.getMessage());
        }
    }

    ////// Expect methods
    private void expectSerializeCurrentScheduledActivityState() {
        expect(currentScheduledActivityStateSerializer.createElement(state)).andReturn(new BaseElement("current-scheduled-activity-state"));
    }

    private void expectSerializePreviousScheduledActivityStates() {
        expect(previousScheduledActivityStateSerializer.createElement(prevState0)).andReturn(new BaseElement("previous-scheduled-activity-state"));
        expect(previousScheduledActivityStateSerializer.createElement(prevState1)).andReturn(new BaseElement("previous-scheduled-activity-state"));
    }
}
