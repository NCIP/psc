package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.CollectionUtils;

import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarGridIdentifiableDao<T extends DomainObject & GridIdentifiable>
    extends StudyCalendarDao<T> implements GridIdentifiableDao<T>
{
    @SuppressWarnings("unchecked")
    public T getByGridId(String gridId) {
        return (T) CollectionUtils.firstElement(
            getHibernateTemplate().find("from " + domainClass().getName() + " where gridId = ?", gridId));
    }

    public T getByGridId(T template) {
        return getByGridId(template.getGridId());
    }
}
