package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.tools.hibernate.MoreRestrictions;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;


/**
 * @author Padmaja Vedula
 */
public class StudySiteDao extends StudyCalendarMutableDomainObjectDao<StudySite> implements DeletableDomainObjectDao<StudySite> {
    @Override public Class<StudySite> domainClass() { return StudySite.class; }

    @SuppressWarnings({ "unchecked" })
    public List<Integer> getIntersectionIds(
        Collection<Integer> studyIds, Collection<Integer> siteIds
    ) {
        if (studyIds == null && siteIds == null) return null;
        return getHibernateTemplate().findByCriteria(
            createIntersectionCriteria(studyIds, siteIds).setProjection(Projections.id()));
    }

    @SuppressWarnings({ "unchecked" })
    public List<StudySite> getIntersections(
        Collection<Integer> studyIds, Collection<Integer> siteIds
    ) {
        return getHibernateTemplate().findByCriteria(createIntersectionCriteria(studyIds, siteIds));
    }

    private DetachedCriteria createIntersectionCriteria(
        Collection<Integer> studyIds, Collection<Integer> siteIds
    ) {
        DetachedCriteria criteria = DetachedCriteria.forClass(StudySite.class);

        Conjunction and = Restrictions.conjunction();
        if (siteIds != null) {
            and.add(MoreRestrictions.in("site.id", siteIds));
        }
        if (studyIds != null) {
            and.add(MoreRestrictions.in("study.id", studyIds));
        }
        criteria.add(and);
        return criteria;
    }

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
