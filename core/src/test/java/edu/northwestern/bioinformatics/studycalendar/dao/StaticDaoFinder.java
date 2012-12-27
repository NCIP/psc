/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.*;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

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
    public <T extends DomainObject> DomainObjectDao<?> findDao(Class<T> klass) {
        DomainObjectDao<T> found = (DomainObjectDao<T>) byClass.get(klass);
        if (found == null) throw new StudyCalendarSystemException("No DAO for %s", klass.getName());
        return found;
    }

    public <T extends ChangeableDao> List<ChangeableDao<?>> findStudyCalendarMutableDomainObjectDaos() {
        Collection<DomainObjectDao<?>> collectionOfDaos = byClass.values();
        List<ChangeableDao<?>> listOfDaos = new ArrayList<ChangeableDao<?>>();
        for(DomainObjectDao dao : collectionOfDaos) {
            listOfDaos.add((ChangeableDao)dao);
        }
        return listOfDaos;
    }
}
