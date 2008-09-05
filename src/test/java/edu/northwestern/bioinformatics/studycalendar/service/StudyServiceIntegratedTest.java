package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyServiceIntegratedTest extends DaoTestCase {
    private StudyService service;
    private StudyDao studyDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (StudyService) getApplicationContext().getBean("studyService");
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    }
    
    private int saveBasicSkeleton() {
        int id;
        Study blank = TemplateSkeletonCreator.BASIC.create(null);
        service.save(blank);

        assertNotNull("Not saved", blank.getId());
        id = blank.getId();
        interruptSession();
        return id;
    }

    public void testSaveBasicSkeleton() throws Exception {
        int id = saveBasicSkeleton();

        Study reloaded = studyDao.getById(id);
        assertEquals("Amendment not saved", 1, reloaded.getDevelopmentAmendment().getDeltas().size());
        List<Change> changes = reloaded.getDevelopmentAmendment().getDeltas().get(0).getChanges();
        // The detail of the epochs are tested in more detail in TemplateSkeletonCreatorTest
        assertEquals("Wrong number of changes", 2, changes.size());
        assertNotNull("First epoch ID not saved with amendment",
            ((Add) changes.get(0)).getChildId());
        assertNotNull("Second epoch ID not saved with amendment",
            ((Add) changes.get(1)).getChildId());
    }

    public void testSaveWithChangedDeltaDoesNotUpdateAmendment() throws Exception {
        int id = saveBasicSkeleton();

        int originalAmendmentVersion;
        {
            Study original = studyDao.getById(id);
            originalAmendmentVersion = original.getDevelopmentAmendment().getVersion();
            log.info("Development amendment under test has id={}", original.getDevelopmentAmendment().getId());
            Delta<?> originalDelta = original.getDevelopmentAmendment().getDeltas().get(0);
            assertEquals("Test setup failure: wrong number of changes in delta", 2,
                originalDelta.getChanges().size());
            originalDelta.addChange(Add.create(Epoch.create("N")));
            service.save(original);
        }

        interruptSession();

        Study reloaded = studyDao.getById(id);
        Delta<?> reloadedDelta = reloaded.getDevelopmentAmendment().getDeltas().get(0);
        assertEquals("New change not present in reloaded study", 3,
            reloadedDelta.getChanges().size());
        assertTrue("New change not present in reloaded study", reloadedDelta.getChanges().get(2) instanceof Add);

        assertEquals("Amendment version changed even though the amendment itself didn't change",
            originalAmendmentVersion, (int) reloaded.getDevelopmentAmendment().getVersion());
    }
}
