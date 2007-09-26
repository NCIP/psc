package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public interface DaoFinder {
    /**
     * Locates the DAO for the given class.  If there's no matching DAO, throw an exception.
     */
    <T extends DomainObject> DomainObjectDao<?> findDao(Class<T> klass);
}
