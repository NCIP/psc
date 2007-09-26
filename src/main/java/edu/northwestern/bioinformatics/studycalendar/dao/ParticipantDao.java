package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;


public class ParticipantDao extends StudyCalendarMutableDomainObjectDao<Participant> {
    @Override
    public Class<Participant> domainClass() {
        return Participant.class;
    }

    public List<Participant> getAll() {
        return getHibernateTemplate().find("from Participant p order by p.lastName, p.firstName");
    }

    public StudyParticipantAssignment getAssignment(Participant participant, Study study, Site site) {
        return (StudyParticipantAssignment) CollectionUtils.firstElement(
            getHibernateTemplate().find(
                "from StudyParticipantAssignment a where a.participant = ? and a.studySite.site = ? and a.studySite.study = ?",
                new Object[] { participant, site, study }
            )
        );
    }
}

