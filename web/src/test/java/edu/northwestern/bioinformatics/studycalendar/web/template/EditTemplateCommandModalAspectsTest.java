package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.Map;
import java.util.Collections;

import static org.easymock.classextension.EasyMock.*;

/**
 * This is a separate test because, historically, {@link EditTemplateCommand) was two
 * different classes.  They've been merged, but the tests remain separate to ensure
 * continuing correct functioning.
 *
 * @author Rhett Sutphin
 */
public class EditTemplateCommandModalAspectsTest extends EditCommandTestCase {
    private EditTemplateCommand command;
    private EditTemplateCommand.Mode mockMode;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new TestableCommand();
        study.getPlannedCalendar().addEpoch(Epoch.create("E", "A", "B"));
        mockMode = registerMockFor(EditTemplateCommand.Mode.class);
    }

    public void testSelectStudyMode() throws Exception {
        command.setStudy(study);
        assertEquals("Study", command.getRelativeViewName());
    }

    public void testSelectEpochMode() throws Exception {
        command.setEpoch(study.getPlannedCalendar().getEpochs().get(0));
        assertEquals("Epoch", command.getRelativeViewName());
    }

    public void testSelectStudySegmentMode() throws Exception {
        command.setStudySegment(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0));
        assertEquals("StudySegment", command.getRelativeViewName());
    }

    public void testPerformEditForwarded() throws Exception {
        useMockMode();
        mockMode.performEdit();

        replayMocks();
        command.performEdit();
        verifyMocks();
    }
    
    public void testGetModelMerges() throws Exception {
        useMockMode();
        expect(mockMode.getModel()).andReturn(Collections.singletonMap("zip", (Object) "zap"));
        expect(command.getRelativeViewName()).andReturn(null);
        replayMocks();
        Map<String, Object> model = command.getModel();
        verifyMocks();

        assertContainsPair(model, "zip", "zap");
    }

    private void useMockMode() {
        command = new TestableCommand() {
            @Override protected Mode studyMode() { return mockMode; }
        };
    }

    private class TestableCommand extends EditTemplateCommand {
        public TestableCommand() {
            setDeltaService(Fixtures.getTestingDeltaService());
            setStudy(study);
        }

        @Override protected Mode studyMode() { return new TestMode("Study"); }
        @Override protected Mode epochMode() { return new TestMode("Epoch"); }
        @Override protected Mode studySegmentMode()   { return new TestMode("StudySegment");   }
    }

    private static class TestMode extends EditTemplateCommand.Mode {
        private String name;

        public TestMode(String name) {
            this.name = name;
        }

        @Override
        public String getRelativeViewName() {
            return name;
        }

        @Override
        public Map<String, Object> getModel() {
            throw new UnsupportedOperationException("getModel not implemented");
        }

        @Override
        public void performEdit() {
            throw new UnsupportedOperationException("performEdit not implemented");
        }
    }
}
