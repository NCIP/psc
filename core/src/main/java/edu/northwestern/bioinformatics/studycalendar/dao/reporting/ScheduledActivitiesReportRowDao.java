/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDao extends ReportDao<ScheduledActivitiesReportFilters, ScheduledActivitiesReportRow> {
    @Override
    public Class<ScheduledActivitiesReportRow> domainClass() {
        return ScheduledActivitiesReportRow.class;
    }
}
