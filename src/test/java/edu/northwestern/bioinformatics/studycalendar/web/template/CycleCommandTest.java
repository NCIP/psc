package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.eq;

/**
 * @author Jalpa Patel
 */
public class CycleCommandTest extends StudyCalendarTestCase {
    private StudySegment studySegment;
    private CycleCommand command;
    private AmendmentService amendmentService;
    private TemplateService templateService;

    public void setUp()  throws Exception  {
        super.setUp();
        int id = 44;
        studySegment = Fixtures.setId(id, Fixtures.createNamedInstance("StudySegment", StudySegment.class));
        studySegment.setCycleLength(10);
        amendmentService = registerMockFor(AmendmentService.class);
        templateService = registerMockFor(TemplateService.class);
        command = new CycleCommand(templateService,amendmentService);
        command.setStudySegment(studySegment);
    }

    public void testApplySetsCycleLength () throws Exception  {
        command.setCycleLength(12);
        command.apply();
        assertEquals("Cycle length not set",(Integer)12, studySegment.getCycleLength());
    }

    public void testApplyCycleLengthChange() throws Exception {
        command.setCycleLength(11);
        amendmentService.updateDevelopmentAmendment(
            same(studySegment), eq(PropertyChange.create("cycleLength",command.getStudySegment().getCycleLength(),11)));
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
