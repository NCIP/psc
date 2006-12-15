package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEventValue;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataReference;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.Operation;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;

import java.util.Calendar;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DataAuditDaoTest extends ContextDaoTestCase<DataAuditDao> {
    public void testGet() throws Exception {
        DataAuditEvent update = getDao().getById(-27);
        assertNotNull("Object not found", update);
        assertEquals("Wrong ID", -27, (int) update.getId());
        assertEquals("Wrong object id", -34, (int) update.getReference().getId());
        assertEquals("Wrong object class", Study.class.getName(), update.getReference().getClassName());
        assertEquals("Wrong operation", Operation.UPDATE, update.getOperation());
        assertEquals("Wrong user", "jo-jo", update.getInfo().getBy());
        assertEquals("Wrong ip", "127.1.2.7", update.getInfo().getIp());
        assertDayOfDate("Wrong time", 2006, Calendar.JULY, 22, update.getInfo().getOn());
        assertTimeOfDate("Wrong time", 18, 54, 0, 0, update.getInfo().getOn());
        assertEquals("Wrong URL", "/studycalendar/update", update.getInfo().getUrl());

        assertEquals("Wrong number of values", 1, update.getValues().size());
        DataAuditEventValue actualValue = update.getValues().get(0);
        assertEquals("Wrong property name", "name", actualValue.getAttributeName());
        assertEquals("Wrong previous value", "[New study]", actualValue.getPreviousValue());
        assertEquals("Wrong current value", "ECOG 1138", actualValue.getCurrentValue());
    }

    public void testGetTrail() throws Exception {
        List<DataAuditEvent> trail = getDao().getAuditTrail(new DataReference(Study.class, -34));
        assertEquals("Wrong number of events in trail", 2, trail.size());
        assertEquals("Wrong first event", -20, (int) trail.get(0).getId());
        assertEquals("Wrong second event", -27, (int) trail.get(1).getId());
    }

    public void testSave() throws Exception {
        Integer savedId;
        DataAuditInfo expectedAuditInfo = (DataAuditInfo) DataAuditInfo.getLocal();
        {
            Epoch epoch = setId(7, createNamedInstance("Treaterment", Epoch.class));
            DataAuditEvent newEvent = new DataAuditEvent(epoch, Operation.CREATE);
            newEvent.setInfo(DataAuditInfo.copy(expectedAuditInfo));
            getDao().save(newEvent);
            savedId = newEvent.getId();
            assertNotNull("No ID available", savedId);
        }

        interruptSession();

        DataAuditEvent reloaded = getDao().getById(savedId);
        assertEquals("Wrong operation", Operation.CREATE, reloaded.getOperation());
        assertEquals("Wrong url", expectedAuditInfo.getUrl(), reloaded.getInfo().getUrl());
        assertEquals("Wrong user", expectedAuditInfo.getBy(), reloaded.getInfo().getUsername());
        assertEquals("Wrong ip", expectedAuditInfo.getIp(), reloaded.getInfo().getIp());
        assertSameDay("Wrong time", expectedAuditInfo.getOn(), reloaded.getInfo().getTime());
        assertEquals("Wrong object id", 7, (int) reloaded.getReference().getId());
        assertEquals("Wrong object class", Epoch.class.getName(), reloaded.getReference().getClassName());
    }
}
