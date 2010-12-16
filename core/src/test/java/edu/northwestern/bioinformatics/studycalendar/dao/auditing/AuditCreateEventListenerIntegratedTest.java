package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDaoTest;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPeriod;

/**
 * @author Jalpa Patel
 */
public class AuditCreateEventListenerIntegratedTest extends AuditEventListenerTestCase {
    private PeriodDao periodDao;

    @Override
    protected String getTestDataFileName() {
        return String.format("../testdata/%s.xml",
            PeriodDaoTest.class.getSimpleName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");
    }

    public void testCreateAuditEventAfterPeriodSave() throws Exception {
        Integer id;
        {
            Period period = createPeriod("TestPeriod", 3, Duration.Unit.week, 2, 3);
            periodDao.save(period);
            assertNotNull("not saved", period.getId());
            id = period.getId();
        }

        interruptSession();
        int eventId = getEventIdForObject(id, Period.class.getName(), Operation.CREATE.name());
        assertNotNull("Audit event for CREATE is not created ", eventId);

        List<Map> rows = getAuditEventValuesForEvent(eventId);
        assertEquals("No of rows are different", 7, rows.size());

        Map durationQuantityRow = rows.get(2);
        Map durationUnitRow = rows.get(3);
        Map nameRow = rows.get(4);

        // Testing duration.quantity
        assertEquals("Duration.quantity attribute name doesn't match", "duration.quantity", durationQuantityRow.get(ATTRIBUTE_NAME));
        assertEquals("Duration.quantity previous value doesn't match", null, durationQuantityRow.get(PREVIOUS_VALUE));
        assertEquals("Duration.quantity new value doesn't match", "2", durationQuantityRow.get(NEW_VALUE));

        // Testing duration.Unit
        assertEquals("Duration.Unit attribute name doesn't match", "duration.unit", durationUnitRow.get(ATTRIBUTE_NAME));
        assertEquals("Duration.Unit previous value doesn't match", null, durationUnitRow.get(PREVIOUS_VALUE));
        assertEquals("Duration.Unit new value doesn't match", "week", durationUnitRow.get(NEW_VALUE));

        // Testing name
        assertEquals("Name attribute name doesn't match", "name", nameRow.get(ATTRIBUTE_NAME));
        assertEquals("Name previous value doesn't match", null, nameRow.get(PREVIOUS_VALUE));
        assertEquals("Name new value doesn't match", "TestPeriod", nameRow.get(NEW_VALUE));
    }
}
