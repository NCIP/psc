package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

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
        String msg = "No subject exist with the given mrn :" + mrn;
        logger.info(msg);

        return null;
    }


    @Transactional(readOnly = false)
    public void commitInProgressSubject(String mrn) {
        Object subjectId = getSession().createSQLQuery("select s.id from subjects s where s.person_id='" + mrn + "'").uniqueResult();
        if (subjectId == null) {
            String msg = "No subject exist with the given mrn :" + mrn;
            logger.error(msg);
            throw new StudyCalendarSystemException(msg);

        }

        logger.info("For MRN:" + mrn + " found subject:id-" + subjectId.toString());
        //update subjects
        getSession().createSQLQuery("update subjects set load_status = 1 where id = " +
                subjectId.toString()).executeUpdate();

        //update subject assignments
//        getSession().createSQLQuery("update subject_assignments set load_status = 1 where subject_id = " +
//                subjectId.toString()).executeUpdate();

        logger.info("commited Subject:id:" + subjectId);

    }

    @Transactional(readOnly = false)
    public void deleteInprogressSubject(String mrn) {
        Object subjectId = getSession().createSQLQuery("select s.id from subjects s where s.load_status=0 and s.person_id='" + mrn + "'").uniqueResult();
        if (subjectId == null) {
            String msg = "Either no subject exist with the given mrn :" + mrn + " or subject with given MRN is already commited";
            logger.error(msg);
            throw new StudyCalendarSystemException(msg);
        }

        //delete assignment
        getSession().createSQLQuery("delete from scheduled_study_segments sss where sss.scheduled_calendar_id  in " +
                "(select sc.id from scheduled_calendars sc where sc.assignment_id in (select sa.id from subject_assignments sa where subject_id =" +
                subjectId.toString() + "))").executeUpdate();

        getSession().createSQLQuery("delete from scheduled_calendars sc where assignment_id in " +
                "(select sa.id from subject_assignments sa where subject_id =" +
                subjectId.toString() + ")").executeUpdate();

        getSession().createSQLQuery("delete from subject_assignments where subject_id = " +
                subjectId.toString()).executeUpdate();

        getSession().createSQLQuery("delete from subject_assignments where subject_id = " +
                subjectId.toString()).executeUpdate();

        //delete subject
        getSession().createSQLQuery("delete from subjects where id = " +
                subjectId.toString()).executeUpdate();

        logger.info("rolledback Subject:id:" + subjectId);

    }


}
