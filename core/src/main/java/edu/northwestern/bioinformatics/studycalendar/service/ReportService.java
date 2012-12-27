/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ReportService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationSecurityManager applicationSecurityManager;
    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private PscUserService pscUserService;
    private AuthorizationManager csmAuthorizationManager;

    public List<ScheduledActivitiesReportRow> searchScheduledActivities(
        ScheduledActivitiesReportFilters filters
    ) {
        filters.setAuthorizedStudySiteIds(
            pscUserService.getVisibleStudySiteIds(
                applicationSecurityManager.getUser(), PscRoleUse.SUBJECT_MANAGEMENT.roles()));

        return resolveResponsibleUsers(scheduledActivitiesReportRowDao.search(filters));
    }

    private List<ScheduledActivitiesReportRow> resolveResponsibleUsers(List<ScheduledActivitiesReportRow> results) {
        Map<Long, User> csmUsers = new HashMap<Long, User>();
        for (ScheduledActivitiesReportRow row : results) {
            if (row.getResponsibleUserCsmUserId() != null) {
                csmUsers.put(row.getResponsibleUserCsmUserId(), null);
            }
        }

        for (Long csmUserId : csmUsers.keySet()) {
            try {
                csmUsers.put(csmUserId, csmAuthorizationManager.getUserById(csmUserId.toString()));
            } catch (CSObjectNotFoundException e) {
                log.warn(
                    "There is at least one subject assignment whose manager_csm_user_id is {}.  That ID can't be resolved to a user record.", 
                    csmUserId);
            }
        }

        for (ScheduledActivitiesReportRow row : results) {
            if (row.getResponsibleUserCsmUserId() != null) {
                row.setResponsibleUser(csmUsers.get(row.getResponsibleUserCsmUserId()));
            }
        }

        return results;
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

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }
}
