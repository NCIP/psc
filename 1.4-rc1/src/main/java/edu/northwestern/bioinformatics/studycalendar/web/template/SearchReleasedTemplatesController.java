package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.StudyListController;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.*;
import static java.util.Collections.EMPTY_LIST;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Dec 17, 2007
 * Time: 3:54:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchReleasedTemplatesController extends PscAbstractCommandController<SearchReleasedTemplatesCommand> {
    private StudyDao studyDao;
    private UserDao userDao;
    private TemplateService templateService;

    public SearchReleasedTemplatesController() {
        setCommandClass(SearchReleasedTemplatesCommand.class);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
    }

    protected ModelAndView handle(SearchReleasedTemplatesCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model =  new HashMap<String, Object>();
            String searchText = command.getSearchText() != null ? command.getSearchText() : EMPTY;
            List<Study> studies = studyDao.getAll();
            log.debug("{} studies found total", studies.size());
            String userName = ApplicationSecurityManager.getUser();
            User user = userDao.getByName(userName);

            List<StudyListController.ReleasedTemplate> releasedAndAssignedTemplates = templateService.getReleasedAndAssignedTemplates(studies, user);

            List<StudyListController.ReleasedTemplate> results = searchStudies(releasedAndAssignedTemplates, searchText);
            model.put("releasedTemplates", results);

            return new ModelAndView("template/ajax/releasedTemplates", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }

    // TODO: remove null check for code if find out code is required (Reconsent doesn't have code)
    private List<StudyListController.ReleasedTemplate> searchStudies(List<StudyListController.ReleasedTemplate> availableStudies, String searchText) {
        if (searchText.equals(EMPTY)) return EMPTY_LIST;

        String searchTextLower = searchText.toLowerCase();

        List<StudyListController.ReleasedTemplate> results = new ArrayList<StudyListController.ReleasedTemplate>();
        for (StudyListController.ReleasedTemplate study : availableStudies) {
            String studyName = study.getStudy().getName().toLowerCase();
            if (studyName.contains(searchTextLower)) {
                results.add(study);
            }
        }
        if (results.size() == 0) {
            return availableStudies;
        }
        return results;
    }

//    private List<Study> union(List<Study>... lists) {
//        Set<Study> union = new LinkedHashSet<Study>();
//        for (List<Study> list : lists) {
//            union.addAll(list);
//        }
//        return new ArrayList<Study>(union);
//    }


    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }    
}