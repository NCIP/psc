package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.CollectionUtils;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import gov.nih.nci.cabig.ctms.dao.MutableDomainObjectDao;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public abstract class StudyCalendarMutableDomainObjectDao<T extends MutableDomainObject>
    extends StudyCalendarDao<T> implements MutableDomainObjectDao<T>
{
    @SuppressWarnings("unchecked")
    public T getByGridId(String gridId) {
        return (T) CollectionUtils.firstElement(
            getHibernateTemplate().find("from " + domainClass().getName() + " where gridId = ?", gridId));
    }

    public T getByGridId(T template) {
        return getByGridId(template.getGridId());
    }

    @Transactional(readOnly = false)
    public void save(T t) {
        getHibernateTemplate().saveOrUpdate(t);
    }
}
