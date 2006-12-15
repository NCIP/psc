package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.Operation;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AuditIntegratedTest extends DaoTestCase {
    private static final DataAuditInfo INFO = new DataAuditInfo("dun", "127.1.2.7",
        DateUtils.createDate(2004, Calendar.NOVEMBER, 2), "/studycalendar/zippo");

    private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    private ArmDao armDao = (ArmDao) getApplicationContext().getBean("armDao");

    private Study created;

    protected void setUp() throws Exception {
        super.setUp();

        DataAuditInfo.setLocal(INFO);

        // create initial study
        created = TemplateSkeletonCreator.BASIC.create();
        studyDao.save(created);
        interruptSession();
    }

    public void testCreation() throws Exception {
        assertDataLogged(created, Operation.CREATE);
        assertDataLogged(created.getPlannedCalendar(), Operation.CREATE);
        for (Epoch epoch : created.getPlannedCalendar().getEpochs()) {
            assertDataLogged(epoch, Operation.CREATE);
            for (Arm arm : epoch.getArms()) {
                assertDataLogged(arm, Operation.CREATE);
            }
        }
    }

    public void testSimpleRename() throws Exception {
        // rename an arm
        Arm arm2 = created.getPlannedCalendar().getEpochs().get(1).getArms().get(2);
        {
            Arm reloaded = armDao.getById(arm2.getId());
            reloaded.setName("Carl");
            armDao.save(reloaded);
        }
        interruptSession();

        int arm2RenameEventId = assertDataLogged(arm2, Operation.UPDATE);
        assertAuditValue(arm2RenameEventId, "name", "C", "Carl");
    }
    /*
    public void testReorderList() throws Exception {
        // reorder epochs
        Epoch epoch1 = created.getPlannedCalendar().getEpochs().get(1);
        {
            Study reloaded = studyDao.getById(created.getId());
            Epoch reloadedE1 = reloaded.getPlannedCalendar().getEpochs().get(1);
            reloaded.getPlannedCalendar().getEpochs().remove(reloadedE1);
            reloaded.getPlannedCalendar().getEpochs().add(reloadedE1);
            studyDao.save(reloaded);
        }
        interruptSession();

        dumpResults("SELECT * FROM epochs");
        dumpResults("SELECT * FROM audit_events");
        dumpResults("SELECT * FROM audit_event_values");
        assertDataLogged(created.getPlannedCalendar(), Operation.UPDATE);
    }
    */
    public void testDelete() throws Exception {
        // delete an arm
        Arm arm1 = created.getPlannedCalendar().getEpochs().get(1).getArms().get(1);
        {
            Study reloaded = studyDao.getById(created.getId());
            reloaded.getPlannedCalendar().getEpochs().get(1).getArms().remove(1);
            studyDao.save(reloaded);
        }
        interruptSession();

        assertDataLogged(arm1, Operation.DELETE);
    }

    private void assertAuditValue(
        int eventId, String attribute, String expectedPrev, String expectedCurr
    ) {
        List<Map<String, Object>> values = (List<Map<String, Object>>) getJdbcTemplate().query(
            "SELECT * FROM audit_event_values aev WHERE aev.audit_event_id = ? AND aev.attribute_name = ?",
            new Object[] { eventId, attribute },
            new RowMapperResultSetExtractor(new ColumnMapRowMapper(), 1));
        assertEquals("Wrong number of values found for " + attribute + " change", 1, values.size());
        Map<String, Object> value = values.get(0);
        assertEquals("Wrong previous value for " + attribute + " change", expectedPrev, value.get("previous_value"));
        assertEquals("Wrong current value for " + attribute + " change", expectedCurr, value.get("current_value"));
    }

    private int assertDataLogged(DomainObject changed, Operation operation) {
        DataAuditInfo info = (DataAuditInfo) DataAuditInfo.getLocal();
        List<Map<String, Object>> events = (List<Map<String, Object>>) getJdbcTemplate().query(
            "SELECT * FROM audit_events ae WHERE ae.operation = ? AND ae.object_class = ? AND ae.object_id = ? AND ae.time = ?",
            new Object[] { operation.toString(), changed.getClass().getName(), changed.getId(), info.getOn() },
            new RowMapperResultSetExtractor(new ColumnMapRowMapper(), 1));
        assertEquals(operation.name() + " not logged for " + changed, 1, events.size());
        return ((Number) events.get(0).get("id")).intValue();
    }
}
