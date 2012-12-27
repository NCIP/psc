/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
//@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ManagePeriodActivitiesController extends PscAbstractController implements PscAuthorizedHandler {
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    private TemplateService templateService;
    private DeltaService deltaService;
    private DaoFinder daoFinder;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private Configuration configuration;

    public ManagePeriodActivitiesController() {
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        Study study;
        try {
            study = templateService.findStudy(loadPeriod(Integer.parseInt(queryParameters.get("period")[0])));
        } catch (RuntimeException e) {
            study = null;
        }
        return ResourceAuthorization.createTemplateManagementAuthorizations(study);
    }

    @Override
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        return new ModelAndView("divsManagePeriod", referenceData(request));
    }

    @SuppressWarnings({ "unchecked" })
    protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "period");
        Period period = loadPeriod(periodId);
        Study study = templateService.findStudy(period);
        StudySegment studySegment = templateService.findParent(period);

        PeriodActivitiesGrid grid
            = new PeriodActivitiesGrid(period,
                studySegment.getCycleLength(), 
                collectActivities(study), activityTypeDao);
        ModelMap model = new ModelMap();
        model.put("grid", grid);
        model.put("period", period);

        Integer selectedActivityId = ServletRequestUtils.getIntParameter(request, "selectedActivity");
        if (selectedActivityId != null) {
            model.put("selectedActivity", activityDao.getById(selectedActivityId));
        }

        model.put("canEdit",
            new UserTemplateRelationship(applicationSecurityManager.getUser(), study, configuration).getCanDevelop());
        
        model.put("activitySources", sourceDao.getAll());
        model.put("activityTypes", activityTypeDao.getAll());
        Amendment amendment = study.getDevelopmentAmendment();
        model.put("amendment", amendment);
        model.put("developmentRevision", amendment);
        model.put("revisionChanges", new RevisionChanges(daoFinder, amendment, study, period));
        getControllerTools().addHierarchyToModel(period, model);

        return model;
    }

    private Period loadPeriod(int periodId) {
        return deltaService.revise(periodDao.getById(periodId));
    }

    private Collection<Activity> collectActivities(Study study) {
        List<Activity> activities = new LinkedList<Activity>();
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            for (StudySegment segment : epoch.getStudySegments()) {
                for (Period period : segment.getPeriods()) {
                    for (PlannedActivity pa : period.getPlannedActivities()) {
                        activities.add(pa.getActivity());
                    }
                }
            }
        }
        return activities;
    }

    ////// CONFIGURATION

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setDeltaService(DeltaService templateService) {
        this.deltaService = templateService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
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
            Period p = context.getPeriod();
            if (p.getName() != null) {
                return "Manage " + p.getName();
            } else {
                return "Manage period";
            }
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            return Collections.singletonMap("period", context.getPeriod().getId().toString());
        }
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}