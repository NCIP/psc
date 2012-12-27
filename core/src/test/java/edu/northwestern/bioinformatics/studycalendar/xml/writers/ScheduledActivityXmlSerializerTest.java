/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;

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

        prevState0 = ScheduledActivityMode.SCHEDULED.createStateInstance();
        prevState1 = ScheduledActivityMode.CANCELED.createStateInstance();
        state = ScheduledActivityMode.SCHEDULED.createStateInstance();

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

        validateScheduledActivityElement(actual);
    }

    public void testCreateCollectionElement() {
        expectSerializePreviousScheduledActivityStates();
        expectSerializeCurrentScheduledActivityState();
        replayMocks();

        Element collectionElement = serializer.createDocument(Collections.singleton(activity)).getRootElement();
        verifyMocks();

        assertEquals("Wrong element name", "scheduled-activities", collectionElement.getName());
        Element actual = collectionElement.element("scheduled-activity");

        validateScheduledActivityElement(actual);
    }

    public void testCreateDocumentString() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<scheduled-activities");
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(MessageFormat.format("<scheduled-activity  id=\"{0}\" ideal-date=\"2008-01-15\" notes=\"{1}\" day=\"{2}\" details=\"{3}\" planned-activity-id=\"{4}\"/", activity.getGridId(),
                activity.getNotes(), activity.getDetails(), activity.getPlannedActivity().getGridId()));

        expected.append("<current-scheduled-activity-state xmlns=\"\"");
        expected.append("<previous-scheduled-activity-state xmlns=\"\"");
        expected.append("<previous-scheduled-activity-state xmlns=\"\"");
        expected.append("</scheduled-activity>");

        expected.append("</scheduled-activities>");

        expectSerializePreviousScheduledActivityStates();
        expectSerializeCurrentScheduledActivityState();
        replayMocks();


        String actual = serializer.createDocumentString(Collections.singleton(activity));
        verifyMocks();
        assertNotNull(actual);
    }


    private void validateScheduledActivityElement(final Element actual) {
        assertEquals("Wrong element name", "scheduled-activity", actual.getName());
        assertEquals("Wrong id", "activity-grid0", actual.attributeValue("id"));
        assertEquals("Wrong ideal date", "2008-01-15", actual.attributeValue("ideal-date"));
        assertEquals("Wrong notes", "some notes", actual.attributeValue("notes"));
        assertEquals("Wrong details", "some details", actual.attributeValue("details"));
        assertEquals("Wrong repetition number", "3", actual.attributeValue("repetition-number"));
        assertEquals("Wrong planned activity id", "planned-activity-grid0", actual.attributeValue("planned-activity-id"));
        assertNotNull("Scheduled activity state is null", actual.element("current-scheduled-activity-state"));
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("scheduled-activity"));
            fail("Exception should be thrown, method not implemented");
        } catch (UnsupportedOperationException success) {
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
