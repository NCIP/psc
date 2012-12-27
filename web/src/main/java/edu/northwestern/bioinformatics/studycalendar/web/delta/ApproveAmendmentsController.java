/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class ApproveAmendmentsController extends PscSimpleFormController implements PscAuthorizedHandler {
    private StudySiteDao studySiteDao;
    private NowFactory nowFactory;
    private AmendmentService amendmentService;
    private ApplicationSecurityManager applicationSecurityManager;
    private Configuration configuration;

    public ApproveAmendmentsController() {
        super();
        setCommandClass(ApproveAmendmentsCommand.class);
        setFormView("delta/approveAmendments");
        setCrumb(new Crumb());
    }

    @Override
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studySiteArray = queryParameters.get("studySite");
        try {
            String studySiteString = studySiteArray[0];
            Integer studySiteId = Integer.parseInt(studySiteString);
            StudySite studySite = studySiteDao.getById(studySiteId);
            Site site = studySite.getSite();
            return ResourceAuthorization.createCollection(site, STUDY_QA_MANAGER);
        } catch (Exception e) {
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
        UserStudySiteRelationship rel =
            new UserStudySiteRelationship(applicationSecurityManager.getUser(), command.getStudySite(), configuration);
        String viewName = rel.getCanAdministerTeam() ?
            "redirectToTeamAdminByStudy" : "redirectToCalendarTemplate";
        return new ModelAndView(viewName,
            "study", command.getStudySite().getStudy().getId());
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

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            return "Approve amendments for " + context.getSite().getName();
        }
    }
}
