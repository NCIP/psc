package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class AuditIntegratedTest extends DaoTestCase {
	public void testDisabled() {
	}

	private static final DataAuditInfo INFO = new DataAuditInfo("dun", "127.1.2.7", DateUtils.createDate(2004,
			Calendar.NOVEMBER, 2), "/studycalendar/zippo");

	private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");

	private StudySegmentDao studySegmentDao = (StudySegmentDao) getApplicationContext().getBean("studySegmentDao");

	private PeriodDao periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");

	private Study created;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		DataAuditInfo.setLocal(INFO);

		// create initial study
		created = Fixtures.createBasicTemplate();
		// amendments are not germane to this test
		created.setAmendment(null);
		studyDao.save(created);
		interruptSession();
	}

	// public void testCreation() throws Exception {
	// int studyCreateId = assertDataLogged(created, Operation.CREATE);
	// assertAuditValue(studyCreateId, "name", null, "[Unnamed study]");
	// assertDataLogged(created.getPlannedCalendar(), Operation.CREATE);
	// for (Epoch epoch : created.getPlannedCalendar().getEpochs()) {
	// assertDataLogged(epoch, Operation.CREATE);
	// for (StudySegment studySegment : epoch.getStudySegments()) {
	// assertDataLogged(studySegment, Operation.CREATE);
	// }
	// }
	// }
	//
	// public void testSimpleRename() throws Exception {
	// // rename an studySegment
	// StudySegment studySegment2 = created.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(2);
	// {
	// StudySegment reloaded = studySegmentDao.getById(studySegment2.getId());
	// reloaded.setName("Carl");
	// studySegmentDao.save(reloaded);
	// }
	// interruptSession();
	//
	// int studySegment2RenameEventId = assertDataLogged(studySegment2, Operation.UPDATE);
	// assertAuditValue(studySegment2RenameEventId, "name", "C", "Carl");
	// }
	//
	// // * public void testReorderList() throws Exception { // reorder epochs Epoch epoch1 =
	// created.getPlannedCalendar().getEpochs().get(1);
	// // {
	// // * Study reloaded = studyDao.getById(created.getId()); Epoch reloadedE1 = reloaded.getPlannedCalendar().getEpochs().get(1);
	// // * reloaded.getPlannedCalendar().getEpochs().remove(reloadedE1); reloaded.getPlannedCalendar().getEpochs().add(reloadedE1);
	// // * studyDao.save(reloaded); } interruptSession(); dumpResults("SELECT * FROM epochs"); dumpResults("SELECT * FROM audit_events");
	// // * dumpResults("SELECT * FROM audit_event_values"); assertDataLogged(created.getPlannedCalendar(), Operation.UPDATE); }
	//
	// public void testDelete() throws Exception {
	// // delete an studySegment
	// StudySegment studySegment1 = created.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(1);
	// {
	// Study reloaded = studyDao.getById(created.getId());
	// reloaded.getPlannedCalendar().getEpochs().get(1).getStudySegments().remove(1);
	// studyDao.save(reloaded);
	// }
	// interruptSession();
	//
	// int deleteId = assertDataLogged(studySegment1, Operation.DELETE);
	// assertAuditValue(deleteId, "name", "B", null);
	// }

	// public void testUpdateComponentHasSubpropertyValues() throws Exception {
	// // Period#duration is a component
	// StudySegment stiduSegment1 = created.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(1);
	// Period p1 = Fixtures.createPeriod("Neptune", 45, Duration.Unit.week, 7, 3);
	// {
	// StudySegment targetStudySegment = studySegmentDao.getById(studySegment1.getId());
	// targetStudySegment.addPeriod(p1);
	// studySegmentDao.save(targetStudySegment);
	// }
	// interruptSession();
	//
	// assertDataLogged(p1, Operation.CREATE);
	//
	// {
	// Period reloaded = periodDao.getById(p1.getId());
	// assertNotNull(reloaded);
	// reloaded.getDuration().setUnit(Duration.Unit.day);
	// periodDao.save(reloaded);
	// }
	// interruptSession();
	//
	// int updateId = assertDataLogged(p1, Operation.UPDATE);
	// assertAuditValue(updateId, "duration.unit", "week", "day");
	// assertNoAuditValue(updateId, "duration.quantity");
	// }

	public void testUpdateCompositeUserTypeHasSubpropertyValues() throws Exception {
		// ScheduledActivity#currentDate uses a CompositeUserType
		// TODO
	}

	private void assertAuditValue(final int eventId, final String attribute, final String expectedPrev,
			final String expectedCurr) {
		List<Map<String, Object>> values = findAuditValues(eventId, attribute);
		assertEquals("Wrong number of values found for " + attribute + " change", 1, values.size());
		Map<String, Object> value = values.get(0);
		assertEquals("Wrong previous value for " + attribute + " change", expectedPrev, value.get("previous_value"));
		assertEquals("Wrong current value for " + attribute + " change", expectedCurr, value.get("new_value"));
	}

	private void assertNoAuditValue(final int eventId, final String attribute) {
		List<Map<String, Object>> actual = findAuditValues(eventId, attribute);
		assertEquals("Did not expect to find " + attribute + " change: " + actual, 0, actual.size());
	}

	@SuppressWarnings( { "unchecked" })
	private List<Map<String, Object>> findAuditValues(final int eventId, final String attribute) {
		List<Map<String, Object>> values = (List<Map<String, Object>>) getJdbcTemplate().query(
				"SELECT * FROM audit_event_values aev WHERE aev.audit_event_id = ? AND aev.attribute_name = ?",
				new Object[] { eventId, attribute }, new RowMapperResultSetExtractor(new ColumnMapRowMapper(), 1));
		return values;
	}

	@SuppressWarnings( { "unchecked" })
	private int assertDataLogged(final DomainObject changed, final Operation operation) {
		DataAuditInfo info = (DataAuditInfo) DataAuditInfo.getLocal();
		List<Map<String, Object>> events = (List<Map<String, Object>>) getJdbcTemplate()
				.query(
						"SELECT * FROM audit_events ae WHERE ae.operation = ? AND ae.class_name = ? AND ae.object_id = ? AND ae.time = ?",
						new Object[] { operation.toString(), changed.getClass().getName(), changed.getId(),
								info.getOn() }, new RowMapperResultSetExtractor(new ColumnMapRowMapper(), 1));
		assertEquals(operation.name() + " not logged for " + changed, 1, events.size());
		return ((Number) events.get(0).get("id")).intValue();
	}

}
