package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Collections;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class NewPeriodController extends AbstractPeriodController<NewPeriodCommand> {
    private AmendmentService amendmentService;
    private ArmDao armDao;

    public NewPeriodController() {
        super(NewPeriodCommand.class);
        setCrumb(new Crumb());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new NewPeriodCommand(amendmentService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "arm", armDao);
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = super.referenceData(request, command, errors);
        refdata.put("verb", "add");
        getControllerTools().addHierarchyToModel(((PeriodCommand) command).getArm(), refdata);
        return refdata;
    }

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Add period");
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("arm", context.getArm().getId().toString());
        }
    }
}
