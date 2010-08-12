package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ReportService {
    private ApplicationSecurityManager applicationSecurityManager;
    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private PscUserService pscUserService;

    public List<ScheduledActivitiesReportRow> searchScheduledActivities(
        ScheduledActivitiesReportFilters filters
    ) {
        filters.setAuthorizedStudySiteIds(
            pscUserService.getVisibleStudySiteIds(
                applicationSecurityManager.getUser(), PscRoleUse.SUBJECT_MANAGEMENT.roles()));

        return scheduledActivitiesReportRowDao.search(filters);
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao) {
        this.scheduledActivitiesReportRowDao = scheduledActivitiesReportRowDao;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }
}
