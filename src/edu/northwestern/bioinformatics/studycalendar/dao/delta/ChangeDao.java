package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;

import java.util.List;

@Transactional(readOnly = true)
public class ChangeDao extends StudyCalendarMutableDomainObjectDao<Change> {
    @Override
    public Class<Change> domainClass() {
        return Change.class;
    }

    public List<Change> getAll() {
        return getHibernateTemplate().find("from Change");
    }

    public Change getById(int id) {
        List<Change> results = getHibernateTemplate().find("from Change a where a.id= ?", id);
        return results.get(0);
    }

//    public Change getByNewValue(String newValue) {
//        List<Change> results = getHibernateTemplate().find("from Change a where a.newValue= ?", newValue);
//        return results.get(0);
//    }
}