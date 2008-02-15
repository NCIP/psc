package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledStudySegmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledStudySegmentXmlSerializer serializer;
    private ScheduledStudySegment segment;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new ScheduledStudySegmentXmlSerializer();

        segment = new ScheduledStudySegment();
        segment.setStartDate(DateUtils.createDate(2008, Calendar.JANUARY, 1));
        segment.setStartDay(5);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(segment);
        assertEquals("Wrong element name", "scheduled-study-segment", actual.getName());
        assertEquals("Wrong start date", "2008-01-01", actual.attributeValue("start-date"));
        assertEquals("Wrong start date", "5", actual.attributeValue("start-day"));
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("scheduled-study-segment"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a scheduled study segment element does not exist", success.getMessage());
        }
    }
}
