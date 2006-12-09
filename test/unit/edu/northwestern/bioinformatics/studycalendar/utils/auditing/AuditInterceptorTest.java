package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.utils.auditing.AuditInterceptor;

import java.util.List;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class AuditInterceptorTest extends StudyCalendarTestCase {
    private AuditInterceptor interceptor = new AuditInterceptor();
    
    public void testFindDifferences() throws Exception {
        Object[] cur  = new Object[] { "A", 4, 5.6, true };
        Object[] prev = new Object[] { "B", 4, 5.7, true };

        List<Integer> actualDiff = interceptor.findDifferences(cur, prev);
        assertEquals("Wrong number of differences", 2, actualDiff.size());
        assertEquals("Wrong first diff", 0, (int) actualDiff.get(0));
        assertEquals("Wrong second diff", 2, (int) actualDiff.get(1));
    }

    public void testDefaultAuditValueIsToString() throws Exception {
        assertEquals("42", interceptor.auditValue(42));
    }

    public void testAuditValueNullSafe() throws Exception {
        assertEquals(null, interceptor.auditValue(null));
    }

    public void testAuditCollection() throws Exception {
        assertEquals("1, 4, 7", interceptor.auditValue(Arrays.asList(1, 4, 7)));
    }
    
    public void testAuditDomainObjectIsId() throws Exception {
        Epoch domainObj = Fixtures.setId(55, new Epoch());
        assertEquals("55", interceptor.auditValue(domainObj));
    }
    
    public void testAuditDomainObjectWithoutId() throws Exception {
        assertEquals("transient Epoch", interceptor.auditValue(new Epoch()));
    }
}
