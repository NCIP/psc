package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyDao extends HibernateDaoSupport {
    public Study getById(int id) {
        return (Study) getHibernateTemplate().get(Study.class, id);
    }

    public void save(Study study) {
        getHibernateTemplate().saveOrUpdate(study);
    }

    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }
}
