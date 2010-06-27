package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * @author Saurabh Agrawal
 */
public class SearchTemplatesController extends PscAbstractCommandController<SearchTemplateCommand> {
    private StudyDao studyDao;
    private TemplateService templateService;
    private ApplicationSecurityManager applicationSecurityManager;

    public SearchTemplatesController() {
        setCommandClass(SearchTemplateCommand.class);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
    }

    protected ModelAndView handle(SearchTemplateCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model = new HashMap<String, Object>();
            String searchText = command.getSearchText() != null ? command.getSearchText() : EMPTY;

            List<Study> studies = studyDao.searchStudiesByStudyName(searchText);
            log.debug("{} studies found total", studies.size());
            User user = applicationSecurityManager.getUser().getLegacyUser();

            List<DevelopmentTemplate> results = templateService.getInDevelopmentTemplates(studies, user);

            log.debug("{} in development studies found ", studies.size());

            List<ReleasedTemplate> releasedTemplates = templateService.getReleasedTemplates(studies,user);
            log.debug("{} in released studies found ", studies.size());

            model.put("inDevelopmentTemplates", results);
            model.put("releasedTemplates", releasedTemplates);

            return new ModelAndView("template/ajax/templates", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
