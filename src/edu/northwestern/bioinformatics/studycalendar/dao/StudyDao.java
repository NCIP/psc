package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends StudyCalendarDao<Study> {
    public Class<Study> domainClass() {
        return Study.class;
    }

    @Transactional(readOnly = false)
    public void save(Study study) {
        getHibernateTemplate().saveOrUpdate(study);
    }

    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }
}
