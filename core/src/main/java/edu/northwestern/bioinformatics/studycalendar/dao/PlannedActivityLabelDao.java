/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class PlannedActivityLabelDao extends StudyCalendarMutableDomainObjectDao<PlannedActivityLabel> implements DeletableDomainObjectDao<PlannedActivityLabel>{
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public Class<PlannedActivityLabel> domainClass() {
        return PlannedActivityLabel.class;
    }

    /**
    * Finds the paLabel doing a LIKE search with some search text for activity name or activity code.
    *
    * @param  searchText the text we are searching with
    * @return      a list of activities found based on the search text
    */
    @SuppressWarnings({ "unchecked" })
    public List<PlannedActivityLabel> getPALabelsSearchText(final String searchText) {
        return (List<PlannedActivityLabel>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(PlannedActivityLabel.class);
                if (searchText != null) {
                    String like = new StringBuilder().append("%").append(searchText.toLowerCase()).append("%").toString();
                    criteria.add(Restrictions.ilike("label", like));
                }
                return criteria.list();
            }
        });
    }


   /**
    * Returns a list of all the activities currently available.
    *
    * @return      list of all the Activities currently available
    */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<PlannedActivityLabel> getAll() {
        List<PlannedActivityLabel> sortedList = super.getAll();
        Collections.sort(sortedList);
        return sortedList;
    }

    @Transactional(readOnly=false)
    public void delete(PlannedActivityLabel plannedActivityLabel) {
        getHibernateTemplate().delete(plannedActivityLabel);
    }

    public void deleteAll(List<PlannedActivityLabel> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
