package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class EditPeriodControllerTest extends ControllerTestCase {
    private EditPeriodController controller;
    private EditPeriodCommand command;
    private StudyService studyService;
    private DeltaService deltaService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);

        controller = new EditPeriodController() {
            @Override
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command();
            }
        };
        controller.setControllerTools(controllerTools);
        controller.setTemplateService(templateService);
        controller.setStudyService(studyService);
        controller.setDeltaService(deltaService);
    }

    private EditPeriodCommand command() {
        return command;
    }

    public void testHandlePost() throws Exception {
        Integer expectedId = 14;
        String expectedName = "Ethical";
        Integer expectedQuantity = 4;
        Integer expectedStartDay = -3;
        Duration.Unit expectedUnit = Duration.Unit.week;

        request.setParameter("period", expectedId.toString());
        request.setParameter("period.duration.quantity", expectedQuantity.toString());
        request.setParameter("period.duration.unit", expectedUnit.toString());
        request.setParameter("period.startDay", expectedStartDay.toString());
        request.setParameter("period.name", expectedName);

        Period period = setId(expectedId, new Period());
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setStartDay(8);
        period.setName("Unethical");

        int armId = 45;
        int studyId = 87;
        Study study = setId(studyId, Fixtures.createSingleEpochStudy("S", "E"));
        setId(armId, study
            .getPlannedCalendar().getEpochs().get(0).getArms().get(0)).addPeriod(period);

        command = registerMockFor(EditPeriodCommand.class);
        expect(command.getPeriod()).andReturn(period).anyTimes();
        expect(command.getArm()).andReturn(period.getArm()).anyTimes();
        expect(studyService.saveStudyFor(period.getArm())).andReturn(study);
        command.apply();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToCalendarTemplate", mv.getViewName());
        assertEquals("Study ID missing from model", studyId, mv.getModel().get("study"));
        assertEquals("Arm ID missing from model", armId, mv.getModel().get("arm"));

        assertEquals("Duration quantity not updated", expectedQuantity, period.getDuration().getQuantity());
        assertEquals("Duration unit not updated", expectedUnit, period.getDuration().getUnit());
        assertEquals("Start day not updated", expectedStartDay, period.getStartDay());
        assertEquals("Name not updated", expectedName, period.getName());
    }
}
