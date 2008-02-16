package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.easymock.EasyMock;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledActivityXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledActivity activity;
    private ScheduledActivityXmlSerializer serializer;
    private ScheduledActivityState state;
    private ScheduledActivityStateXmlSerializer scheduledActivityStateSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        scheduledActivityStateSerializer = registerMockFor(ScheduledActivityStateXmlSerializer.class);

        serializer = new ScheduledActivityXmlSerializer();
        serializer.setScheduledActivityStateXmlSerializer(scheduledActivityStateSerializer);

        PlannedActivity plannedActivity = setGridId("planned-activity-grid0", new PlannedActivity());

        state = new Scheduled();

        activity = new ScheduledActivity();
        activity.setIdealDate(DateUtils.createDate(2008, Calendar.JANUARY, 15));
        activity.setNotes("some notes");
        activity.setDetails("some details");
        activity.setRepetitionNumber(3);
        activity.setPlannedActivity(plannedActivity);
        activity.changeState(state);
    }

    public void testCreateElement() {
        expectSerializeScheduledActivityState();
        replayMocks();
        
        Element actual = serializer.createElement(activity);
        verifyMocks();

        assertEquals("Wrong element name", "scheduled-activity", actual.getName());
        assertEquals("Wrong ideal date", "2008-01-15", actual.attributeValue("ideal-date"));
        assertEquals("Wrong notes", "some notes", actual.attributeValue("notes"));
        assertEquals("Wrong details", "some details", actual.attributeValue("details"));
        assertEquals("Wrong repitition number", "3", actual.attributeValue("repitition-number"));
        assertEquals("Wrong planned activity id", "planned-activity-grid0", actual.attributeValue("planned-activity-id"));
        assertNotNull("Scheduled activity state is null", actual.element("scheduled-activity-state"));
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
    private void expectSerializeScheduledActivityState() {
        EasyMock.expect(scheduledActivityStateSerializer.createElement(state)).andReturn(new BaseElement("scheduled-activity-state"));
    }
}
