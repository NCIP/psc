package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class NextScheduledStudySegmentXmlSerializerTest extends StudyCalendarTestCase {
    private StudySegmentDao studySegmentDao;
    private StudySegment segment;
    private NextScheduledStudySegmentXmlSerializer serializer;

    protected void setUp() throws Exception {
        super.setUp();

        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        
        serializer = new NextScheduledStudySegmentXmlSerializer();
        serializer.setStudySegmentDao(studySegmentDao);

        segment = Fixtures.setGridId("segment-grid0", new StudySegment());
    }

    public void testReadElement() {
        expect(studySegmentDao.getByGridId("segment-grid0")).andReturn(segment);
        replayMocks();

        NextScheduledStudySegment actual = serializer.readElement(createTestElement());
        verifyMocks();

        assertEquals("Wrong start day", 5, (int) actual.getStartDay());
        assertSameDay("Wrong start date", createDate(2008, Calendar.JANUARY, 1), actual.getStartDate());
        assertSame("Wrong next study segment mode", NextStudySegmentMode.PER_PROTOCOL, actual.getMode());
        assertSame("Wrong study segment", segment, actual.getStudySegment());
    }

    ////// Helper methods
    private Element createTestElement() {
        Element elt = new BaseElement("next-scheduled-study-segment");
        elt.addAttribute("start-day", "5");
        elt.addAttribute("start-date", "2008-01-01");
        elt.addAttribute("study-segment-id", "segment-grid0");
        elt.addAttribute("mode", "per-protocol");
        return elt;
    }
}
