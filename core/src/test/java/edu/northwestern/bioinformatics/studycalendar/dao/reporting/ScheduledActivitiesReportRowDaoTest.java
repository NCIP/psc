/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.apache.commons.httpclient.util.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertDayOfDate;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDaoTest extends
    ReportDaoTestCase<ScheduledActivitiesReportFilters, ScheduledActivitiesReportRow, ScheduledActivitiesReportRowDao>
{
    private static final long NEG_16 = -16;
    private static final long NEG_17 = -17;
    private static final long NEG_18 = -18;
    private static final long NEG_19 = -19;

    @Override
    protected ScheduledActivitiesReportFilters createFilters() {
        return new ScheduledActivitiesReportFilters();
    }

    // TODO: separate out test for bound values
    public void testSearchWithStudyFilter_Pos() {
        filters.setStudyAssignedIdentifier("Foo");
        ScheduledActivitiesReportRow row = assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16).get(2);

        assertNotNull("ID should not be null", row.getId());

        ScheduledActivity schd = row.getScheduledActivity();
        assertNotNull(schd.getDetails());
        assertNotNull("Scheduled Activity id should not be null", schd.getId());
        assertNotNull("Scheduled Activity actual date should not be null", schd.getCurrentState());
        assertNotNull("Scheduled Activity ideal date should not be null", schd.getIdealDate());
        assertEquals("Scheduled activity grid ID is wrong", "G17", schd.getGridId());

        Activity acivity = schd.getActivity();
        assertNotNull("Activity id should not be null", acivity.getId());
        assertNotNull("Activity name should not be null", acivity.getName());
        assertEquals("Wrong activity type", "Procedure", acivity.getType().getName());

        assertNotNull("Subject should not be null", row.getSubject());
        assertEquals("Subject grid ID is wrong", row.getSubject().getGridId(), "123");

        assertNotNull("Study should not be null", row.getStudy());

        assertNotNull("Site should not be null", row.getSite());
        assertEquals("Manager ID is wrong", new Long(-200L), row.getResponsibleUserCsmUserId());
    }

    public void testSearchWithStudyFilter_Neg() {
        filters.setStudyAssignedIdentifier("Fla");
        assertSearchWithResults();
    }

    public void testSearchWithScheduledActivityMode_Scheduled() {
        filters.setCurrentStateModes(
            Arrays.<ScheduledActivityMode>asList(ScheduledActivityMode.SCHEDULED));
        assertSearchWithResults(NEG_16);
    }

    public void testSearchWithScheduledActivityMode_Canceled() {
        filters.setCurrentStateModes(
            Arrays.<ScheduledActivityMode>asList(ScheduledActivityMode.CANCELED));
        assertSearchWithResults(NEG_17);
    }

    public void testSearchWithScheduledActivityMode_Occurred() {
        filters.setCurrentStateModes(
            Arrays.<ScheduledActivityMode>asList(ScheduledActivityMode.OCCURRED));
        assertSearchWithResults();
    }

    public void testSearchWithScheduledActivityMode_ScheduledAndCanceled() {
        filters.setCurrentStateModes(Arrays.<ScheduledActivityMode>asList(
            ScheduledActivityMode.SCHEDULED, ScheduledActivityMode.CANCELED));
        assertSearchWithResults(NEG_17, NEG_16);
    }

    public void testSearchWithSiteFilter_Pos() {
        filters.setSiteName("DC");
        assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
    }

    public void testSearchWithLabelFilter() {
        filters.setLabel("LABELB");
        assertSearchWithResults(NEG_17, NEG_16);
    }

    public void testSearchWithSiteFilter_Neg() {
        filters.setSiteName("Bedrock");
        assertSearchWithResults();
    }

    public void testSearchWithStartDateFilter_Pos() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_19, NEG_18, NEG_17);
    }

    public void testSearchWithStartDateFilter_Neg() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 15));
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_19, NEG_18);
    }

    public void testSearchWithStopDateFilter_Pos() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStop(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_16);
    }

    public void testSearchWithStopDateFilter_Neg() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStop(DateUtils.createDate(2006, Calendar.OCTOBER, 25));
        filters.setActualActivityDate(range);
        assertSearchWithResults();
    }

    public void testSearchWithStartIdealDateFilter_Pos() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
        filters.setIdealDate(range);
        assertSearchWithResults(NEG_19, NEG_18);
    }

    public void testSearchWithStopIdealDateFilter_Pos() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStop(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
        filters.setIdealDate(range);
        assertSearchWithResults(NEG_17, NEG_16);
    }

    public void testSearchWithStartIdealDateFilter_Neg() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(DateUtils.createDate(2007, Calendar.DECEMBER, 5));
        filters.setIdealDate(range);
        assertSearchWithResults();
    }

    public void testSearchWithStopIdealDateFilter_Neg() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStop(DateUtils.createDate(2006, Calendar.OCTOBER, 25));
        filters.setIdealDate(range);
        assertSearchWithResults();
    }

    public void testSearchWithActivityTypeFilter_Pos() {
        ActivityType activityType = Fixtures.setId(2, new ActivityType());
        filters.setActivityTypes(Arrays.asList(activityType));
        assertSearchWithResults(NEG_19, NEG_17);
    }

    public void testSearchWithActivityTypeFilter_Multiple() {
        ActivityType type1 = Fixtures.setId(1, new ActivityType());
        ActivityType type2 = Fixtures.setId(2, new ActivityType());
        filters.setActivityTypes(Arrays.asList(type1, type2));
        assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
    }

    public void testSearchWithActivityTypeFilter_Neg() {
        ActivityType activityType = Fixtures.setId(3, new ActivityType());
        filters.setActivityTypes(Arrays.asList(activityType));
        assertSearchWithResults();
    }

    public void testSearchResponsibleUserFilter_Pos() {
        filters.setResponsibleUser(AuthorizationObjectFactory.createCsmUser(-200, "jo"));
        assertSearchWithResults(NEG_19, NEG_17, NEG_16);
    }

    public void testSearchWithSubjectCoordinatorFilter_Neg() {
        filters.setResponsibleUser(AuthorizationObjectFactory.createCsmUser(-201, "bad"));
        assertSearchWithResults();
    }

    public void testSearchWithNoFiltersIsEmpty() {
        assertSearchWithResults();
    }

    public void testRowWithPersonIdFilter() {
        filters.setPersonId("UNIVERSAL");
        assertSearchWithResults(NEG_18, NEG_17, NEG_16);
    }

    public void testRowWithPersonIdAndStartAndEndDate() {
        filters.setPersonId("UNIVERSAL");
        MutableRange<Date> range = new MutableRange<Date>();
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = API_DATE_FORMAT.get().parse("2007-09-29");
            endDate = API_DATE_FORMAT.get().parse("2007-10-29");
        } catch (ParseException pe) {
            pe.getMessage(); 
        }
        range.setStart(startDate);
        range.setStop(endDate);
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_18);
    }

    public void testRowWithLabelFilter() {
        filters.setLabel("LABELB");
        List<ScheduledActivitiesReportRow> rows = doSearch();
        for (ScheduledActivitiesReportRow row : rows ) {
            assertTrue("labelB is not in the row", row.getScheduledActivity().getLabels().contains("labelB"));
        }
    }

    public void testAuthorizedStudySiteIdsFilter() throws Exception {
        filters.setAuthorizedStudySiteIds(Arrays.asList(-2));
        assertSearchWithResults(NEG_18);
    }

    public void testSearchResultsIncludeScheduledSegment() throws Exception {
        ScheduledStudySegment seg = rowForNeg17().getScheduledActivity().getScheduledStudySegment();
        assertNotNull("Scheduled segment not present", seg);
    }

    public void testSearchResultsIncludeScheduledSegmentGridId() throws Exception {
        ScheduledStudySegment seg = rowForNeg17().getScheduledActivity().getScheduledStudySegment();
        assertEquals("Scheduled segment ID wrong", "SSS-21", seg.getGridId());
    }

    public void testSearchResultsIncludeScheduledSegmentStartDay() throws Exception {
        ScheduledStudySegment seg = rowForNeg17().getScheduledActivity().getScheduledStudySegment();
        assertEquals("Scheduled segment start day wrong", (Integer) 1, seg.getStartDay());
    }

    public void testSearchResultsIncludeScheduledSegmentStartDate() throws Exception {
        ScheduledStudySegment seg = rowForNeg17().getScheduledActivity().getScheduledStudySegment();
        assertDayOfDate("Scheduled segment start date wrong",
            2006, Calendar.NOVEMBER, 4, seg.getStartDate());
    }

    private ScheduledActivitiesReportRow rowForNeg17() {
        filters.setStudyAssignedIdentifier("Foo");
        return assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16).get(2);
    }

    private static final ThreadLocal<DateFormat> API_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd"); }
    };
}
