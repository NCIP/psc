package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.easymock.EasyMock;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledStudySegmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledStudySegmentXmlSerializer serializer;
    private ScheduledStudySegment segment;
    private ScheduledActivity activity0, activity1;
    private ScheduledActivityXmlSerializer scheduledActivitySerializer;

    protected void setUp() throws Exception {
        super.setUp();

        scheduledActivitySerializer = registerMockFor(ScheduledActivityXmlSerializer.class);

        serializer = new ScheduledStudySegmentXmlSerializer();
        serializer.setScheduledActivityXmlSerializer(scheduledActivitySerializer);

        activity0 = new ScheduledActivity();
        activity1 = new ScheduledActivity();

        segment = new ScheduledStudySegment();
        segment.setStartDate(DateUtils.createDate(2008, Calendar.JANUARY, 1));
        segment.setStartDay(5);
        segment.addEvent(activity0);
        segment.addEvent(activity1);
    }

    public void testCreateElement() {
        expectSerializeScheduledActivities();
        replayMocks();

        Element actual = serializer.createElement(segment);
        verifyMocks();

        assertEquals("Wrong element name", "scheduled-study-segment", actual.getName());
        assertEquals("Wrong start date", "2008-01-01", actual.attributeValue("start-date"));
        assertEquals("Wrong start date", "5", actual.attributeValue("start-day"));
        assertEquals("Wrong scheduled activity element size", 2, actual.elements().size());
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("scheduled-study-segment"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a scheduled study segment element does not exist", success.getMessage());
        }
    }

    ////// Expect methods
    private void expectSerializeScheduledActivities() {
        EasyMock.expect(scheduledActivitySerializer.createElement(activity0)).andReturn(new BaseElement("scheduled-activity"));
        EasyMock.expect(scheduledActivitySerializer.createElement(activity1)).andReturn(new BaseElement("scheduled-activity"));
    }
}
