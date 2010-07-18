package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.nwu.bioinformatics.commons.CollectionUtils;

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
    @Override
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

    /**
     * Finds Manual Activity Target Source
     *
     * @return the source with the manual_flag set to true
     */
    @SuppressWarnings({ "unchecked" })
    public Source getManualTargetSource() {
        return CollectionUtils.firstElement(
            (List<Source>) getHibernateTemplate().find("from Source where manual_flag = true"));
    }
    
    /**
     * Returns the number of sources available
     *
     * @return      an integer for the number of sources available
     */
    public int getCount() {
        Long count = (Long) getHibernateTemplate().find("select COUNT(s) from Source s").get(0);
        return count.intValue();
    }
}
