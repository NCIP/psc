package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SITE_COORDINATOR)
public class ApproveAmendmentsController extends PscSimpleFormController implements PscAuthorizedHandler {
    private StudySiteDao studySiteDao;
    private NowFactory nowFactory;
    private AmendmentService amendmentService;

    public ApproveAmendmentsController() {
        super();
        setCommandClass(ApproveAmendmentsCommand.class);
        setFormView("delta/approveAmendments");
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studySiteArray = queryParameters.get("studySite");
        try {
            String studySiteString = studySiteArray[0];
            Integer studySiteId = Integer.parseInt(studySiteString);
            StudySite studySite = studySiteDao.getById(studySiteId);
            Site site = studySite.getSite();
            return ResourceAuthorization.createCollection(site, STUDY_QA_MANAGER);
        } catch (Exception e) {
            log.error("StudySite parameter is invalid " + e);
            return ResourceAuthorization.createCollection(STUDY_QA_MANAGER);
        }
    }
    

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int studySiteId = ServletRequestUtils.getRequiredIntParameter(request, "studySite");
        return new ApproveAmendmentsCommand(studySiteDao.getById(studySiteId), amendmentService, nowFactory);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
    }

    @Override
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        ApproveAmendmentsCommand command = (ApproveAmendmentsCommand) oCommand;
        Map<String, Object> refdata = new HashMap<String, Object>();
        getControllerTools().addToModel(command.getStudySite(), refdata);
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ApproveAmendmentsCommand command = ((ApproveAmendmentsCommand) oCommand);
        command.apply();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("selected", command.getStudySite().getStudy().getId());
        return new ModelAndView("redirectToAssignSubjectCoordinatorByStudy", model);
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

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            return "Approve amendments for " + context.getSite().getName();
        }
    }
}
