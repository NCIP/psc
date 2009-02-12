package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import junit.framework.TestCase;

import java.util.Date;

/**
 * Test for methods implemented in the abstract base class {@link Change}.
 *
 * @author Rhett Sutphin
 */
public class ChangeTest extends TestCase {
    public void testCloneDoesNotIncludeParentDelta() throws Exception {
        SampleChange source = new SampleChange();
        source.setDelta(Delta.createDeltaFor(new Epoch()));

        SampleChange clone = (SampleChange) source.clone();
        assertNull("Clone has stale delta ref", clone.getDelta());
    }

    private static class SampleChange extends Change {
        @Override
        public ChangeAction getAction() {
            throw new UnsupportedOperationException("getAction not implemented");
        }

        @Override
        public boolean isNoop() {
            throw new UnsupportedOperationException("createMergeLogic not implemented");
        }

        @Override
        protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
            throw new UnsupportedOperationException("createMergeLogic not implemented");
        }
    }
}
