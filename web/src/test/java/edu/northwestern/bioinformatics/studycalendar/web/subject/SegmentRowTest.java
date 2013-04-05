/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static gov.nih.nci.cabig.ctms.lang.DateTools.*;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class SegmentRowTest extends StudyCalendarTestCase {
    private static final Date START_DATE = createDate(2008, Calendar.MARCH, 9);
    private static final Date END_DATE = createDate(2008, Calendar.MARCH, 17);

    private SegmentRow row;

    public void setUp() throws Exception {
        super.setUp();
        row = new SegmentRow(START_DATE, END_DATE, 1, new StudySubjectAssignment());
    }

    public void testWillFitInEmpyMask() throws Exception {
        assertTrue(row.willFit(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 11), 4)));
    }

    public void testWillFitInMaskWhenDoesNotOverlap() throws Exception {
        row.add(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 10), 3));
        assertTrue(row.willFit(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 13), 2)));
    }

    public void testWillNotFitInMaskWithOverlap() throws Exception {
        row.add(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 10), 6));
        assertFalse(row.willFit(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 13), 2)));
    }

    public void testWillNotFitInMaskIfOutOfRangeHigh() throws Exception {
        assertFalse(row.willFit(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 1), 4)));
    }

    public void testWillNotFitInMaskIfOutOfRangeLow() throws Exception {
        assertFalse(row.willFit(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 18), 4)));
    }

    public void testAddWhenWillFit() throws Exception {
        ScheduledStudySegment segment = createScheduledStudySegment(createDate(2008, Calendar.MARCH, 11), 4);
        row.add(segment);
        assertEquals("Not added", 1, row.getSegments().size());
    }

    public void testAddWhenWillNotFit() throws Exception {
        try {
            row.add(createScheduledStudySegment(createDate(2008, Calendar.MARCH, 1), 6));
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException ex) {
            assertContains(ex.getMessage(), "willFit");
        }
    }
}

