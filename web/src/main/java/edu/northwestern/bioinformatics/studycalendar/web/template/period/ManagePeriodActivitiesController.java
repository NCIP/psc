package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
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
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ManagePeriodActivitiesController extends PscAbstractController {
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    private TemplateService templateService;
    private DeltaService deltaService;
    private DaoFinder daoFinder;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;

    public ManagePeriodActivitiesController() {
        setCrumb(new Crumb());
    }

    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        return new ModelAndView("divsManagePeriod", referenceData(request));
    }

    @SuppressWarnings({ "unchecked" })
    protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "period");
        Period period = deltaService.revise(periodDao.getById(periodId));
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

        model.put("activitySources", sourceDao.getAll());
        model.put("activityTypes", activityTypeDao.getAll());
        Amendment amendment = study.getDevelopmentAmendment();
        model.put("amendment", amendment);
        model.put("developmentRevision", amendment);
        model.put("revisionChanges", new RevisionChanges(daoFinder, amendment, study, period));
        getControllerTools().addHierarchyToModel(period, model);

        return model;
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

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            Period p = context.getPeriod();
            if (p.getName() != null) {
                return "Manage " + p.getName();
            } else {
                return "Manage period";
            }
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("period", context.getPeriod().getId().toString());
        }
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}