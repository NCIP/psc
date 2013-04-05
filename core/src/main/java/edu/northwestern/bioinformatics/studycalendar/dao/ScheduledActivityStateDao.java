/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;

import java.util.List;

public class ScheduledActivityStateDao extends StudyCalendarMutableDomainObjectDao<ScheduledActivityState> implements DeletableDomainObjectDao<ScheduledActivityState> {
    @Override public Class<ScheduledActivityState> domainClass() { return ScheduledActivityState.class;}

    public void delete(ScheduledActivityState scheduledActivityState) {
        getHibernateTemplate().delete(scheduledActivityState);
    }

    public void deleteAll(List<ScheduledActivityState> scheduledActivityStates) {
        getHibernateTemplate().deleteAll(scheduledActivityStates);
    }
}