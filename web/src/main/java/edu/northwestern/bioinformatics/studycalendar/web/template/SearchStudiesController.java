package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparatorByLetterCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;

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
 * Date: Jan 14, 2008
 * Time: 1:11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchStudiesController extends PscAbstractCommandController<SearchReleasedTemplatesCommand> {
    private StudyDao studyDao;
    private UserDao userDao;
    private TemplateService templateService;

    public SearchStudiesController() {
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

            List<Study> ownedStudies
                = templateService.filterForVisibility(studies, user.getUserRole(Role.SITE_COORDINATOR));
            log.debug("{} studies visible to {}", ownedStudies.size(), user.getName());

            List<Study> assignableStudies = new ArrayList<Study>();
            for (Study ownedStudy : ownedStudies) {
                if (ownedStudy.isReleased()) {
                    assignableStudies.add(ownedStudy);
                }
            }
            Collections.sort(assignableStudies, new NamedComparatorByLetterCase());
            log.debug("{} released studies visible to {}", assignableStudies.size(), user.getName());

            List<Study> results = searchStudies(assignableStudies, searchText);
            model.put("searchStudies", results);
            return new ModelAndView("template/ajax/searchStudies", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }

    // TODO: remove null check for code if find out code is required (Reconsent doesn't have code)
    private List<Study> searchStudies(List<Study> availableStudies, String searchText) {
        if (searchText.equals(EMPTY)) return EMPTY_LIST;

        String searchTextLower = searchText.toLowerCase();

        List<Study> results = new ArrayList<Study>();
        for (Study study : availableStudies) {
            String studyName = study.getName().toLowerCase();
            if (studyName.contains(searchTextLower)) {
                results.add(study);
            }
        }
        if (results.size() == 0) {
            return availableStudies;
        }
        return results;
    }

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
