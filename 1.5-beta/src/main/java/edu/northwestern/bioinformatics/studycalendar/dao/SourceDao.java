package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;

import java.util.List;

public class SourceDao extends StudyCalendarMutableDomainObjectDao<Source> {
    public Class<Source> domainClass() {
        return Source.class;
    }

    public List<Source> getAll() throws Exception {
        return getHibernateTemplate().find("from Source order by name");
    }

    public Source getByName(String name) {
        List<Source> sources = getHibernateTemplate().find("from Source where name = ?", name);
        if (!sources.isEmpty()) {
            return sources.get(0);
        }
        return null; 
    }
}
