package edu.northwestern.bioinformatics.studycalendar.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRowDao;

import java.util.List;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDaoTest extends ContextDaoTestCase<ScheduledActivitiesReportRowDao> {
    public void testSearch() {
        List<ScheduledActivitiesReportRow> results = getDao().search();
        assertEquals("Wrong result size", 5, results.size());

        ScheduledActivitiesReportRow row = results.get(0);
        assertNotNull("ID should not be null", row.getId());

        ScheduledActivity schd = row.getScheduledActivity();
        assertNotNull(schd.getDetails());
        assertNotNull("Scheduled Activity id should not be null", schd.getId());
        assertNotNull("Scheduled Activity actual date should not be null", schd.getCurrentState());
        assertNotNull("Scheduled Activity ideal date should not be null", schd.getIdealDate());
    }
}
