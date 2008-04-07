package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDaoTest extends
        ReportDaoTestCase<ScheduledActivitiesReportFilters, ScheduledActivitiesReportRow, ScheduledActivitiesReportRowDao> {
    private static final long NEG_16 = -16;
    private static final long NEG_17 = -17;

    protected ScheduledActivitiesReportFilters createFilters() {
        return new ScheduledActivitiesReportFilters();
    }

    public void testSearchWithStudyFilter_Pos() {
        filters.setStudyAssignedIdentifier("Foo");
        ScheduledActivitiesReportRow row = assertSearchWithResults(NEG_17, NEG_16).get(0);

        assertNotNull("ID should not be null", row.getId());

        ScheduledActivity schd = row.getScheduledActivity();
        assertNotNull(schd.getDetails());
        assertNotNull("Scheduled Activity id should not be null", schd.getId());
        assertNotNull("Scheduled Activity actual date should not be null", schd.getCurrentState());
        assertNotNull("Scheduled Activity ideal date should not be null", schd.getIdealDate());

        Activity acivity = schd.getActivity();
        assertNotNull("Activity id should not be null", acivity.getId());
        assertNotNull("Activity name should not be null", acivity.getName());

        assertNotNull("Subject should not be null", row.getSubject());

        assertNotNull("Study should not be null", row.getStudy());

        assertNotNull("Site should not be null", row.getSite());
    }

    public void testSearchWithStudyFilter_Neg() {
        filters.setStudyAssignedIdentifier("Fla");
        assertSearchWithResults();
    }

    public void testSearchWithScheduledActivityMode_Scheduled() {
        filters.setCurrentStateMode(ScheduledActivityMode.SCHEDULED);
        assertSearchWithResults(NEG_16);
    }

    public void testSearchWithScheduledActivityMode_Canceled() {
        filters.setCurrentStateMode(ScheduledActivityMode.CANCELED);
        assertSearchWithResults(NEG_17);
    }

    public void testSearchWithScheduledActivityMode_Occurred() {
        filters.setCurrentStateMode(ScheduledActivityMode.OCCURRED);
        assertSearchWithResults();
    }
}
