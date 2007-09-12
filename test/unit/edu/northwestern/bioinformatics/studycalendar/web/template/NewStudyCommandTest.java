package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import static edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorTest.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.notNull;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommandTest extends StudyCalendarTestCase {
    private NewStudyCommand command;
    private StudyService studyService;

    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);
        command = new NewStudyCommand(studyService);
    }

    public void testCreateBlank() throws Exception {
        command.setBase(TemplateSkeletonCreator.BLANK);
        expectCreate();
        replayMocks();
        assertBlankStudy(command.create());
        verifyMocks();
    }
    
    public void testCreateBasic() throws Exception {
        command.setBase(TemplateSkeletonCreator.BASIC);
        expectCreate();
        replayMocks();
        assertBasicStudy(command.create());
        verifyMocks();
    }

    public void testDefaultIsBasic() throws Exception {
        command.setBase(null);
        expectCreate();
        replayMocks();
        assertBasicStudy(command.create());
        verifyMocks();
    }

    private void expectCreate() {
        studyService.save((Study) notNull());
    }
}
