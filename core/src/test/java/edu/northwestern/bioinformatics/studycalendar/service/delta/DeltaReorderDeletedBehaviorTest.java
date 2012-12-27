/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StaticDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.SimpleRevision;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import junit.framework.TestCase;

import java.util.Date;

/**
 * A series of higher-level tests which examine the behavior for
 * various indexed change deletions.
 *
 * @author Rhett Sutphin
 */
public class DeltaReorderDeletedBehaviorTest extends TestCase {
    private Epoch epoch;
    private StudySegment[] studySegments;
    private StudySegment anotherStudySegment;

    private Delta<?> delta;
    private Reorder firstReorder;
    private Revision revision;

    private DeltaService deltaService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Study study = setId(0, new Study());
        study.setPlannedCalendar(setId(1, new PlannedCalendar()));

        epoch = setId(2, Epoch.create("Z", "A", "B", "C", "D", "E", "F"));
        study.getPlannedCalendar().addEpoch(epoch);
        studySegments = new StudySegment[epoch.getStudySegments().size()];
        for (int i = 0; i < epoch.getStudySegments().size(); i++) {
            studySegments[i] = setId(10 + i, epoch.getStudySegments().get(i));
        }
        anotherStudySegment = setId(30, createNamedInstance("x", StudySegment.class));

        firstReorder = Reorder.create(studySegments[0], 0, 0); // intended to be overridden via setFirstReorder
        delta = Delta.createDeltaFor(epoch, firstReorder);
        revision = SimpleRevision.create(delta);

        deltaService = new DeltaService();
        deltaService.setTemplateService(new TestingTemplateService());
        MutatorFactory factory = new MutatorFactory();
        factory.setDaoFinder(new StaticDaoFinder(new StudySegmentDao()));
        deltaService.setMutatorFactory(factory);
    }

    private void setFirstReorder(int oldIndex, int newIndex) {
        firstReorder.setChild(studySegments[oldIndex]);
        firstReorder.setOldIndex(oldIndex);
        firstReorder.setNewIndex(newIndex);
    }

    public void testMoveDownAndAddAtOldIndex() throws Exception {
        setFirstReorder(1, 3);
        delta.addChange(Add.create(anotherStudySegment, 1));
        removeFirstAndExpectOrder("AxCDBEF", "AxBCDEF");
    }

    public void testMoveDownAndAddAtIndexInRange() throws Exception {
        setFirstReorder(1, 3);
        delta.addChange(Add.create(anotherStudySegment, 2));
        removeFirstAndExpectOrder("ACxDBEF", "ABCxDEF");
    }

    public void testMoveDownAndAddAtNewIndex() throws Exception {
        setFirstReorder(1, 3);
        delta.addChange(Add.create(anotherStudySegment, 3));
        removeFirstAndExpectOrder("ACDxBEF", "ABCDxEF");
    }

    public void testMoveUpAndAddAtOldIndex() throws Exception {
        setFirstReorder(3, 1);
        delta.addChange(Add.create(anotherStudySegment, 1));
        removeFirstAndExpectOrder("AxDBCEF", "AxBCDEF");
    }

    public void testMoveUpAndAddAtIndexInRange() throws Exception {
        setFirstReorder(3, 1);
        delta.addChange(Add.create(anotherStudySegment, 2));
        removeFirstAndExpectOrder("ADxBCEF", "AxBCDEF");
    }

    public void testMoveUpAndAddAtNewIndex() throws Exception {
        setFirstReorder(3, 1);
        delta.addChange(Add.create(anotherStudySegment, 3));
        removeFirstAndExpectOrder("ADBxCEF", "ABxCDEF");
    }

    ////// REORDER TESTS
    // In theory, there are 50 possible cases to test.
    // In reality, it is not clear what the correct change would be for most of them,
    // so we just punt and make sure the old index continues to point at the same child.

    public void testMoveUpAndThenReorderOld1AboveNew1Inside() throws Exception {
        setFirstReorder(3, 1);
        delta.addChange(Reorder.create(studySegments[0], 0, 2));
        removeFirstAndExpectOrder("DBACEF", "BCADEF");
    }

    public void testMoveUpAndThenReorderOld1EqualsOld0New1Inside() throws Exception {
        setFirstReorder(3, 1);
        delta.addChange(Reorder.create(studySegments[2], 3, 4));
        removeFirstAndExpectOrder("ADBECF", "ABDECF");
    }

    public void testMoveUpAndThenReorderOld1InsideNew1Inside() throws Exception {
        setFirstReorder(5, 1);
        delta.addChange(Reorder.create(studySegments[2], 3, 2));
        removeFirstAndExpectOrder("AFCBDE", "ABCDEF");
    }

    public void testMoveUpAndThenReorderOld1BelowNew1Inside() throws Exception {
        setFirstReorder(5, 3);
        delta.addChange(Reorder.create(studySegments[3], 4, 2));
        removeFirstAndExpectOrder("ABDCFE", "ABDCEF");
    }

    public void testMoveDownAndThenReorderOld1AboveNew1Inside() throws Exception {
        setFirstReorder(1, 3);
        delta.addChange(Reorder.create(studySegments[0], 0, 2));
        removeFirstAndExpectOrder("CDABEF", "BCADEF");
    }

    public void testMoveDownAndThenReorderOld1InsideNew1Inside() throws Exception {
        // ABCDEF
        setFirstReorder(1, 5);
        // ACDEFB
        delta.addChange(Reorder.create(studySegments[4], 3, 2));
        // ACEDFB
        removeFirstAndExpectOrder("ACEDFB", "ABECDF");
    }

    public void testMoveDownAndThenReorderOld1BelowNew1Inside() throws Exception {
        // ABCDEF
        setFirstReorder(3, 5);
        // ABCEFD
        delta.addChange(Reorder.create(studySegments[5], 4, 2));
        // ABFCED
        removeFirstAndExpectOrder("ABFCED", "ABFCDE");
    }

    private void removeFirstAndExpectOrder(String expectedInitialOrder, String expectedOrderAfterRemove) {
        assertEquals("Order not as expected initially", expectedInitialOrder, studySegmentOrder());
        delta.removeChange(firstReorder, new Date());
        assertEquals("Order not as expected after first reorder deleted", expectedOrderAfterRemove, studySegmentOrder());
        for (Change change : delta.getChanges()) {
            if (change instanceof Reorder) {
                Reorder r = (Reorder) change;
                assertEquals("Child's real position != old index",
                    epoch.getChildren().indexOf(r.getChild()), (int) r.getOldIndex());
            }
        }
    }

    private String studySegmentOrder() {
        StringBuilder sb = new StringBuilder();
        Epoch reordered = deltaService.revise(epoch, revision);
        for (StudySegment studySegment : reordered.getStudySegments()) {
            sb.append(studySegment.getName());
        }
        return sb.toString();
    }
}
