package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import static java.util.Calendar.JANUARY;

/**
 * @author John Dzak
 */
public class ScheduledActivityStateXmlSerializerTest extends StudyCalendarXmlTestCase {
    private CurrentScheduledActivityStateXmlSerializer serializer;
    private ScheduledActivityState missed, scheduled, occurred, canceled, conditional, notApplicable;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new CurrentScheduledActivityStateXmlSerializer();

        missed = missed();
        occurred = occurred();
        canceled = canceled();
        scheduled = scheduled();
        conditional = conditional();
        notApplicable = notApplicable();
    }

    public void testCreateElementWhenStateIsMissed() {
        Element actual = serializer.createElement(missed);
        assertStateAttributesEquals("missed", "some reason", null, actual);
    }

    public void testCreateElementWhenStateIsOccurred() {
        Element actual = serializer.createElement(occurred);
        assertStateAttributesEquals("occurred", "some reason", "2008-01-05", actual);
    }

    public void testCreateElementWhenStateIsScheduled() {
        Element actual = serializer.createElement(scheduled);
        assertStateAttributesEquals("scheduled", "some reason", "2008-01-05", actual);
    }

    public void testCreateElementWhenStateIsCancelled() {
        Element actual = serializer.createElement(canceled);
        assertStateAttributesEquals("canceled", "some reason", null, actual);
    }

    public void testCreateElementWhenStateIsConditional() {
        Element actual = serializer.createElement(conditional);
        assertStateAttributesEquals("conditional", "some reason", "2008-01-05", actual);
    }

    public void testCreateElementWhenStateIsNotApplicable() {
        Element actual = serializer.createElement(notApplicable);
        assertStateAttributesEquals("not-applicable", "some reason", null, actual);
    }

    public void testCreateElementForPreviousScheduledActivityState() {
        PreviousScheduledActivityStateXmlSerializer prevStateSerializer = new PreviousScheduledActivityStateXmlSerializer();
        Element actual = prevStateSerializer.createElement(scheduled);
        assertEquals("Wrong element name", "previous-scheduled-activity-state", actual.getName());
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("current-scheduled-activity-state"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a scheduled activity state element does not exist", success.getMessage());
        }
    }

    ////// Helper Asserts
    public void assertStateAttributesEquals(String expectedState, String expectedReason, String expectedDate, Element actual) {
        assertEquals("Wrong element name", "current-scheduled-activity-state", actual.getName());
        assertEquals("Wrong state", expectedState, actual.attributeValue("state"));
        assertEquals("Wrong reason", expectedReason, actual.attributeValue("reason"));
        assertEquals("Wrong date", expectedDate, actual.attributeValue("date"));
    }

    ////// Helper Methods
    private ScheduledActivityState missed() { return setBaseAttributes(new Missed()); }

    private ScheduledActivityState scheduled() { return setDateAttributes(new Scheduled()); }

    private ScheduledActivityState occurred() { return setDateAttributes(new Occurred()); }

    private ScheduledActivityState canceled() { return setBaseAttributes(new Canceled()); }

    private ScheduledActivityState conditional() { return setDateAttributes(new Conditional()); }

    private ScheduledActivityState notApplicable() { return setBaseAttributes(new NotApplicable()); }


    private ScheduledActivityState setDateAttributes(DatedScheduledActivityState state) {
        state.setDate(DateUtils.createDate(2008, JANUARY, 5));
        return setBaseAttributes(state);
    }

    private ScheduledActivityState setBaseAttributes(ScheduledActivityState state) {
        state.setReason("some reason");
        return state;
    }

}
