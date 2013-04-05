/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.List;

import static edu.nwu.bioinformatics.commons.CollectionUtils.firstElement;

/**
 * @author Nataliya Shurupova
 */
public class ActivityTypeDao extends StudyCalendarMutableDomainObjectDao<ActivityType> implements DeletableDomainObjectDao<ActivityType> {
    @Override
    public Class<ActivityType> domainClass() {
        return ActivityType.class;
    }

   /**
    * Returns a list of all the activity types currently available.
    *
    * @return      list of all the Activity types currently available
    */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<ActivityType> getAll() {
        return getHibernateTemplate().find("from ActivityType order by name");
    }

    /**
    * Finds the activity type by activity type.
    *
    * @param  name the name of the activity type we want to find
    * @return      the activity type found that corresponds to the type parameter
    */
    @SuppressWarnings({ "unchecked" })
    public ActivityType getByName(String name) {
        List<ActivityType> activityTypes = getHibernateTemplate().find("from ActivityType where name = ?", name);
        if (!activityTypes.isEmpty()) {
            return activityTypes.get(0);
        }
        return null;
    }

    @SuppressWarnings({ "unchecked" })
    public ActivityType getByNameIgnoringCase(String name) {
        DetachedCriteria criteria = DetachedCriteria.forClass(domainClass()).
            add(Restrictions.ilike("name", name, MatchMode.EXACT));
        return (ActivityType) firstElement(getHibernateTemplate().findByCriteria(criteria));
    }

    /**
    * Deletes an activity type
    *
    * @param  activityType the activityType to delete
    */
    public void delete(ActivityType activityType) {
        getHibernateTemplate().delete(activityType);
    }

    public void deleteAll(List<ActivityType> t) {
        getHibernateTemplate().deleteAll(t);
    }
}

