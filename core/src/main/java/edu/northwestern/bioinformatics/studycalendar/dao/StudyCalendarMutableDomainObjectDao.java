/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.CollectionUtils;
import gov.nih.nci.cabig.ctms.dao.MutableDomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    public List<T> getAll(){
         return getHibernateTemplate().find("from " + domainClass().getName());
    }

    /**
     * Reassociates the given objects with the current hibernate session.
     *
     * @return the input collection for chaining
     */
    public <C extends Collection<T>> C reassociate(C collection) {
        for (T t : collection) {
            getHibernateTemplate().update(t);
        }
        return collection;
    }

    public T getByGridId(T template) {
        return getByGridId(template.getGridId());
    }

    @Transactional(readOnly = false)
    public void save(T t) {
        getHibernateTemplate().saveOrUpdate(t);
    }
}
