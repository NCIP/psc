package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;


public class SubjectDao extends StudyCalendarMutableDomainObjectDao<Subject> {
    @Override
    public Class<Subject> domainClass() {
        return Subject.class;
    }

    public List<Subject> getAll() {
        return getHibernateTemplate().find("from Subject p order by p.lastName, p.firstName");
    }

    public StudySubjectAssignment getAssignment(Subject subject, Study study, Site site) {
        return (StudySubjectAssignment) CollectionUtils.firstElement(
            getHibernateTemplate().find(
                "from StudySubjectAssignment a where a.subject = ? and a.studySite.site = ? and a.studySite.study = ?",
                new Object[] { subject, site, study }
            )
        );
    }
}

