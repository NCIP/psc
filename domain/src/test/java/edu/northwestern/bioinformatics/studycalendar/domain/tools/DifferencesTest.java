package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.DeepComparable;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DifferencesTest extends DomainTestCase {
    private Differences diff = new Differences();

    public void testRegisterEqualValuesDoesNothing() throws Exception {
        diff.registerValueDifference("foo", 5, 5);
        assertNoDifferences();
    }

    public void testRegisterTwoNullValuesDoesNothing() throws Exception {
        diff.registerValueDifference("bar", null, null);
        assertNoDifferences();
    }

    public void testRegisterLeftNullRecordsDifference() throws Exception {
        diff.registerValueDifference("bar", null, "A");
        assertDifferences(diff, "bar <null> does not match \"A\"");
    }

    public void testRegisterRightNullRecordsDifference() throws Exception {
        diff.registerValueDifference("bar", "A", null);
        assertDifferences(diff, "bar \"A\" does not match <null>");
    }

    public void testRegisterDifferentNumbersUsesArthimeticNotation() throws Exception {
        diff.registerValueDifference("bar", 5, 7);
        assertDifferences(diff, "bar does not match: 5 != 7");
    }

    public void testRegisterDifferentOtherValuesCreatesMessageToStrings() throws Exception {
        diff.registerValueDifference("bar", true, false);
        assertDifferences(diff, "bar true does not match false");
    }

    public void testRegisterDifferentStringValuesCreatesMessageWithQuotedValues() throws Exception {
        diff.registerValueDifference("bar", "A", "T");
        assertDifferences(diff, "bar \"A\" does not match \"T\"");
    }

    public void testRegisterDifferentDateValuesFormatsDatesPerFormatTools() throws Exception {
        diff.registerValueDifference("baz",
            DateTools.createDate(2011, Calendar.FEBRUARY, 6),
            DateTools.createDate(2001, Calendar.MARCH, 7));
        assertDifferences(diff, "baz 2011-02-06 does not match 2001-03-07");
    }

    public void testRegisterDifferentNaturallyKeyedValuesUsesTheirNaturalKeys() throws Exception {
        diff.registerValueDifference("baz",
            Fixtures.createPopulation("A", "Alpha"),
            Fixtures.createPopulation("B", "Beta"));
        assertDifferences(diff, "baz A does not match B");
    }

    public void testRegisterCollectionDifferenceWithMissing() throws Exception {
        List<String> first = Arrays.asList("A", "B", "C");
        List<String> second = Arrays.asList("A", "C");
        diff.registerValueCollectionDifference("quux", first, second);

        assertDifferences(diff, "missing quux B");
    }

    public void testRegisterCollectionDifferenceWithExtra() throws Exception {
        List<String> first = Arrays.asList("A", "C");
        List<String> second = Arrays.asList("A", "C", "D");
        diff.registerValueCollectionDifference("quux", first, second);

        assertDifferences(diff, "extra quux D");
    }

    public void testRegisterCollectionDifferenceWithMissingAndExtra() throws Exception {
        List<String> first = Arrays.asList("A", "B", "C");
        List<String> second = Arrays.asList("A", "C", "D");
        diff.registerValueCollectionDifference("quux", first, second);

        assertDifferences(diff, "missing quux B", "extra quux D");
    }

    public void testRecurseDifferencesWhenBothNull() throws Exception {
        diff.recurseDifferences("foo", null, (Object) null);
        assertNoDifferences();
    }

    public void testRecurseDifferencesWhenLeftNull() throws Exception {
        diff.recurseDifferences("foo", null, new DeepComparableImpl("8"));
        assertDifferences(diff, "extra foo");
    }

    public void testRecurseDifferencesWhenRightNull() throws Exception {
        diff.recurseDifferences("foo", new DeepComparableImpl("8"), null);
        assertDifferences(diff, "missing foo");
    }

    public void testRecurseDifferencesWhenChildrenSame() throws Exception {
        diff.recurseDifferences("foo", new DeepComparableImpl("8"), new DeepComparableImpl("8"));
        assertFalse("Should have no differences for foo",
            diff.getChildDifferences().containsKey("foo"));
    }

    public void testRecurseDifferencesWhenDifferent() throws Exception {
        diff.recurseDifferences("foo", new DeepComparableImpl("A"), new DeepComparableImpl("B"));
        assertChildDifferences(diff, "foo", "value \"A\" does not match \"B\"");
    }

    public void testRecurseCollectionDifferencesForGridIdentifiableAlignsOnGridId() throws Exception {
        diff.recurseDifferences("zap",
            Arrays.asList(
                new GridIdentifiableDeepComparable("A", "1"),
                new GridIdentifiableDeepComparable("D", "11"),
                new GridIdentifiableDeepComparable("C", "2")),
            Arrays.asList(
                new GridIdentifiableDeepComparable("C", "2"),
                new GridIdentifiableDeepComparable("A", "3"),
                new GridIdentifiableDeepComparable("B", "1")));

        assertDifferences(diff, "missing zap D", "extra zap B");
        assertChildDifferences(diff, "zap A", "value \"1\" does not match \"3\"");
    }

    public void testRecurseCollectionDifferencesForNaturallyKeyedAlignsOnNaturalKey() throws Exception {
        diff.recurseDifferences("zap",
            Arrays.asList(
                new NaturallyKeyedDeepComparable("A", "1"),
                new NaturallyKeyedDeepComparable("D", "11"),
                new NaturallyKeyedDeepComparable("C", "2")),
            Arrays.asList(
                new NaturallyKeyedDeepComparable("C", "2"),
                new NaturallyKeyedDeepComparable("A", "3"),
                new NaturallyKeyedDeepComparable("B", "1")));

        assertDifferences(diff, "missing zap D", "extra zap B");
        assertChildDifferences(diff, "zap A", "value \"1\" does not match \"3\"");
    }

    public void testRecurseCollectionDifferencesFallsThroughIndexOptionsWhenFirstIsNull() throws Exception {
        diff.recurseDifferences("baz",
            Arrays.asList(
                new NaturallyKeyedAndGridIdentifiableDeepComparable(null, "B", "8"),
                new NaturallyKeyedAndGridIdentifiableDeepComparable(null, "Y", "6"),
                new NaturallyKeyedAndGridIdentifiableDeepComparable("K", null, "7"),
                new NaturallyKeyedAndGridIdentifiableDeepComparable("A", "C", "7")
            ), Arrays.asList(
                new NaturallyKeyedAndGridIdentifiableDeepComparable(null, "B", "6"),
                new NaturallyKeyedAndGridIdentifiableDeepComparable(null, "Y", "6"),
                new NaturallyKeyedAndGridIdentifiableDeepComparable("K", null, "9")
            )
        );

        assertDifferences(diff, "missing baz A");
        assertChildDifferences(diff, "baz K", "value \"7\" does not match \"9\"");
        assertChildDifferences(diff, "baz B", "value \"8\" does not match \"6\"");
    }

    public void testTreeStringWithMessagesOnly() throws Exception {
        diff.addMessage("A");
        diff.addMessage("B");
        diff.addMessage("C");
        assertEquals("* A\n* B\n* C", diff.toTreeString());
    }

    public void testTreeStringWithNestedChildrenMessages() throws Exception {
        diff.addMessage("A");
        diff.addMessage("B");

        Differences childOne = new Differences();
        childOne.addMessage("one");
        diff.getChildDifferences().put("C1", childOne);
        Differences grandchild = new Differences();
        grandchild.addMessage("g one");
        grandchild.addMessage("g two");
        childOne.getChildDifferences().put("G1", grandchild);

        Differences childTwo = new Differences();
        childTwo.addMessage("two");
        diff.getChildDifferences().put("C2", childTwo);

        assertEquals("* A\n" +
            "* B\n" +
            "- C1\n" +
            "  * one\n" +
            "  - G1\n" +
            "    * g one\n" +
            "    * g two\n" +
            "- C2\n" +
            "  * two",
            diff.toTreeString()
        );
    }

    ////// HELPERS

    private void assertNoDifferences() {
        assertEquals(Collections.<String>emptyList(), diff.getMessages());
    }

    private abstract static class AbstractSimpleDeepComparable<T extends AbstractSimpleDeepComparable<T>>
        implements DeepComparable<T>
    {
        protected final String value;
        public AbstractSimpleDeepComparable(String value) { this.value = value; }

        public Differences deepEquals(T other) {
            return new Differences().registerValueDifference("value", this.value, other.value);
        }
    }

    private static class DeepComparableImpl
        extends AbstractSimpleDeepComparable<DeepComparableImpl>
    {
        private DeepComparableImpl(String value) { super(value); }
    }

    private static class GridIdentifiableDeepComparable
        extends AbstractSimpleDeepComparable<GridIdentifiableDeepComparable>
        implements GridIdentifiable
    {
        private final String gridId;

        public GridIdentifiableDeepComparable(String gridId, String value) {
            super(value);
            this.gridId = gridId;
        }

        public String getGridId() { return gridId; }
        public void setGridId(String s) { throw new UnsupportedOperationException("setGridId not implemented"); }
        public boolean hasGridId() { return true; }
    }

    private static class NaturallyKeyedDeepComparable
        extends AbstractSimpleDeepComparable<NaturallyKeyedDeepComparable>
        implements NaturallyKeyed
    {
        private final String naturalKey;

        public NaturallyKeyedDeepComparable(String naturalKey, String value) {
            super(value);
            this.naturalKey = naturalKey;
        }

        public String getNaturalKey() { return naturalKey; }
    }

    private static class NaturallyKeyedAndGridIdentifiableDeepComparable
        extends AbstractSimpleDeepComparable<NaturallyKeyedAndGridIdentifiableDeepComparable>
        implements NaturallyKeyed, GridIdentifiable
    {
        private final String gridId, naturalKey;

        private NaturallyKeyedAndGridIdentifiableDeepComparable(String naturalKey, String gridId, String value) {
            super(value);
            this.gridId = gridId;
            this.naturalKey = naturalKey;
        }

        public String getNaturalKey() { return naturalKey; }
        public String getGridId() { return gridId; }
        public void setGridId(String gridId) { throw new UnsupportedOperationException("setGridId not implemented"); }
        public boolean hasGridId() { return getGridId() != null; }
    }
}
