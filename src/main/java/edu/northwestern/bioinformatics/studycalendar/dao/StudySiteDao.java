package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;


/**
 * @author Padmaja Vedula
 */
public class StudySiteDao extends StudyCalendarMutableDomainObjectDao<StudySite> {
    @Override public Class<StudySite> domainClass() { return StudySite.class; }

    public void delete(StudySite studySite) {
        getHibernateTemplate().delete(studySite);
    }
}
