package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;

import java.util.List;

public class SourceDao extends StudyCalendarMutableDomainObjectDao<Source> {
    public Class<Source> domainClass() {
        return Source.class;
    }

    /**
     * Finds all the activity sources available
     *
     * @return      a list of all the available activity sources
     */
    @SuppressWarnings({ "unchecked" })
    public List<Source> getAll()  {
        return getHibernateTemplate().find("from Source order by name");
    }

    /**
     * Finds all the actiivty sources for the given activity source name
     *
     * @param  name the name of the activity source to search for
     * @return      the source that was found for the given activity source name
     */
    @SuppressWarnings({ "unchecked" })
    public Source getByName(String name) {
        List<Source> sources = getHibernateTemplate().find("from Source where name = ?", name);
        if (!sources.isEmpty()) {
            return sources.get(0);
        }
        return null; 
    }
}
