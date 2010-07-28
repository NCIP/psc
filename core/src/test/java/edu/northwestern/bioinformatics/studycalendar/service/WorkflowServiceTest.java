package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowMessageFactory;

/**
 * @author Rhett Sutphin
 */
public class WorkflowServiceTest extends StudyCalendarTestCase {
    private WorkflowService service;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        service = new WorkflowService();
        service.setApplicationSecurityManager(applicationSecurityManager);
        service.setDeltaService(Fixtures.getTestingDeltaService());
        service.setWorkflowMessageFactory(new WorkflowMessageFactory());

        SecurityContextHolderTestHelper.setUserAndReturnMembership("jo", PscRole.DATA_IMPORTER);
    }

    public void testBuild() throws Exception {
        Study study = Fixtures.createBasicTemplate();
        StudyWorkflowStatus actual = service.build(study);

        assertEquals("Wrong user", "jo", actual.getUser().getUsername());
        assertEquals("Wrong study", study, actual.getStudy());
    }
}
