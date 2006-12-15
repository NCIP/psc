package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class DataAuditInfoTest extends StudyCalendarTestCase {
    public void testCopySuperclass() throws Exception {
        edu.nwu.bioinformatics.commons.DataAuditInfo source
            = new edu.nwu.bioinformatics.commons.DataAuditInfo("joe", "15", new Date());
        DataAuditInfo copy = DataAuditInfo.copy(source);
        assertEquals("Wrong username in copy", source.getBy(), copy.getUsername());
        assertEquals("Wrong ip in copy", source.getIp(), copy.getIp());
        assertEquals("Wrong date in copy", source.getOn(), copy.getTime());
        assertNull("Unexpected URL in copy", copy.getUrl());
    }

    public void testCopySelfclass() throws Exception {
        DataAuditInfo source = new DataAuditInfo(
            "jim", "127", new Date(), "/where/it/is"
        );
        DataAuditInfo copy = DataAuditInfo.copy(source);
        assertEquals("Wrong username in copy", source.getBy(), copy.getUsername());
        assertEquals("Wrong ip in copy", source.getIp(), copy.getIp());
        assertEquals("Wrong date in copy", source.getOn(), copy.getTime());
        assertEquals("Wrong url in copy", source.getUrl(), copy.getUrl());
    }
}
