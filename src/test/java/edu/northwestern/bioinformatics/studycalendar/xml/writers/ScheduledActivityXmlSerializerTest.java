package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledActivityXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledActivity activity;
    private ScheduledActivityXmlSerializer serializer;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new ScheduledActivityXmlSerializer();

        PlannedActivity plannedActivity = setGridId("planned-activity-grid0", new PlannedActivity());

        activity = new ScheduledActivity();
        activity.setIdealDate(DateUtils.createDate(2008, Calendar.JANUARY, 15));
        activity.setNotes("some notes");
        activity.setDetails("some details");
        activity.setRepetitionNumber(3);
        activity.setPlannedActivity(plannedActivity);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(activity);
        assertEquals("Wrong element name", "scheduled-activity", actual.getName());
        assertEquals("Wrong ideal date", "2008-01-15", actual.attributeValue("ideal-date"));
        assertEquals("Wrong notes", "some notes", actual.attributeValue("notes"));
        assertEquals("Wrong details", "some details", actual.attributeValue("details"));
        assertEquals("Wrong repitition number", "3", actual.attributeValue("repitition-number"));
        assertEquals("Wrong planned activity id", "planned-activity-grid0", actual.attributeValue("planned-activity-id"));
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("scheduled-activity"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a scheduled activity element does not exist", success.getMessage());
        }
    }
}
