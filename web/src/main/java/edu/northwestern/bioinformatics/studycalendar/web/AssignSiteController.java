package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Padmaja Vedula
 */
@AccessControl(roles = Role.STUDY_ADMIN)
public class AssignSiteController extends PscSimpleFormController {
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteService studySiteService;

    public AssignSiteController() {
        setCommandClass(AssignSiteCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("assignSite");
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
        Map<String, List<Site>> userLists = studySiteService.getSiteLists(study);
        refdata.put("study", study);
        refdata.put("assignedSites", userLists.get(StudyCalendarAuthorizationManager.ASSIGNED_PGS));
        refdata.put("availableSites", userLists.get(StudyCalendarAuthorizationManager.AVAILABLE_PGS));
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
}
