package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
 */
public class StudyDao extends HibernateDaoSupport {
    public Study getById(int id) {
        return (Study) getHibernateTemplate().get(Study.class, id);
    }
}
