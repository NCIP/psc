package edu.northwestern.bioinformatics.studycalendar.utils.auditing;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public abstract class AuditInterceptorTest extends StudyCalendarTestCase {
	// private final AuditInterceptor interceptor = new AuditInterceptor();

	// public void testAuditCollection() throws Exception {
	// assertEquals("[1, 4, 7]",
	// interceptor.scalarAuditableValue(Arrays.asList(1, 4, 7)));
	// }
	//
	// public void testAuditDomainObjectIsId() throws Exception {
	// Epoch domainObj = Fixtures.setId(55, new Epoch());
	// assertEquals("55", interceptor.scalarAuditableValue(domainObj));
	// }
	//
	// public void testAuditDomainObjectWithoutId() throws Exception {
	// assertEquals("transient Epoch", interceptor.scalarAuditableValue(new
	// Epoch()));
	// }
	//
	// public void testAuditValueNullSafe() throws Exception {
	// assertEquals(null, interceptor.scalarAuditableValue(null));
	// }
	//
	// public void testDefaultAuditValueIsToString() throws Exception {
	// assertEquals("42", interceptor.scalarAuditableValue(42));
	// }
	//
	// public void testFindDifferences() throws Exception {
	// Object[] cur = new Object[] { "A", 4, 5.6, true };
	// Object[] prev = new Object[] { "B", 4, 5.7, true };
	//
	// List<Integer> actualDiff = interceptor.findDifferences(cur, prev);
	// assertEquals("Wrong number of differences", 2, actualDiff.size());
	// assertEquals("Wrong first diff", 0, (int) actualDiff.get(0));
	// assertEquals("Wrong second diff", 2, (int) actualDiff.get(1));
	// }
}
