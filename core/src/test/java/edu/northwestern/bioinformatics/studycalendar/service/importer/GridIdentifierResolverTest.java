package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

import static org.easymock.EasyMock.expect;



/**
 * @author Jalpa Patel
 */
public class GridIdentifierResolverTest extends StudyCalendarTestCase {
    private DaoFinder daoFinder;
    private GridIdentifierResolver gridIdentifierResolver;
    private PlannedCalendarDao plannedCalendarDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);

        daoFinder = registerMockFor(DaoFinder.class);
        expect(daoFinder.findDao(PlannedCalendar.class)).andStubReturn(plannedCalendarDao);

        gridIdentifierResolver =  new GridIdentifierResolver();
        gridIdentifierResolver.setDaoFinder(daoFinder);
    }

    public void testGridIdForExistingNode() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("CAL-GRID");
        expect(plannedCalendarDao.getByGridId("CAL-GRID")).andReturn(calendar);
        replayMocks();
        assertTrue(gridIdentifierResolver.resolveGridId(calendar.getClass(), calendar.getGridId()));
        verifyMocks();
    }

    public void testGridIdForNewNode() throws Exception {
        String expectedGridId = "CAL-GRID2";
        expect(plannedCalendarDao.getByGridId(expectedGridId)).andReturn(null);
        replayMocks();
        assertFalse(gridIdentifierResolver.resolveGridId(PlannedCalendar.class, expectedGridId));
        verifyMocks();
    }
}
