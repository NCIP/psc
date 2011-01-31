package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang.StringUtils.join;

public class UserActionDao extends StudyCalendarMutableDomainObjectDao<UserAction>{
    private JdbcTemplate jdbcTemplate;
    @Override
    public Class<UserAction> domainClass() {
        return UserAction.class;
    }

    /**
    * Finds the user actions by context.
    *
    * @param  context url we want to get user actions for
    * @return      the user actions found that corresponds to the context parameters
    *              with associated time of earliest audit event time
    */
    @SuppressWarnings({ "unchecked" })
    public List<UserAction> getUserActionsByContext(String context) {
         List<UserAction> userActions = getHibernateTemplate().findByCriteria(criteria().add(Restrictions.eq("context", context)));

         Map<String, Date> userActionTimeMap = getUserActionsTime(context);
         List<UserAction> userActionList = new ArrayList<UserAction>(userActionTimeMap.size());
         for (UserAction ua : userActions) {
             if (userActionTimeMap.containsKey(ua.getGridId())) {
                ua.setTime(userActionTimeMap.get(ua.getGridId()));
                userActionList.add(ua);
             }
         }
         Collections.sort(userActionList);
         return userActionList;
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Date> getUserActionsTime(String context) {
         String[] query =  new String[] {
                 "SELECT MIN(time), user_action_id FROM audit_events INNER JOIN user_actions",
                 "ON (audit_events.user_action_id=user_actions.grid_id) WHERE context=? GROUP BY user_action_id"
         };
         return (Map<String, Date>) jdbcTemplate.query(join(query, ' '), new Object[]{ context },
                                                        new UserActionTimeResultSetExtractor());
    }

    private class UserActionTimeResultSetExtractor implements ResultSetExtractor {
        public Object extractData(ResultSet rs) throws SQLException {
            Map<String, Date> map = new LinkedHashMap<String, Date>();
            while (rs.next()) {
                String gridId = rs.getString("user_action_id");
                Date time = rs.getTimestamp(1);
                map.put(gridId, time);
            }
            return map;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
