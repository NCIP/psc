package edu.northwestern.bioinformatics.studycalendar.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
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
    }
}
