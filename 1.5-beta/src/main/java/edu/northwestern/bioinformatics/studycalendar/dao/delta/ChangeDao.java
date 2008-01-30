package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;

import java.util.List;

public class ChangeDao extends StudyCalendarMutableDomainObjectDao<Change> {
    @Override
    public Class<Change> domainClass() {
        return Change.class;
    }

    public List<Change> getAll() {
        return getHibernateTemplate().find("from Change");
    }
}
