package edu.northwestern.bioinformatics.studycalendar.dao;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;


public class ParticipantDao extends HibernateDaoSupport {
    public Participant getById(int id) {
        return (Participant) getHibernateTemplate().get(Participant.class, id);
    }

    public void save(Participant participant) {
        getHibernateTemplate().saveOrUpdate(participant);
    }
    public List<Participant> getAll() {
        return getHibernateTemplate().find("from Participant");
    }

}

