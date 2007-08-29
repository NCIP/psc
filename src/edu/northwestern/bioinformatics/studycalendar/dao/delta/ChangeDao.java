package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

import java.util.List;

@Transactional(readOnly = true)
public class ChangeDao extends StudyCalendarDao<Change> {
        private static final Logger log = LoggerFactory.getLogger(Change.class.getName());

    public Class<Change> domainClass() {
        return Change.class;
    }

    public List<Change> getAll() {
        return getHibernateTemplate().find("from Change");
    }

    @Transactional(readOnly = false)
    public void save(Change change) {
        getHibernateTemplate().saveOrUpdate(change);
    }

    public Change getById(int id) {
        List<Change> results = getHibernateTemplate().find("from Change a where a.id= ?", id);
        return results.get(0);
    }
}