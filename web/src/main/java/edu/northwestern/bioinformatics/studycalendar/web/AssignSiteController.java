package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Padmaja Vedula
 */
public class AssignSiteController extends PscSimpleFormController implements PscAuthorizedHandler {
    private StudyDao studyDao;
    private SiteDao siteDao;
    private SiteService siteService;
    private StudySiteService studySiteService;

    public AssignSiteController() {
        setCommandClass(AssignSiteCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("assignSite");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studyArray = queryParameters.get("id");
        try {
            String studyString = studyArray[0];
            Integer studyId = Integer.parseInt(studyString);
            Study study = studyDao.getById(studyId);
            return ResourceAuthorization.createTemplateManagementAuthorizations(study, STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
        } catch (Exception e) {
            return ResourceAuthorization.createCollection(STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
        }
    }

    @Override
     protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return new AssignSiteCommand(studyDao);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "assignedSites", siteDao);
        getControllerTools().registerDomainObjectEditor(binder, "availableSites", siteDao);
    }

    @Override
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        List<Site> assigned = studySiteService.refreshAssociatedSites(study);
        refdata.put("study", study);
        refdata.put("assignedSites", assigned);
        refdata.put("availableSites", CollectionUtils.subtract(siteService.getAll(), assigned));
        refdata.put("action", "Assign");
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSiteCommand assignCommand = (AssignSiteCommand) oCommand;
        Study assignedStudy = studyDao.getById(assignCommand.getStudyId());
        if (assignCommand.getAssign()) {
            studySiteService.assignStudyToSites(assignedStudy, assignCommand.getAvailableSites());
        } else {
            studySiteService.removeStudyFromSites(assignedStudy, assignCommand.getAssignedSites());
        }

        return getControllerTools().redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"));
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
