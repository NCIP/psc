package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.util.List;

/**
 * @author John Dzak
 */
public abstract class ReportDao<R extends DomainObject> extends StudyCalendarDao<R> {
    @SuppressWarnings("unchecked")
    public List<R> search() {
        return getHibernateTemplate().executeFind(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            public Object doInHibernate(Session session) throws HibernateException {
                Criteria criteria = session.createCriteria(domainClass());
                return (List<R>) criteria.list();
            }
        });
    }
}