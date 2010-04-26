package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;

import java.util.List;

public class StudySecondaryIdentifierDao extends StudyCalendarMutableDomainObjectDao<StudySecondaryIdentifier> implements DeletableDomainObjectDao<StudySecondaryIdentifier> {
    @Override public Class<StudySecondaryIdentifier> domainClass() { return StudySecondaryIdentifier.class; }
    public void delete(StudySecondaryIdentifier id) {
        getHibernateTemplate().delete(id);
    }

    public void deleteAll(List<StudySecondaryIdentifier> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
