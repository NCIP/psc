package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.dao.MutableDomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface DeletableDomainObjectDao<T extends MutableDomainObject> extends MutableDomainObjectDao<T> {
    void delete(T t);

    void deleteAll(List<T> t);
}
