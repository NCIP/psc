package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorTest;
import static edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorTest.*;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommandTest extends StudyCalendarTestCase {
    private NewStudyCommand command;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        command = new NewStudyCommand(studyDao);
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
        studyDao.save((Study) notNull());
    }
}
