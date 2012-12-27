/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DynamicMockDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

/**
 * Test for the deletion methods in TemplateService
 *
 * @author Rhett Sutphin
 */
public class TemplateServiceDeletionTest extends StudyCalendarTestCase {
    private PlannedCalendar plannedCalendar;

    private PlannedCalendarDao plannedCalendarDao;
    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;
    private PlannedActivityDao plannedActivityDao;

    private TemplateService service;
    private StudySegment segment;
    private Epoch epoch;
    private Period period;
    private PlannedActivity plannedActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedCalendar = Fixtures.createBasicTemplate().getPlannedCalendar();
        epoch = plannedCalendar.getEpochs().get(1);
        segment = epoch.getStudySegments().get(0);
        plannedActivity = Fixtures.createPlannedActivity("Toothpick", 7);
        period = Fixtures.createPeriod("A", 5, 7, 6);
        period.addPlannedActivity(plannedActivity);
        segment.addPeriod(period);

        DynamicMockDaoFinder daoFinder = new DynamicMockDaoFinder();
        plannedCalendarDao = daoFinder.expectDaoFor(PlannedCalendar.class, PlannedCalendarDao.class);
        epochDao = daoFinder.expectDaoFor(Epoch.class, EpochDao.class);
        studySegmentDao = daoFinder.expectDaoFor(StudySegment.class, StudySegmentDao.class);
        periodDao = daoFinder.expectDaoFor(Period.class, PeriodDao.class);
        plannedActivityDao = daoFinder.expectDaoFor(PlannedActivity.class, PlannedActivityDao.class);

        service = new TemplateService();
        service.setDaoFinder(daoFinder);
    }
    
    public void testDeletePlannedCalendar() throws Exception {
        expectDeleteEpochs(plannedCalendar.getEpochs());
        plannedCalendarDao.delete(plannedCalendar);

        replayMocks();
        service.delete(plannedCalendar);
        verifyMocks();
        
        assertEquals("In-memory collection not cleared", 0, plannedCalendar.getEpochs().size());
    }

    public void testDeleteEpoch() throws Exception {
        expectDeleteSegments(epoch.getStudySegments());
        epochDao.delete(epoch);

        replayMocks();
        service.delete(epoch);
        verifyMocks();

        assertEquals("In-memory collection not cleared", 0, epoch.getStudySegments().size());
    }

    public void testDeleteSegment() throws Exception {
        expectDeletePeriods(segment.getPeriods());
        studySegmentDao.delete(segment);

        replayMocks();
        service.delete(epoch);
        verifyMocks();

        assertEquals("In-memory collection not cleared", 0, segment.getPeriods().size());
    }

    public void testDeletePeriod() throws Exception {
        expectDeletePlannedActivities(period.getPlannedActivities());
        periodDao.delete(period);

        replayMocks();
        service.delete(epoch);
        verifyMocks();

        assertEquals("In-memory collection not cleared", 0, period.getPlannedActivities().size());
    }

    public void testDeletePlannedActivity() throws Exception {
        plannedActivityDao.delete(plannedActivity);

        replayMocks();
        service.delete(epoch);
        verifyMocks();
    }

    private void expectDeleteEpochs(Iterable<Epoch> epochs) {
        for (Epoch e : epochs) {
            expectDeleteSegments(e.getStudySegments());
            epochDao.delete(e);
        }
    }

    private void expectDeleteSegments(Iterable<StudySegment> studySegments) {
        for (StudySegment studySegment : studySegments) {
            expectDeletePeriods(studySegment.getPeriods());
            studySegmentDao.delete(studySegment);
        }
    }

    private void expectDeletePeriods(Iterable<Period> periods) {
        for (Period p : periods) {
            expectDeletePlannedActivities(p.getPlannedActivities());
            periodDao.delete(p);
        }
    }

    private void expectDeletePlannedActivities(Iterable<PlannedActivity> plannedActivities) {
        for (PlannedActivity pa : plannedActivities) {
            plannedActivityDao.delete(pa);
        }
    }
}
