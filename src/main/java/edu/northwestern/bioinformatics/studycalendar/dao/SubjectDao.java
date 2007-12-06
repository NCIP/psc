package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

public class SubjectDao extends StudyCalendarMutableDomainObjectDao<Subject> {
    @Override
    public Class<Subject> domainClass() {
        return Subject.class;
    }

    public List<Subject> getAll() {
        return getHibernateTemplate().find("from Subject p order by p.lastName, p.firstName");
    }

    public StudySubjectAssignment getAssignment(final Subject subject, final Study study, final Site site) {
        return (StudySubjectAssignment) CollectionUtils.firstElement(getHibernateTemplate().find(
                "from StudySubjectAssignment a where a.subject = ? and a.studySite.site = ? and a.studySite.study = ?",
                new Object[]{subject, site, study}));
    }

    @SuppressWarnings("unchecked")
    public Subject findSubjectByPersonId(final String mrn) {
        List<Subject> results = getHibernateTemplate().find("from Subject where personId= ?", mrn);
        if (!results.isEmpty()) {
            return results.get(0);
        }
        String message = "No subject exist with the given mrn :" + mrn;
        logger.info(message);

        return null;
    }

    @Transactional(readOnly = false)
    public void delete(Subject subject) {
        getHibernateTemplate().delete(subject);
    }
}
