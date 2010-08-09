package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import gov.nih.nci.cabig.ctms.tools.hibernate.MoreRestrictions;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudySubjectAssignmentDao extends StudyCalendarMutableDomainObjectDao<StudySubjectAssignment> implements DeletableDomainObjectDao<StudySubjectAssignment> {
    @Override
    public Class<StudySubjectAssignment> domainClass() {
        return StudySubjectAssignment.class;
    }

    /**
     * Returns on-study patients with nothing scheduled beyond a date
     *
     * @param date
     * @return
     */
    public List<StudySubjectAssignment> getAllAssignmenetsWhichHaveNoActivityBeyondADate(Date date) {
        //following HQL execute following sql query

        /*select studysubje2_.id as id14_, studysubje2_.version as version14_, studysubje2_.grid_id as grid3_14_, studysubje2_.study_site_id as study10_14_,
         studysubje2_.current_amendment_id as current7_14_, studysubje2_.subject_id as subject8_14_, studysubje2_.first_epoch_stdate as first4_14_,
         studysubje2_.last_epoch_enddate as last5_14_, studysubje2_.subject_coordinator_id as subject9_14_, studysubje2_.study_id as study6_14_

         from scheduled_study_segments scheduleds0_, scheduled_calendars scheduledc1_
         inner join subject_assignments studysubje2_ on scheduledc1_.assignment_id=studysubje2_.id
         where scheduleds0_.scheduled_calendar_id=scheduledc1_.id
         and ( scheduleds0_.id not in  ( select scheduleda3_.scheduled_study_segment_id from scheduled_activities scheduleda3_ where scheduleda3_.ideal_date>=? ) )
        */
        final List list = getHibernateTemplate().find("select  sss.scheduledCalendar.assignment from ScheduledStudySegment as sss " +
                "where sss.id not in( select sa.scheduledStudySegment.id from ScheduledActivity sa where sa.idealDate>=?)",
                new Object[]{date});
        return list;

    }

    @SuppressWarnings({ "unchecked" })
    public StudySubjectAssignment getByStudySubjectIdentifier(Study inStudy, String identifier) {
        return CollectionUtils.firstElement((List<StudySubjectAssignment>) getHibernateTemplate().find(
            "select ssa from StudySubjectAssignment ssa where ssa.studySite.study = ? and ssa.studySubjectId = ?",
            new Object[] { inStudy, identifier }
        ));
    }

    public void delete(StudySubjectAssignment t) {
        getHibernateTemplate().delete(t);
    }

    public void deleteAll(List<StudySubjectAssignment> t) {
        getHibernateTemplate().deleteAll(t);
    }

    @SuppressWarnings({ "unchecked" })
    public List<StudySubjectAssignment> getAssignmentsInIntersection(
        Collection<Integer> studyIds, Collection<Integer> siteIds
    ) {
        if (studyIds == null && siteIds == null) return getAll();
        DetachedCriteria criteria = criteria().createAlias("studySite", "ss");

        Conjunction and = Restrictions.conjunction();
        if (siteIds != null) {
            and.add(MoreRestrictions.in("ss.site.id", siteIds));
        }
        if (studyIds != null) {
            and.add(MoreRestrictions.in("ss.study.id", studyIds));
        }
        criteria.add(and);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    @SuppressWarnings({ "unchecked" })
    public List<StudySubjectAssignment> getAssignmentsByManagerCsmUserId(int csmUserId) {
        return getHibernateTemplate().findByCriteria(
            criteria().add(Restrictions.eq("managerCsmUserId", csmUserId)));
    }

    private DetachedCriteria criteria() {
        return DetachedCriteria.forClass(StudySubjectAssignment.class);
    }
}
