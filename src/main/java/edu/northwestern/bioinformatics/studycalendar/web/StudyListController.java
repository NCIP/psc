package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;

import java.util.*;

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

        List<DevelopmentTemplate> inDevelopmentTemplates = templateService.getInDevelopmentTemplates(studies, user);
        List<ReleasedTemplate> releasedTemplates = templateService.getReleasedTemplates(studies, user);
        List<ReleasedTemplate> pendingTemplates = templateService.getPendingTemplates(studies, user);
        List<ReleasedTemplate> releasedAndAssignedTemplates = templateService.getReleasedAndAssignedTemplates(studies, user);

        log.debug("{} released templates visible to {}", releasedTemplates.size(), userName);
        log.debug("{} studies open for editing by {}", inDevelopmentTemplates.size(), userName);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("pendingTemplates", pendingTemplates);
        model.put("releasedAndAssignedTemplate", releasedAndAssignedTemplates);
        model.put("releasedAndAssignedTemplatesSize", releasedAndAssignedTemplates.size());

        model.put("releasedTemplates", releasedTemplates);
        model.put("inDevelopmentTemplates", inDevelopmentTemplates);

        return new ModelAndView("studyList", model);
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

        public Study getStudy() {
            return study;
        }


//    private List<SiteCoordinatorController.Notification> createPendingApprovalNotifications(Collection<Site> sites) {
//        List<SiteCoordinatorController.Notification> notes = new ArrayList<SiteCoordinatorController.Notification>();
//        for (Site site : sites) {
//            for (StudySite studySite : site.getStudySites()) {
//                for (Amendment amendment : studySite.getUnapprovedAmendments()) {
//                    notes.add(new SiteCoordinatorController.Notification(studySite, amendment));
//                }
//            }
//        }
//        return notes;
//    }
        

    }
}
