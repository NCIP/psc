package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDaoTest;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

import java.util.List;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class AuditUpdateEventListenerIntegratedTest extends AuditEventListenerTestCase {
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

    public void testAuditEventAfterPeriodUpdate() throws Exception {
        {
            Period loaded = periodDao.getById(-100);
            loaded.getDuration().setUnit(Duration.Unit.day);
            loaded.getDuration().setQuantity(10);

            periodDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-100, Period.class.getName(), Operation.UPDATE.name());
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        List<Map> rows = getAuditEventValuesForEvent(eventId);
        assertEquals("No of rows are different", 3, rows.size());
        Map durationQuantityRow = rows.get(1);
        Map durationUnitRow = rows.get(2);

        // Testing duration.quantity
        assertEquals("Duration.quantity attribute name doesn't match", "duration.quantity", durationQuantityRow.get(ATTRIBUTE_NAME));
        assertEquals("Duration.quantity previous value doesn't match", "6", durationQuantityRow.get(PREVIOUS_VALUE));
        assertEquals("Duration.quantity new value doesn't match", "10", durationQuantityRow.get(NEW_VALUE));

        // Testing duration.Unit
        assertEquals("Duration.Unit attribute name doesn't match", "duration.unit", durationUnitRow .get(ATTRIBUTE_NAME));
        assertEquals("Duration.Unit previous value doesn't match", "week", durationUnitRow .get(PREVIOUS_VALUE));
        assertEquals("Duration.Unit new value doesn't match", "day", durationUnitRow .get(NEW_VALUE));
    }
}
