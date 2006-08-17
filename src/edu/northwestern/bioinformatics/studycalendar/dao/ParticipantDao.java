package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;


/**
 * @author Padmaja Vedula
 */
public class ParticipantDao extends HibernateDaoSupport {
    public Participant getById(int id) {
        return (Participant) getHibernateTemplate().get(Participant.class, id);
    }

    public void save(Participant participant) {
        getHibernateTemplate().saveOrUpdate(participant);
    }


}
