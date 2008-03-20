package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

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
    
    public void testSaveBasicSkeleton() throws Exception {
        int id;
        {
            Study blank = TemplateSkeletonCreator.BASIC.create(null);
            service.save(blank);

            assertNotNull("Not saved", blank.getId());
            id = blank.getId();
        }

        interruptSession();

        Study reloaded = studyDao.getById(id);
        assertEquals("Amendment not saved", 1, reloaded.getDevelopmentAmendment().getDeltas().size());
        List<Change> changes = reloaded.getDevelopmentAmendment().getDeltas().get(0).getChanges();
        // The detail of the epochs are tested in more detail in TemplateSkeletonCreatorTest
        assertEquals("Wrong number of changes", 3, changes.size());
        assertNotNull("First epoch ID not saved with amendment",
            ((Add) changes.get(0)).getChildId());
        assertNotNull("Second epoch ID not saved with amendment",
            ((Add) changes.get(1)).getChildId());
        assertNotNull("Third epoch ID not saved with amendment",
            ((Add) changes.get(2)).getChildId());
    }
}
