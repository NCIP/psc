package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;
import java.sql.SQLException;

@Transactional(readOnly = true)
public class SubjectDao extends StudyCalendarMutableDomainObjectDao<Subject> implements DeletableDomainObjectDao<Subject> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Class<Subject> domainClass() {
        return Subject.class;
    }

    /**
     * Finds all the subjects
     *
     * @return      a list of all the subjects
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<Subject> getAll() {
        return getHibernateTemplate().find("from Subject p order by p.lastName, p.firstName");
    }

    /**
     * Finds the subject assignment for the given subject, study, and site
     *
     * @param  subject the subject to search with for the assignment
     * @param  study the study to search with for the assignment
     * @param  site the site to search with for the assignment
     * @return      the subject assignment for the given subject, study, and site
     */
    public StudySubjectAssignment getAssignment(final Subject subject, final Study study, final Site site) {
        return (StudySubjectAssignment) CollectionUtils.firstElement(getHibernateTemplate().find(
                "from StudySubjectAssignment a where a.subject = ? and a.studySite.site = ? and a.studySite.study = ?",
                new Object[]{subject, site, study}));
    }

    /**
     * Finds the subject for the given mrn (person id)
     *
     * @param  mrn the mrn (person id) to search for the subject with
     * @return      the subject that correspnds to the given mrn
     */
    @SuppressWarnings("unchecked")
    public Subject findSubjectByPersonId(final String mrn) {
        if (mrn != null) {
            List<Subject> results = getHibernateTemplate().find("from Subject s left join fetch s.assignments where s.personId= ?", mrn);
            if (!results.isEmpty()) {
                Subject subject = results.get(0);
                return subject;
            }
        }
        return null;
    }


    /**
     * Finds the subject for the given mrn (person id)
     *
     * @param  mrn the mrn (person id) to search for the subject with
     * @return      the subject that correspnds to the given mrn
     */
    @SuppressWarnings("unchecked")
    public Subject findSubjectByGridOrPersonId(final String mrn) {
        if (mrn != null) {
           Subject subject = findSubjectByPersonId(mrn);
           if (subject == null) {
                List<Subject> results = getHibernateTemplate().find("from Subject s left join fetch s.assignments where s.gridId= ?", mrn);
                if (!results.isEmpty()) {
                    return results.get(0);
                }
            }
        }
        return null;
    }

   /**
    * Finds the subjects doing a LIKE search with some search text for subject's firs name, middle name or last name.
    *
    * @param  searchText the text we are searching with
    * @return      a list of subjects found based on the search text
    */
    @SuppressWarnings({ "unchecked" })
    public List<Subject> getSubjectsBySearchText(final String searchText) {
        return (List<Subject>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Subject.class);
                if (searchText != null) {
                    String like = new StringBuilder().append("%").append(searchText.toLowerCase()).append("%").toString();
                    criteria.add(Restrictions.or(Restrictions.or(Restrictions.ilike("firstName", like), Restrictions.ilike("lastName", like)), Restrictions.ilike("personId", like)));
                }
                return criteria.list();
            }
        });
    }

    /**
     * Finds all the subjects for the given first name, last name, and birth date.
     *
     * @param  firstName the first name to search for the subject with
     * @param  lastName the lastName to search for the subject with
     * @param  dateOfBirth the birth date to search for the subject with
     * @return      finds the subject for the given first name, last name and date of birth
     */
    @SuppressWarnings("unchecked")
    public List<Subject> findSubjectByFirstNameLastNameAndDoB(final String firstName, final String lastName, Date dateOfBirth) {
        List<Subject> results = getHibernateTemplate().find("from Subject s left join fetch s.assignments where s.firstName= ? and s.lastName= ? and s.dateOfBirth= ?", new Object[] {firstName, lastName, dateOfBirth});
        if (!results.isEmpty()) {
            return results;
        }

        return null;
    }

    @Transactional(readOnly = false)
    public void delete(Subject subject) {
        getHibernateTemplate().delete(subject);
    }

    public void deleteAll(List<Subject> t) {
        getHibernateTemplate().delete(t); 
    }
}
