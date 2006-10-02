package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.List;


import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Padmaja Vedula
 */
public class StudySiteDao extends HibernateDaoSupport {
    public StudySite getById(int id) {
        return (StudySite) getHibernateTemplate().get(StudySite.class, new Integer(id));
    }

    public void save(StudySite studySite) {
        getHibernateTemplate().saveOrUpdate(studySite);
    }
}
