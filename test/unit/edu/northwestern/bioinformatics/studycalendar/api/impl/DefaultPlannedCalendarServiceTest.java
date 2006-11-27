package edu.northwestern.bioinformatics.studycalendar.api.impl;

import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;

/**
 * @author Rhett Sutphin
 */
public class DefaultPlannedCalendarServiceTest extends StudyCalendarTestCase {
    private DefaultPlannedCalendarService service;

    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;

    private Study parameterStudy;
    private Study loadedStudy;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);

        service = new DefaultPlannedCalendarService();
        service.setStudyDao(studyDao);
        service.setPlannedCalendarDao(plannedCalendarDao);

        parameterStudy = Fixtures.createNamedInstance("S1", Study.class);
        parameterStudy.setBigId("UNIQUE!");
        loadedStudy = Fixtures.createNamedInstance("S1", Study.class);
        loadedStudy.setBigId("UNIQUE!");
        loadedStudy.setPlannedCalendar(new PlannedCalendar());
    }

    public void testRegisterRequiresBigId() throws Exception {
        parameterStudy.setBigId(null);
        assertRegistrationError("no bigId");
    }

    public void testRegisterRequiresName() throws Exception {
        parameterStudy.setName(null);
        assertRegistrationError("no name");
    }

    public void testDefaultDefaultCreatorIsBasic() throws Exception {
        assertSame(TemplateSkeletonCreator.BASIC, service.getDefaultTemplateCreator());
    }

    public void testRegisterCreatesDefaultTemplate() throws Exception {
        Study created = new Study();
        created.setPlannedCalendar(new PlannedCalendar());
        TemplateSkeletonCreator defaultCreator = registerMockFor(TemplateSkeletonCreator.class);
        service.setDefaultTemplateCreator(defaultCreator);

        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        expect(defaultCreator.create()).andReturn(created);
        studyDao.save(created);
        replayMocks();
        PlannedCalendar actual = service.registerStudy(parameterStudy);
        verifyMocks();

        assertSame(created.getPlannedCalendar(), actual);
        assertEquals("name not copied to created study", parameterStudy.getName(), actual.getStudy().getName());
        assertEquals("bigId not copied to created study", parameterStudy.getBigId(), actual.getStudy().getBigId());
    }

    public void testRegisterReturnsExistingIfAlreadyRegistered() throws Exception {
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(loadedStudy);
        plannedCalendarDao.initialize(loadedStudy.getPlannedCalendar());
        replayMocks();

        assertSame(loadedStudy.getPlannedCalendar(), service.registerStudy(parameterStudy));
    }

    public void testNullIfNoStudy() throws Exception {
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        replayMocks();

        assertNull(service.getPlannedCalendar(parameterStudy));
    }

    public void testGetWithoutBigId() throws Exception {
        parameterStudy.setBigId(null);
        try {
            service.getPlannedCalendar(parameterStudy);
        } catch (IllegalArgumentException iae) {
            assertEquals("Cannot locate planned calendar for a study without a bigId", iae.getMessage());
        }
    }

    public void testBasicGet() throws Exception {
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(loadedStudy);
        plannedCalendarDao.initialize(loadedStudy.getPlannedCalendar());
        replayMocks();

        assertSame(loadedStudy.getPlannedCalendar(), service.getPlannedCalendar(parameterStudy));
    }

    private void assertRegistrationError(String submsg) {
        replayMocks();
        try {
            service.registerStudy(parameterStudy);
        } catch (IllegalArgumentException iae) {
            assertEquals("Wrong error message", "Cannot register study: " + submsg, iae.getMessage());
        }
    }
}
