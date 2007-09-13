package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class EditPeriodController extends AbstractPeriodController<EditPeriodCommand> {
    private PeriodDao periodDao;

    public EditPeriodController() {
        super(EditPeriodCommand.class);
        setFormView("editPeriod");
        setCrumb(new Crumb());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "period");
        return new EditPeriodCommand(periodDao.getById(periodId), periodDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        // prevent spring from trying to bind "period" itself (manually bound in #formBackingObject)
        binder.setAllowedFields(new String[] { "period.*" });
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = super.referenceData(request, command, errors);
        // include period in refdata for breadcrumbs
        getControllerTools().addHierarchyToModel(((EditPeriodCommand) command).getPeriod(), refdata);
        refdata.put("verb", "edit");
        return refdata;
    }

    private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            return new StringBuilder("Edit ").append(context.getPeriod().getDisplayName())
                .toString();
        }
    }

    ////// CONFIGURATION

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
