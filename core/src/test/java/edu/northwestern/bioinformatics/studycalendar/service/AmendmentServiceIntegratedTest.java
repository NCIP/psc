/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PeriodDelta;
import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class AmendmentServiceIntegratedTest extends DaoTestCase {
    private AmendmentService amendmentService;
    private StudyService studyService;
    private DeltaService deltaService;
    private StudyDao studyDao;
    private PeriodDao periodDao;
    private ActivityTypeDao activityTypeDao;

    private Study study;
    private ActivityType activityType;
    private Activity activity;
    private Epoch treatmentEpoch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = (AmendmentService) getApplicationContext().getBean("amendmentService");
        studyService = (StudyService) getApplicationContext().getBean("studyService");
        deltaService = (DeltaService) getApplicationContext().getBean("deltaService");
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
        periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");
        activityTypeDao = (ActivityTypeDao) getApplicationContext().getBean("activityTypeDao");

        study = TemplateSkeletonCreator.BASIC.create(null);
        saveStudyAndReplace();

        activityType = activityTypeDao.getByName("DISEASE_MEASURE");
        activity = Fixtures.createActivity("Foo", "Foo", null, activityType);
        ((ActivityDao) getApplicationContext().getBean("activityDao")).save(activity);
    }

    private void saveStudyAndReplace() {
        studyService.save(study);
        interruptSession();
        study = studyDao.getById(study.getId());
        treatmentEpoch = deltaService.revise(study, study.getDevelopmentAmendment()).getPlannedCalendar().getEpochs().get(0);
    }

    public void testAddPlannedActivityToPeriodWhichIsAlsoNewDoesNotUpdatePeriodVersion() throws Exception {
        amendmentService.updateDevelopmentAmendment(
            treatmentEpoch.getStudySegments().get(0), Add.create(Fixtures.createPeriod(1, 4, 2)));
        saveStudyAndReplace();

        Period target = treatmentEpoch.getStudySegments().get(0).getPeriods().first();
        PlannedActivity newPA = Fixtures.createPlannedActivity(activity, 2);
        amendmentService.addPlannedActivityToDevelopmentAmendmentAndSave(target, newPA);
        saveStudyAndReplace();

        Period reloadedPeriod = treatmentEpoch.getStudySegments().get(0).getPeriods().first();
        assertEquals("Period version changed", target.getVersion(), reloadedPeriod.getVersion());
        assertEquals("Planned activity not added to period", 1, reloadedPeriod.getPlannedActivities().size());
    }

    public void testAddPlannedActivityToPeriodWithExistingDeltaDoesNotUpdatePeriodDeltaVersion() throws Exception {
        amendmentService.updateDevelopmentAmendment(
            treatmentEpoch.getStudySegments().get(0), Add.create(Fixtures.createPeriod(1, 4, 2)));
        saveStudyAndReplace();

        amendmentService.amend(study);
        Amendment dev = Fixtures.createAmendment("DC", DateTools.createDate(2007, Calendar.JANUARY, 3));
        study.setDevelopmentAmendment(dev);
        saveStudyAndReplace();

        Period existing = periodDao.getById(treatmentEpoch.getStudySegments().get(0).getPeriods().first().getId());
        study.getDevelopmentAmendment().addDelta(Delta.createDeltaFor(existing));
        saveStudyAndReplace();

        Period target = treatmentEpoch.getStudySegments().get(0).getPeriods().first();
        PeriodDelta delta = (PeriodDelta) study.getDevelopmentAmendment().getDeltas().get(0);
        PlannedActivity newPA = Fixtures.createPlannedActivity(activity, 2);
        amendmentService.addPlannedActivityToDevelopmentAmendmentAndSave(target, newPA);
        saveStudyAndReplace();

        PeriodDelta reloadedDelta = (PeriodDelta) study.getDevelopmentAmendment().getDeltas().get(0);
        assertEquals("PeriodDelta version changed", delta.getVersion(), reloadedDelta.getVersion());
        assertEquals("Planned activity not added to delta", 1, reloadedDelta.getChanges().size());
        assertEquals("Planned activity not added to delta", Add.class, reloadedDelta.getChanges().get(0).getClass());
        assertNotNull(newPA.getId());
        assertEquals("Planned activity not added to delta", newPA.getId(), ((Add) reloadedDelta.getChanges().get(0)).getChildId());
    }
}
