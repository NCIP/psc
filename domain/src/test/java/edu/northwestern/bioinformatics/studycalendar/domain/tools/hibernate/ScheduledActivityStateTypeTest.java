/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityStateTypeTest extends DomainTestCase {
    private static final Date DATE = DateTools.createDate(2003, Calendar.APRIL, 6);
    private static final String REASON = "reason";
    private static final String[] COLUMN_NAMES = new String[] { "current_state_mode_id", "current_state_reason", "current_state_date", "current_state_with_time" };
    private static final Boolean WITH_TIME = false;

    private ScheduledActivityStateType type = new ScheduledActivityStateType();
    private ResultSet rs;
    private PreparedStatement st;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        rs = registerMockFor(ResultSet.class);
        st = registerMockFor(PreparedStatement.class);
    }

    public void testDeepCopy() throws Exception {
        ScheduledActivityState state = ScheduledActivityMode.SCHEDULED.
            createStateInstance(DateTools.createDate(2004, Calendar.JANUARY, 4), "Because");
        Object copy = type.deepCopy(state);
        assertNotSame(state, copy);

        assertTrue(copy instanceof ScheduledActivityState);
        ScheduledActivityState copyState = (ScheduledActivityState) copy;
        assertEquals(state.getMode(), copyState.getMode());
        assertEquals(state.getDate(), copyState.getDate());
        assertEquals(state.getReason(), copyState.getReason());
        assertEquals(state.getWithTime(), copyState.getWithTime());
    }

    public void testNullSafeGetScheduled() throws Exception {
        expectGetStateFields(SCHEDULED, true);
        ScheduledActivityState state = doNullSafeGet();
        assertScheduledActivityState(SCHEDULED, REASON, DATE, state, WITH_TIME);
    }

    public void testNullSafeGetOccurred() throws Exception {
        expectGetStateFields(OCCURRED, true);
        ScheduledActivityState state = doNullSafeGet();
        assertScheduledActivityState(OCCURRED, REASON, DATE, state, WITH_TIME);
    }

    public void testNullSafeGetCanceled() throws Exception {
        expectGetStateFields(CANCELED, true);
        ScheduledActivityState state = doNullSafeGet();
        assertScheduledActivityState(CANCELED, REASON, DATE, state, WITH_TIME);
    }

    public void testNullSafeGetConditional() throws Exception {
        expectGetStateFields(CONDITIONAL, true);
        ScheduledActivityState state = doNullSafeGet();
        assertScheduledActivityState(CONDITIONAL, REASON, DATE, state, WITH_TIME);
    }

    public void testNullSafeGetNotAvailable() throws Exception {
        expectGetStateFields(NOT_APPLICABLE, true);
        ScheduledActivityState state = doNullSafeGet();
        assertScheduledActivityState(NOT_APPLICABLE, REASON, DATE, state, WITH_TIME);
    }
    // TODO (requires changes to ControlledVocabularyObjectType)
//    public void testNullSafeGetNull() throws Exception {
//        expect(rs.getInt(COLUMN_NAMES[0])).andReturn(null);
//        assertNull(doNullSafeGet());
//    }

    private ScheduledActivityState doNullSafeGet() throws SQLException {
        replayMocks();
        ScheduledActivityState state = (ScheduledActivityState) type.nullSafeGet(rs, COLUMN_NAMES, null, null);
        verifyMocks();
        return state;
    }

    private void expectGetStateFields(ScheduledActivityMode expectedMode, boolean expectDate) throws SQLException {
        if (expectDate) expect(rs.getTimestamp(COLUMN_NAMES[2])).andReturn(new Timestamp(DATE.getTime()));
        expect(rs.getString(COLUMN_NAMES[1])).andReturn(REASON);
        expect(rs.getInt(COLUMN_NAMES[0])).andReturn(expectedMode.getId());
        expect(rs.getBoolean(COLUMN_NAMES[3])).andReturn(WITH_TIME);
    }

    private void assertScheduledActivityState(ScheduledActivityMode expectedMode, String expectedReason, Date expectedDate,
                                              ScheduledActivityState actual, Boolean expectedWithTime) {
        assertEquals("Wrong type", expectedMode, actual.getMode());
        assertEquals("Wrong reason", expectedReason, actual.getReason());
        if (expectedDate != null) {
            assertEquals("Wrong date", expectedDate, actual.getDate());
        }
        assertEquals("Wrong withTime", expectedWithTime, actual.getWithTime());
    }

    public void testNullSafeSetScheduled() throws Exception {
        expectSetStateFields(SCHEDULED, 4, true, WITH_TIME);
        doNullSafeSet(ScheduledActivityMode.SCHEDULED.createStateInstance(DATE, REASON), 4);
    }

    public void testNullSafeSetOccurred() throws Exception {
        expectSetStateFields(OCCURRED, 2, true, WITH_TIME);
        doNullSafeSet(ScheduledActivityMode.OCCURRED.createStateInstance(DATE, REASON), 2);
    }

    public void testNullSafeSetCanceled() throws Exception {
        expectSetStateFields(CANCELED, 7, true, WITH_TIME);
        doNullSafeSet(ScheduledActivityMode.CANCELED.createStateInstance(DATE, REASON), 7);
    }

    public void testNullSafeSetConditional() throws Exception {
        expectSetStateFields(CONDITIONAL, 5, true, WITH_TIME);
        doNullSafeSet(ScheduledActivityMode.CONDITIONAL.createStateInstance(DATE, REASON), 5);
    }

    public void testNullSafeSetNotAvailable() throws Exception {
        expectSetStateFields(NOT_APPLICABLE, 6, true, WITH_TIME);
        doNullSafeSet(ScheduledActivityMode.NOT_APPLICABLE.createStateInstance(DATE, REASON), 6);
    }

    private void doNullSafeSet(ScheduledActivityState expectedState, int index) throws SQLException {
        replayMocks();
        type.nullSafeSet(st, expectedState, index, null);
        verifyMocks();
    }

    private void expectSetStateFields(ScheduledActivityMode expectedMode, int index, boolean expectDate, boolean withTime) throws SQLException {
        st.setObject(index    , expectedMode.getId(), Types.INTEGER);
        st.setString(index + 1, REASON);
        st.setTimestamp(index + 2, expectDate ? new Timestamp(DATE.getTime()) : null);
        st.setBoolean(index + 3, withTime);
    }
}
