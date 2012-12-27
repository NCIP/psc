/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ApplicationPathAware;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Rhett Sutphin
 */
public class ChangeAmendmentController extends PscSimpleFormController implements PscAuthorizedHandler, ApplicationPathAware {
    private AmendmentDao amendmentDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private DeltaService deltaService;
    private ApplicationSecurityManager applicationSecurityManager;
    private UserActionDao userActionDao;
    private String applicationPath;

    public ChangeAmendmentController() {
        setCommandClass(ChangeAmendmentCommand.class);
        setFormView("schedule/changeAmendment");
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studySubjectAssignmentArray = queryParameters.get("assignment");
        try {
            StudySubjectAssignment studySubjectAssignment = interpretUsingIdOrGridId(studySubjectAssignmentArray[0], studySubjectAssignmentDao);
            StudySite studySite = studySubjectAssignment.getStudySite();
            Site site = studySite.getSite();
            return ResourceAuthorization.createCollection(site, STUDY_SUBJECT_CALENDAR_MANAGER);
        } catch (Exception e) {
            return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
        }
    }    

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ChangeAmendmentCommand(
           interpretUsingIdOrGridId(ServletRequestUtils.getRequiredStringParameter(request, "assignment"), studySubjectAssignmentDao),
           deltaService
        );
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request,binder);
        getControllerTools().registerDomainObjectEditor(binder, null, amendmentDao);
    }

    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
        ChangeAmendmentCommand command = (ChangeAmendmentCommand) oCommand;
        Map<String, Object> refdata = new HashMap<String, Object>();
        getControllerTools().addHierarchyToModel(command.getAssignment(), refdata);
        return refdata;
    }

    protected ModelAndView onSubmit(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, java.lang.Object oCommand, org.springframework.validation.BindException errors)  throws Exception {
        ChangeAmendmentCommand command = (ChangeAmendmentCommand) oCommand;
        associateWithUserAction(command);
        command.apply();
        return getControllerTools().redirectToSchedule(command.getAssignment().getId());
    }

    private void associateWithUserAction(ChangeAmendmentCommand command) {
        StudySubjectAssignment assignment =  command.getAssignment();
        StringBuilder sb = new StringBuilder(applicationPath);
        sb.append("/api/v1/subjects/");
        Subject subject = assignment.getSubject();
        sb.append(subject.getGridId()).append("/schedules");

        UserAction userAction = new UserAction();
        userAction.setContext(sb.toString());
        userAction.setActionType("amendment");
        StringBuilder des = new StringBuilder("Amendment ");
        for (Map.Entry<Amendment, Boolean> entry : command.getAmendments().entrySet()) {
            if (entry.getValue()) {
                des.append("[").append(entry.getKey().getDisplayName()).append("]");
            }
        }
        des.append(" applied to ").append(subject.getFullName()).append(" for ").append(assignment.getName());
        userAction.setDescription(des.toString());
        PscUser user = applicationSecurityManager.getUser();
        if (user != null) {
            userAction.setUser(user.getCsmUser());
        }

        userActionDao.save(userAction);
        AuditEvent.setUserAction(userAction);
    }

    ////// CONFIGURATION

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
    }

    @Required
    public void setUserActionDao(UserActionDao userActionDao) {
        this.userActionDao = userActionDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Change amendment");
        }

        public Map<String, String> getParameters(DomainContext context) {
            return createParameters("assignment", context.getStudySubjectAssignment().getId().toString());
        }
    }

    // TODO: defined in class so any controller with requirement of id or gridId can use it.
    @SuppressWarnings({"unchecked"})
    private <T extends GridIdentifiable & DomainObject> T interpretUsingIdOrGridId(
        String param, GridIdentifiableDao<T> dao
    ) throws ServletRequestBindingException {
        DaoBasedEditor editor = new GridIdentifiableDaoBasedEditor(dao);
        try{
            editor.setAsText(param);
        } catch (IllegalArgumentException iae) {
            return null;
        }
        return (T) editor.getValue();
    }
}
