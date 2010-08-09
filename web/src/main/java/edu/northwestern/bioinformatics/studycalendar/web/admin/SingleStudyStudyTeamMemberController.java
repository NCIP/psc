package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.JsonObjectEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SingleStudyStudyTeamMemberController
    extends PscAbstractCommandController<SingleStudyStudyTeamMemberCommand>
{
    private PscUserService pscUserService;
    private StudyDao studyDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private ProvisioningSessionFactory psFactory;

    public SingleStudyStudyTeamMemberController() {
        setCommandClass(SingleStudyStudyTeamMemberCommand.class);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Study study = studyDao.getById(studyId);
        PscUser putativeTeamAdmin = applicationSecurityManager.getUser();

        return SingleStudyStudyTeamMemberCommand.create(study,
            pscUserService.getTeamMembersFor(putativeTeamAdmin),
            psFactory, applicationSecurityManager, studyDao,
            putativeTeamAdmin);
    }

    @Override
    protected void initBinder(
        HttpServletRequest request, ServletRequestDataBinder binder
    ) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(JSONObject.class, "roleChanges", new JsonObjectEditor());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected ModelAndView handle(
        SingleStudyStudyTeamMemberCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        if (request.getMethod().equals("POST")) {
            command.apply();
            return new ModelAndView("redirectToTeamAdmin");
        } else {
            Map<String, Object> model = errors.getModel();
            model.put("roles", PscRole.valuesProvisionableByStudyTeamAdministrator());
            return new ModelAndView("admin/studyTeamSingleStudy", model);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setProvisioningSessionFactory(ProvisioningSessionFactory psFactory) {
        this.psFactory = psFactory;
    }
}