/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledStudySegmentDaoTest extends ContextDaoTestCase<ScheduledStudySegmentDao> {
    @Override
    protected String getTestDataFileName() {
        return "testdata/ScheduledCalendarDaoTest.xml";
    }

    public void testGetById() throws Exception {
        ScheduledStudySegment loaded = getDao().getById(-22);
        assertEquals("Wrong studySegment", -4, (int) loaded.getStudySegment().getId());
        assertEquals("Wrong schedule", -20, (int) loaded.getScheduledCalendar().getId());
        assertEquals("Wrong start day", 2, (int) loaded.getStartDay());
        StudyCalendarTestCase.assertDayOfDate("Wrong start date", 2006, Calendar.OCTOBER, 27, loaded.getStartDate());
    }
}
