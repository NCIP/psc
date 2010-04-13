package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.List;


/**
 * @author Padmaja Vedula
 */
public class StudySiteDao extends StudyCalendarMutableDomainObjectDao<StudySite> implements DeletableDomainObjectDao<StudySite> {
    @Override public Class<StudySite> domainClass() { return StudySite.class; }

    /**
     * Deletes the study site relationship
     *
     * @param  studySite the study site relationship to delete
     */
    public void delete(StudySite studySite) {
        getHibernateTemplate().delete(studySite);
    }

    public void deleteAll(List<StudySite> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
