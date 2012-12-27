/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * @author Jalpa Patel
 */
public class ActivityPropertyDao extends StudyCalendarMutableDomainObjectDao<ActivityProperty>{
    @Override
    public Class<ActivityProperty> domainClass() {
        return ActivityProperty.class;
    }

    /**
    * Finds the ActivityProperties by Activity id.
    * * @param  activityId the activity id for the ActivityProperty we want to find
    * @return      the ActivityProperty found that corresponds to the activity id parameters
    */
    @SuppressWarnings({ "unchecked" })
    public List<ActivityProperty> getByActivityId(Integer activityId) {
        return (List<ActivityProperty>) getHibernateTemplate().find("from ActivityProperty where activity_id = ?", activityId);
    }

    @SuppressWarnings({ "unchecked" })
    public ActivityProperty getByNamespaceAndName(Integer activityId, String namespace, String name) {
        List<Object> params = new ArrayList<Object>(3);
        params.add(activityId);
        params.add(namespace);
        params.add(name);
           return CollectionUtils.firstElement(
                 (List<ActivityProperty>) getHibernateTemplate().find("from ActivityProperty where activity_id = ? and namespace = ? and name = ?", params.toArray()));
    }
}
