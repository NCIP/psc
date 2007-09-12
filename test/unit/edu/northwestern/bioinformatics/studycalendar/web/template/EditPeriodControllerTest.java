package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.notNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class EditPeriodControllerTest extends ControllerTestCase {
    private EditPeriodController controller;
    private EditPeriodCommand command;
    private PeriodDao periodDao;

    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerDaoMockFor(PeriodDao.class);

        controller = new EditPeriodController() {
            @Override
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command();
            }
        };
        controller.setPeriodDao(periodDao);
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

        Period expected = setId(expectedId, new Period());
        int armId = 45;
        int studyId = 87;
        setId(armId, setId(studyId, Fixtures.createSingleEpochStudy("S", "E")).getPlannedCalendar().getEpochs().get(0).getArms().get(0)).addPeriod(expected);

        periodDao.save((Period) notNull());
        command = new EditPeriodCommand(expected, periodDao);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        // TODO: make these pass again
//        assertEquals("Wrong view", "redirectToCalendarTemplate", mv.getViewName());
//        assertEquals("Study ID missing from model", studyId, mv.getModel().get("study"));
//        assertEquals("Arm ID missing from model", armId, mv.getModel().get("arm"));

        assertEquals("Duration quantity not updated", expectedQuantity, expected.getDuration().getQuantity());
        assertEquals("Duration unit not updated", expectedUnit, expected.getDuration().getUnit());
        assertEquals("Start day not updated", expectedStartDay, expected.getStartDay());
        assertEquals("Name not updated", expectedName, expected.getName());
    }
}
