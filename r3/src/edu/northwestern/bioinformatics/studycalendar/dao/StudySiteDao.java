package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.List;


import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Padmaja Vedula
 */
public class StudySiteDao extends StudyCalendarDao<StudySite> {
    public Class<StudySite> domainClass() {
        return StudySite.class;
    }

    public void save(StudySite studySite) {
        getHibernateTemplate().saveOrUpdate(studySite);
    }
}
