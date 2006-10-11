package edu.northwestern.bioinformatics.studycalendar.utils.hibernate;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode.*;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventStateTypeTest extends StudyCalendarTestCase {
    private static final Date DATE = DateUtils.createDate(2003, Calendar.APRIL, 6);
    private static final String REASON = "reason";
    private static final String[] COLUMN_NAMES = new String[] { "current_state_mode_id", "current_state_reason", "current_state_date" };

    private ScheduledEventStateType type = new ScheduledEventStateType();
    private ResultSet rs;
    private PreparedStatement st;

    protected void setUp() throws Exception {
        super.setUp();
        rs = registerMockFor(ResultSet.class);
        st = registerMockFor(PreparedStatement.class);
    }

    public void testDeepCopy() throws Exception {
        Scheduled state = new Scheduled("Because", DateUtils.createDate(2004, Calendar.JANUARY, 4));
        Object copy = type.deepCopy(state);
        assertNotSame(state, copy);

        assertTrue(copy instanceof Scheduled);
        Scheduled copyState = (Scheduled) copy;
        assertEquals(state.getDate(), copyState.getDate());
        assertEquals(state.getReason(), copyState.getReason());
    }

    public void testNullSafeGetScheduled() throws Exception {
        expectGetStateFields(SCHEDULED, true);
        ScheduledEventState state = doNullSafeGet();
        assertScheduledEventState(Scheduled.class, REASON, DATE, state);
    }

    public void testNullSafeGetOccurred() throws Exception {
        expectGetStateFields(OCCURRED, true);
        ScheduledEventState state = doNullSafeGet();
        assertScheduledEventState(Occurred.class, REASON, DATE, state);
    }

    public void testNullSafeGetCanceled() throws Exception {
        expectGetStateFields(CANCELED, false);
        ScheduledEventState state = doNullSafeGet();
        assertScheduledEventState(Canceled.class, REASON, state);
    }

    // TODO (requires changes to ControlledVocabularyObjectType)
//    public void testNullSafeGetNull() throws Exception {
//        expect(rs.getInt(COLUMN_NAMES[0])).andReturn(null);
//        assertNull(doNullSafeGet());
//    }

    private ScheduledEventState doNullSafeGet() throws SQLException {
        replayMocks();
        ScheduledEventState state = (ScheduledEventState) type.nullSafeGet(rs, COLUMN_NAMES, null, null);
        verifyMocks();
        return state;
    }

    private void expectGetStateFields(ScheduledEventMode expectedMode, boolean expectDate) throws SQLException {
        if (expectDate) expect(rs.getDate(COLUMN_NAMES[2])).andReturn(new java.sql.Date(DATE.getTime()));
        expect(rs.getString(COLUMN_NAMES[1])).andReturn(REASON);
        expect(rs.getInt(COLUMN_NAMES[0])).andReturn(expectedMode.getId());
    }

    private void assertScheduledEventState(Class<? extends ScheduledEventState> expectedType, String expectedReason, ScheduledEventState actual) {
        assertScheduledEventState(expectedType, expectedReason, null, actual);
    }

    private void assertScheduledEventState(Class<? extends ScheduledEventState> expectedType, String expectedReason, Date expectedDate, ScheduledEventState actual) {
        assertEquals("Wrong type", expectedType, actual.getClass());
        assertEquals("Wrong reason", expectedReason, actual.getReason());
        if (expectedDate != null) {
            assertEquals("Wrong date", expectedDate, ((DatedScheduledEventState) actual).getDate());
        }
    }

    public void testNullSafeSetScheduled() throws Exception {
        expectSetStateFields(SCHEDULED, 4, true);
        doNullSafeSet(new Scheduled(REASON, DATE), 4);
    }

    public void testNullSafeSetOccurred() throws Exception {
        expectSetStateFields(OCCURRED, 2, true);
        doNullSafeSet(new Occurred(REASON, DATE), 2);
    }

    public void testNullSafeSetCanceled() throws Exception {
        expectSetStateFields(CANCELED, 7, false);
        doNullSafeSet(new Canceled(REASON), 7);
    }

    private void doNullSafeSet(ScheduledEventState expectedState, int index) throws SQLException {
        replayMocks();
        type.nullSafeSet(st, expectedState, index, null);
        verifyMocks();
    }

    private void expectSetStateFields(ScheduledEventMode expectedMode, int index, boolean expectDate) throws SQLException {
        st.setObject(index + 0, expectedMode.getId(), Types.INTEGER);
        st.setString(index + 1, REASON);
        st.setDate(index + 2, expectDate ? new java.sql.Date(DATE.getTime()) : null);
    }
}
