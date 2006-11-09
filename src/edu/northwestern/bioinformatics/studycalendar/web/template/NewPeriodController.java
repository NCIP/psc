package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class NewPeriodController extends PscSimpleFormController {
    private ArmDao armDao;

    public NewPeriodController() {
        setCommandClass(NewPeriodCommand.class);
        setFormView("editPeriod");
        setCrumb(new DefaultCrumb("New Period"));
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("arm", armDao.getById(ServletRequestUtils.getIntParameter(request, "arm")));
        data.put("durationUnits", Duration.Unit.values());
        return data;
    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        return onSubmit((NewPeriodCommand) command);
    }

    private ModelAndView onSubmit(NewPeriodCommand command) throws Exception {
        Arm arm = doSubmitAction(command);
        Integer studyId = arm.getEpoch().getPlannedCalendar().getStudy().getId();
        return ControllerTools.redirectToCalendarTemplate(studyId, arm.getId());
    }

    private Arm doSubmitAction(NewPeriodCommand command) throws Exception {
        Arm arm = armDao.getById(command.getArmId());
        arm.addPeriod(command);
        armDao.save(arm);
        return arm;
    }

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }
}
