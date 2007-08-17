package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class StaticDaoFinder implements DaoFinder {
    private Map<Class<?>, DomainObjectDao<?>> byClass;

    public StaticDaoFinder(DomainObjectDao<?>... daos) {
        byClass = new HashMap<Class<?>, DomainObjectDao<?>>();
        for (DomainObjectDao<?> dao : daos) {
            byClass.put(dao.domainClass(), dao);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends DomainObject> DomainObjectDao<T> findDao(Class<T> klass) {
        return (DomainObjectDao<T>) byClass.get(klass);
    }
}
