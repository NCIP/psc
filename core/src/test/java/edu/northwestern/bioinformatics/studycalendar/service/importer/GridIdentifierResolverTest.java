package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import static org.easymock.EasyMock.expect;
import java.util.Arrays;



/**
 * @author Jalpa Patel
 */
public class GridIdentifierResolverTest extends StudyCalendarTestCase {
    private DaoFinder daoFinder;
    private GridIdentifierResolver gridIdentifierResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        daoFinder = registerMockFor(DaoFinder.class);
        gridIdentifierResolver =  new GridIdentifierResolver();
        gridIdentifierResolver.setDaoFinder(daoFinder);
    }

    public void testGridIdForExistingNode() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("CAL-GRID");
        PlannedCalendarDao plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);
        expect(daoFinder.findDao(PlannedCalendar.class)).andReturn((StudyCalendarMutableDomainObjectDao)plannedCalendarDao);
        expect(plannedCalendarDao.getAll()).andReturn(Arrays.asList(calendar));
        replayMocks();
        Boolean value = gridIdentifierResolver.resolveGridId(calendar.getClass(), calendar.getGridId());
        verifyMocks();
        assertTrue(value);
    }

    public void testGridIdForNewNode() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("CAL-GRID");
        PlannedCalendar calendar1 = new PlannedCalendar();
        calendar1.setGridId("CAL-GRID1");
        PlannedCalendarDao plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);
        expect(daoFinder.findDao(PlannedCalendar.class)).andReturn((StudyCalendarMutableDomainObjectDao)plannedCalendarDao);
        expect(plannedCalendarDao.getAll()).andReturn(Arrays.asList(calendar));
        replayMocks();
        Boolean value = gridIdentifierResolver.resolveGridId(calendar1.getClass(), calendar1.getGridId());
        verifyMocks();
        assertFalse(value);
    }
}
