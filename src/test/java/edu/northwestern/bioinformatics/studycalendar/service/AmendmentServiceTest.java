package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;
import java.util.Collections;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class AmendmentServiceTest extends StudyCalendarTestCase {
    private AmendmentService service;
    private StudyService studyService;
    private DeltaService mockDeltaService;
    private ScheduledCalendarDao scheduledCalendarDao;

    private Study study;
    private PlannedCalendar calendar;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);

        study = setGridId("STUDY-GRID", setId(300, createBasicTemplate()));
        calendar = setGridId("CAL-GRID", setId(400, study.getPlannedCalendar()));
        Epoch e1 = setGridId("E1-GRID", setId(1, calendar.getEpochs().get(1)));
        Epoch e2 = setGridId("E2-GRID", setId(2, calendar.getEpochs().get(2)));
        StudySegment e1a0 = setGridId("E1A0-GRID",
            setId(10, calendar.getEpochs().get(1).getStudySegments().get(0)));

        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        study.setAmendment(a3);

        a2.addDelta(Delta.createDeltaFor(calendar, Add.create(e2)));
        a3.addDelta(Delta.createDeltaFor(e1, Add.create(e1a0, 0)));

        service = new AmendmentService();
        service.setStudyService(studyService);
        service.setDeltaService(Fixtures.getTestingDeltaService());
        service.setTemplateService(new TestingTemplateService());
        service.setScheduledCalendarDao(scheduledCalendarDao);

        mockDeltaService = registerMockFor(DeltaService.class);
    }

    public void testAmend() throws Exception {
        assertEquals("Wrong number of epochs to start with", 3, calendar.getEpochs().size());
        assertEquals("Wrong number of amendments to start with", 3,
            study.getAmendment().getPreviousAmendmentsCount());

        Amendment inProgress = new Amendment("LTF");
        Epoch newEpoch = setGridId("E-NEW", setId(8, Epoch.create("Long term")));
        inProgress.addDelta(Delta.createDeltaFor(calendar, Add.create(newEpoch)));
        study.setDevelopmentAmendment(inProgress);

        studyService.save(study);
        // temporary:
        expect(scheduledCalendarDao.getAllFor(study)).andReturn(Collections.<ScheduledCalendar>emptyList());

        replayMocks();
        service.amend(study);
        verifyMocks();

        assertEquals("Epoch not added", 4, study.getPlannedCalendar().getEpochs().size());
        assertEquals("Epoch not added in the expected location", 8,
            (int) study.getPlannedCalendar().getEpochs().get(3).getId());
        assertEquals("Development amendment did not become current", inProgress, study.getAmendment());
        assertEquals("Development amendment did not become current", "A3",
            study.getAmendment().getPreviousAmendment().getName());
        assertNull("Development amendment not moved to stack (still present as dev)", study.getDevelopmentAmendment());
        assertEquals("Wrong number of amendments on stack", 4, study.getAmendment().getPreviousAmendmentsCount());
    }

    public void testGetAmendedWhenAtAmendedLevel() throws Exception {
        Study actual = service.getAmendedStudy(study, study.getAmendment());
        assertEquals(study.getAmendment(), actual.getAmendment());
        assertTrue("Amended study is not marked transient", actual.isMemoryOnly());
    }

    public void testGetAmendedWhenAmendmentNotRelevant() throws Exception {
        study.setName("Study E");
        study.setLongTitle("Study E");
        Amendment irrelevant = setGridId("B0-GRID", createAmendments("B0"));
        try {
            service.getAmendedStudy(study, irrelevant);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Amendment B0 (B0-GRID) does not apply to the template for Study E (STUDY-GRID)", e.getMessage());
        }
    }

    public void testGetAmendedOneRevBack() throws Exception {
        assertEquals("Test setup failure", 3, calendar.getEpochs().size());

        replayMocks();
        Study amended
            = service.getAmendedStudy(study, study.getAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);
        assertEquals("Amended calendar reflects incorrect level", "A2",
            amended.getAmendment().getName());

        List<Epoch> actualEpochs = amended.getPlannedCalendar().getEpochs();
        List<StudySegment> actualE1StudySegments = actualEpochs.get(1).getStudySegments();
        assertEquals("StudySegment add not reverted: " + actualE1StudySegments, 2, actualE1StudySegments.size());
        assertEquals("Epoch add incorrectly reverted: " + actualEpochs, 3, actualEpochs.size());
    }

    public void testGetAmendedTwoRevsBack() throws Exception {
        assertEquals("Test setup failure", 3, calendar.getEpochs().size());
        assertEquals("Test setup failure", 3, calendar.getEpochs().get(1).getStudySegments().size());

        replayMocks();
        Study amended
            = service.getAmendedStudy(study, study.getAmendment().getPreviousAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);

        List<StudySegment> actualE1StudySegments = amended.getPlannedCalendar().getEpochs().get(1).getStudySegments();
        assertEquals("StudySegment add in A3 not reverted: " + actualE1StudySegments, 2, actualE1StudySegments.size());
        assertEquals("Epoch add in A2 not reverted: " + amended.getPlannedCalendar().getEpochs(), 2,
            amended.getPlannedCalendar().getEpochs().size());
        assertEquals("Amended calendar reflects incorrect level", "A1",
            amended.getAmendment().getName());
    }

    public void testUpdateDevAmendment() throws Exception {
        service.setDeltaService(mockDeltaService);

        Amendment expectedDevAmendment = new Amendment();
        study.setDevelopmentAmendment(expectedDevAmendment);
        Epoch epoch = calendar.getEpochs().get(1);
        Remove expectedChange = Remove.create(epoch.getStudySegments().get(0));

        mockDeltaService.updateRevision(expectedDevAmendment, epoch, expectedChange);
        replayMocks();
        service.updateDevelopmentAmendment(epoch, expectedChange);
        verifyMocks();
    }
}
