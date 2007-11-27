package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SITE_COORDINATOR)
public class ApproveAmendmentsController extends PscSimpleFormController {
    private StudySiteDao studySiteDao;
    private NowFactory nowFactory;

    public ApproveAmendmentsController() {
        super();
        setCommandClass(ApproveAmendmentsCommand.class);
        setFormView("template/approveAmendments");
        setCrumb(new Crumb());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int studySiteId = ServletRequestUtils.getRequiredIntParameter(request, "studySite");
        return new ApproveAmendmentsCommand(studySiteDao.getById(studySiteId), studySiteDao, nowFactory);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
    }

    @Override
    protected void doSubmitAction(Object command) throws Exception {
        ((ApproveAmendmentsCommand) command).apply();
        // TODO: this should update all amendments from the studysite, too, if the amendment is mandatory
    }

    ////// CONFIGURATION

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            return "Approve amendments for " + context.getSite().getName();
        }
    }
}
