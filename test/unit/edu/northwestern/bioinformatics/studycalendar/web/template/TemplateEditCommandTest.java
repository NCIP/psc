package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.Map;
import java.util.Collections;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class TemplateEditCommandTest extends EditCommandTestCase {
    private TemplateEditCommand command;
    private TemplateEditCommand.Mode mockMode;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new TestableCommand();
        study.getPlannedCalendar().addEpoch(Epoch.create("E", "A", "B"));
        mockMode = registerMockFor(TemplateEditCommand.Mode.class);
    }

    public void testSelectStudyMode() throws Exception {
        command.setStudy(study);
        assertEquals("Study", command.getRelativeViewName());
    }

    public void testSelectEpochMode() throws Exception {
        command.setEpoch(study.getPlannedCalendar().getEpochs().get(0));
        assertEquals("Epoch", command.getRelativeViewName());
    }

    public void testSelectArmMode() throws Exception {
        command.setArm(study.getPlannedCalendar().getEpochs().get(0).getArms().get(0));
        assertEquals("Arm", command.getRelativeViewName());
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

        replayMocks();
        Map<String, Object> model = command.getModel();
        verifyMocks();

        assertContainsPair(model, "zip", "zap");
    }

    private void useMockMode() {
        command = new TestableCommand() {
            protected Mode studyMode() { return mockMode; }
        };
    }

    private class TestableCommand extends TemplateEditCommand {
        public TestableCommand() {
            setDeltaService(Fixtures.getTestingDeltaService());
            setStudy(study);
        }

        protected Mode studyMode() { return new TestMode("Study"); }
        protected Mode epochMode() { return new TestMode("Epoch"); }
        protected Mode armMode()   { return new TestMode("Arm");   }
    }

    private static class TestMode implements TemplateEditCommand.Mode {
        private String name;

        public TestMode(String name) {
            this.name = name;
        }

        public String getRelativeViewName() {
            return name;
        }

        public Map<String, Object> getModel() {
            throw new UnsupportedOperationException("getModel not implemented");
        }

        public void performEdit() {
            throw new UnsupportedOperationException("performEdit not implemented");
        }
    }
}
