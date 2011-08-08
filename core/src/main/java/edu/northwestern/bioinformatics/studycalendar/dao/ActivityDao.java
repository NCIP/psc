package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class ActivityDao extends StudyCalendarMutableDomainObjectDao<Activity> implements DeletableDomainObjectDao<Activity> {
    @Override
    public Class<Activity> domainClass() {
        return Activity.class;
    }

   /**
    * Returns a list of all the activities currently available.
    *
    * @return      list of all the Activities currently available
    */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<Activity> getAll() {
        List<Activity> sortedList = super.getAll();
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
    * Finds the activity by activity name.
    *
    * @param  name the name of the activity we want to find
    * @return      the activity found that corresponds to the name parameter
    */
    @SuppressWarnings({ "unchecked" })
    public Activity getByName(String name) {
        return CollectionUtils.firstElement(
            (List<Activity>) getHibernateTemplate().find("from Activity where name = ?", name));
    }

    /**
    * Finds the activity by activity name and source name.
    *
    * @param  name the name of the activity we want to find
    * @param  sourceName the source name for the activity we want to find
    * @return      the activity found that corresponds to the activity name and source name parameters
    */
    @SuppressWarnings({ "unchecked" })
    public Activity getByNameAndSourceName(String name, String sourceName) {
        return CollectionUtils.firstElement(
            (List<Activity>) getHibernateTemplate().find(
                "from Activity a where name = ? and a.source.name = ?", new String[] { name, sourceName }));
    }

    /**
    * Finds the activity by activity code and source name.
    *
    * @param  code the code of the activity we want to find
    * @param  sourceName the source name for the activity we want to find
    * @return      the activity found that corresponds to the activity code and source name parameters
    */
    @SuppressWarnings({ "unchecked" })
    public Activity getByCodeAndSourceName(String code, String sourceName) {
        return CollectionUtils.firstElement(
            (List<Activity>) getHibernateTemplate().find(
                "from Activity a where code = ? and a.source.name = ?", new String[] { code, sourceName }));
    }

    public Activity getByUniqueKey(String key) {
        Map<String, String> parts = Activity.splitPropertyChangeKey(key);
        return getByCodeAndSourceName(parts.get("code"), parts.get("source"));
    }

    /**
    * Finds the activities by source id.
    *
    * @param  sourceId the source id for the activity we want to find
    * @return      the activity found that corresponds to the source id parameters
    */
    @SuppressWarnings({ "unchecked" })
    public List<Activity> getBySourceId(Integer sourceId) {
        return (List<Activity>) getHibernateTemplate().find("from Activity where source_id = ?", sourceId);
    }

    public List<Activity> getActivitiesBySearchText(String searchText) {
        return getActivitiesBySearchText(searchText, null, null);
    }

    /**
    * Finds the activity doing a LIKE search with some search text for activity name or activity code.
    *
    * @param  searchText the text we are searching with
    * @return      a list of activities found based on the search text
    */
    @SuppressWarnings({ "unchecked" })
    public List<Activity> getActivitiesBySearchText(final String searchText, final ActivityType type, final Source source) {
        return (List<Activity>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Activity.class);
                if (searchText != null) {
                    String like = new StringBuilder().append("%").append(searchText.toLowerCase()).append("%").toString();
                    criteria.add(Restrictions.or(Restrictions.ilike("name", like), Restrictions.ilike("code", like)));
                }
                if (type != null) {
                    criteria.add(Restrictions.eq("type", type));
                }
                if (source != null) {
                    criteria.add(Restrictions.eq("source", source));
                }
                return criteria.list();
            }
        });
    }

    /**
    * Deletes an activity
    *
    * @param  activity the activity to delete
    */
    public void delete(Activity activity) {
        getHibernateTemplate().delete(activity);
        //have to flush, otherwise it's failing to add a new activity in the same session.
        getSession().flush();
    }

    public void deleteAll(List<Activity> t) {
        getHibernateTemplate().deleteAll(t);
        //have to flush, otherwise it's failing to add a new activity in the same session.
        getSession().flush();
    }

    @SuppressWarnings({ "unchecked" })
    public List<Activity> getAllWithLimit(int limit) {
        return defaultCriteria().setMaxResults(limit).list();
    }

    @SuppressWarnings({ "unchecked" })
    public Integer getCount() {
        return  (Integer) CollectionUtils.firstElement(
                getSession().createCriteria(Activity.class)
                    .setProjection( Projections.projectionList()
                    .add( Projections.rowCount() ))
                    .list());
    }

    @SuppressWarnings({ "unchecked" })
    public List<Activity> getAllWithLimitAndOffset(int limit, int offset) {
        return defaultCriteria()
                .setMaxResults(limit)
                .setFirstResult(offset)
                .list();
    }

    private Criteria defaultCriteria() {
        return getSession().createCriteria(Activity.class)
                .createCriteria("type").addOrder(Order.asc("name"))
                .addOrder(Order.asc("name"));
    }
}
