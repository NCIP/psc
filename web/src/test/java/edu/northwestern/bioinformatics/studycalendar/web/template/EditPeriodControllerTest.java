package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static org.easymock.classextension.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

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

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CALENDAR_TEMPLATE_BUILDER);
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

        int studySegmentId = 45;
        int studyId = 87;
        int amendmentId = 55;
        Study study = setId(studyId, Fixtures.createSingleEpochStudy("S", "E"));
        study.setDevelopmentAmendment(setId(amendmentId, new Amendment("dev")));
        setId(studySegmentId, study
            .getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0)).addPeriod(period);

        command = registerMockFor(EditPeriodCommand.class);
        expect(command.getPeriod()).andReturn(period).anyTimes();
        expect(command.getStudySegment()).andReturn(period.getStudySegment()).anyTimes();
        expect(studyService.saveStudyFor(period.getStudySegment())).andReturn(study);
        expect(command.apply()).andReturn(false);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToCalendarTemplate", mv.getViewName());
        assertEquals("Study ID missing from model", studyId, mv.getModel().get("study"));
        assertEquals("StudySegment ID missing from model", studySegmentId, mv.getModel().get("studySegment"));
        assertEquals("Amendment ID missing from model", amendmentId, mv.getModel().get("amendment"));

        assertEquals("Duration quantity not updated", expectedQuantity, period.getDuration().getQuantity());
        assertEquals("Duration unit not updated", expectedUnit, period.getDuration().getUnit());
        assertEquals("Start day not updated", expectedStartDay, period.getStartDay());
        assertEquals("Name not updated", expectedName, period.getName());
    }
}
