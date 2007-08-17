package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public interface DaoFinder {
    <T extends DomainObject> DomainObjectDao<T> findDao(Class<T> klass);
}
