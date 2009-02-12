package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ChildrenChangeTest extends StudyCalendarTestCase {

    public void testCloneDeepClonesChild() throws Exception {
        Hide src = new Hide(Epoch.create("Gary"));
        Hide clone = (Hide) src.clone();
        assertNotSame("Child of clone is same as child of original", clone.getChild(), src.getChild());
    }

    public void testSetMemOnlyRecursiveToChild() throws Exception {
        Hide src = new Hide(Epoch.create("Gary"));
        src.setMemoryOnly(true);
        assertTrue(src.getChild().isMemoryOnly());
    }

    private static class Hide extends ChildrenChange {
        private Hide(Child<?> child) {
            setChild(child);
        }

        @Override
        public ChangeAction getAction() {
            throw new UnsupportedOperationException("getAction not implemented");
        }

        @Override
        public boolean isNoop() {
            throw new UnsupportedOperationException("isNoop not implemented");
        }

        @Override
        protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
            throw new UnsupportedOperationException("createMergeLogic not implemented");
        }
    }
}
