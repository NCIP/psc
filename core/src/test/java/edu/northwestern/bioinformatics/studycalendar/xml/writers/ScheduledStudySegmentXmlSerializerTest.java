/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class ScheduledStudySegmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledStudySegmentXmlSerializer serializer;
    private ScheduledStudySegment schdSegment;
    private ScheduledActivity activity0, activity1;
    private ScheduledActivityXmlSerializer scheduledActivitySerializer;

    protected void setUp() throws Exception {
        super.setUp();

        scheduledActivitySerializer = registerMockFor(ScheduledActivityXmlSerializer.class);

        serializer = new ScheduledStudySegmentXmlSerializer();
        serializer.setScheduledActivityXmlSerializer(scheduledActivitySerializer);

        StudySegment segment = Fixtures.setGridId("segment-grid0", new StudySegment());

        activity0 = new ScheduledActivity();
        activity1 = new ScheduledActivity();

        schdSegment = setGridId("schdSegment-grid0", new ScheduledStudySegment());
        schdSegment.setStartDate(createDate(2008, Calendar.JANUARY, 1));
        schdSegment.setStartDay(5);
        schdSegment.setStudySegment(segment);
        schdSegment.addEvent(activity0);
        schdSegment.addEvent(activity1);
    }

    public void testCreateElement() {
        expectSerializeScheduledActivities();
        replayMocks();

        Element actual = serializer.createElement(schdSegment);
        verifyMocks();

        assertEquals("Wrong element name", "scheduled-study-segment", actual.getName());
        assertEquals("Wrong id", "schdSegment-grid0", actual.attributeValue("id"));
        assertEquals("Wrong start date", "2008-01-01", actual.attributeValue("start-date"));
        assertEquals("Wrong start date", "5", actual.attributeValue("start-day"));
        assertEquals("Wrong segment id", "segment-grid0", actual.attributeValue("study-segment-id"));
        assertEquals("Wrong scheduled activity element size", 2, actual.elements().size());
    }


    ////// Expect methods
    private void expectSerializeScheduledActivities() {
        expect(scheduledActivitySerializer.createElement(activity0)).andReturn(new BaseElement("scheduled-activity"));
        expect(scheduledActivitySerializer.createElement(activity1)).andReturn(new BaseElement("scheduled-activity"));
    }
}
