package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * @author Rhett Sutphin
 */
public class StudyListController extends PscAbstractController {
    private StudyDao studyDao;
    private TemplateService templateService;
    private UserDao userDao;

    public StudyListController() {
        setCrumb(new DefaultCrumb("Studies"));
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Study> studies = studyDao.getAll();
        log.debug("{} studies found total", studies.size());
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        List<Study> devableStudies = templateService.filterForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
        devableStudies = union(devableStudies, templateService.filterForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
        log.debug("{} developable studies visible to {}", devableStudies.size(), userName);

        List<Study> subjectAssignableStudies = templateService.filterForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));

        List<Study> visibleStudies = union(
            devableStudies,
            templateService.filterForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
            subjectAssignableStudies,
            templateService.filterForVisibility(studies, user.getUserRole(RESEARCH_ASSOCIATE))
        );

        List<DevelopmentTemplate> inDevelopmentTemplates = new ArrayList<DevelopmentTemplate>();
        for (Study devableStudy : devableStudies) {
            if (devableStudy.isInDevelopment()) {
                inDevelopmentTemplates.add(new DevelopmentTemplate(devableStudy));
            }
        }

        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        for (Study visibleStudy : visibleStudies) {
            if (visibleStudy.isReleased()) {
                releasedTemplates.add(new ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
            }
        }

        log.debug("{} released templates visible to {}", releasedTemplates.size(), userName);
        log.debug("{} studies open for editing by {}", inDevelopmentTemplates.size(), userName);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("releasedTemplates", releasedTemplates);
        model.put("inDevelopmentTemplates", inDevelopmentTemplates);

        return new ModelAndView("studyList", model);
    }

    private List<Study> union(List<Study>... lists) {
        Set<Study> union = new LinkedHashSet<Study>();
        for (List<Study> list : lists) {
            union.addAll(list);
        }
        return new ArrayList<Study>(union);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    ////// INNER CLASSES

    public static class DevelopmentTemplate {
        private Study study;

        public DevelopmentTemplate(Study study) {
            this.study = study;
        }

        public int getId() {
            return study.getId();
        }

        public int getDevelopmentAmendmentId() {
            return study.getDevelopmentAmendment().getId();
        }

        public String getDisplayName() {
            StringBuilder sb = new StringBuilder(study.getName());
            if (study.isInAmendmentDevelopment()) {
                sb.append(" [").append(study.getDevelopmentAmendment().getDisplayName()).append(']');
            }
            return sb.toString();
        }
    }

    public static class ReleasedTemplate {
        private Study study;
        private boolean canAssignSubjects;

        public ReleasedTemplate(Study study, boolean canAssignSubjects) {
            this.study = study;
            this.canAssignSubjects = canAssignSubjects;
        }

        public boolean getCanAssignSubjects() {
            return canAssignSubjects;
        }

        public int getId() {
            return study.getId();
        }

        public String getDisplayName() {
            StringBuilder sb = new StringBuilder(study.getName());
            if (study.isAmended()) {
                sb.append(" [").append(study.getAmendment().getDisplayName()).append(']');
            }
            return sb.toString();
        }
    }
}
