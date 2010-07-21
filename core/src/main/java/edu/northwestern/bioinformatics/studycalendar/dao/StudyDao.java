package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleStudyParameters;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import gov.nih.nci.cabig.ctms.tools.hibernate.MoreRestrictions;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends StudyCalendarMutableDomainObjectDao<Study> implements DeletableDomainObjectDao<Study> {
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    /**
     * Finds the assignments for a particular study
     *
     * @param studyId the study to get the assignments for
     *
     * @return a list of the assignments for the study id given
     */
    @SuppressWarnings({"unchecked"})
    public List<StudySubjectAssignment> getAssignmentsForStudy(final Integer studyId) {
        return getHibernateTemplate().find(
            "select a from StudySubjectAssignment a inner join a.studySite ss inner join a.subject p where ss.study.id = ? order by p.lastName, p.firstName",
            studyId);
    }

    /**
     * Finds a study based on the assignment identifer given
     *
     * @param assignedIdentifier the assignment identifier for the study to search for
     *
     * @return the study that has the given assignment identifier
     */
    @SuppressWarnings({"unchecked"})
    public Study getByAssignedIdentifier(String assignedIdentifier) {
        List<Study> results = getHibernateTemplate().find("from Study where assignedIdentifier= ?", assignedIdentifier);
        return CollectionUtils.firstElement(results);
    }

    @SuppressWarnings({ "unchecked" })
    public List<Study> getByAssignedIdentifiers(List<String> assignedIdentifiers) {
        List<Study> fromDatabase = getHibernateTemplate().findByCriteria(
            criteria().add(MoreRestrictions.in("assignedIdentifier", assignedIdentifiers)));
        List<Study> inOrder = new ArrayList<Study>();
        for (String assignedIdentifier : assignedIdentifiers) {
            for (Iterator<Study> foundIt = fromDatabase.iterator(); foundIt.hasNext();) {
                Study found = foundIt.next();
                if (found.getAssignedIdentifier().equals(assignedIdentifier)) {
                    inOrder.add(found);
                    foundIt.remove();
                    break;
                }
            }
        }
        return inOrder;
    }

    /**
     * Gets the study id that corresponds to the given assignment identifier
     *
     * @param assignedIdentifier the assignment identifier to search for the study to search for
     *
     * @return the study id that corresponds to the study assignment identifier given
     */
    public String getStudyIdByAssignedIdentifier(final String assignedIdentifier) {
        Object studyId = getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createSQLQuery(
                    "select max(id) from studies where  assigned_identifier='" + assignedIdentifier + '\'')
                    .uniqueResult();
            }
        });

        if (studyId != null) return studyId.toString();
        return null;
    }

    /**
	 * Gets the StudySecondaryIdentifier that corresponds to the Coppa Identifier
	 * 
	 * @param identifierType
	 * 				type is equal to coppa
	 * 
	 * @param coppaIdentifier 
	 * 				the coppa identifier to search for
	 * 
	 * @return the StudySecondaryIdentifier that corresponds to given identifier and type
	 */
	public StudySecondaryIdentifier getStudySecondaryIdentifierByCoppaIdentifier(
			final String identifierType, final String coppaIdentifier) {
		List<StudySecondaryIdentifier> results = getHibernateTemplate()
				.find("from StudySecondaryIdentifier where type=? and value=?",
						new Object[] { identifierType, coppaIdentifier });
		if ((results != null) && (results.size() > 0)) {
			return results.get(0);
		}
		return null;
	}
	
    /**
     * Deletes a study
     *
     * @param study the study to be deleted
     */
    @Transactional(readOnly = false)
    public void delete(Study study) {
        getHibernateTemplate().delete(study);
    }

    public void deleteAll(List<Study> t) {
        getHibernateTemplate().deleteAll(t);
    }

    /**
     * Search studies by matching study name (or assigned identifier of study) to search text.
     */
    @SuppressWarnings({ "unchecked" })
    public List<Study> searchStudiesByStudyName(final String studySearchText) {
        return (List<Study>) getHibernateTemplate().findByCriteria(
            criteria().add(Restrictions.ilike(
                "assignedIdentifier", studySearchText, MatchMode.ANYWHERE)).
                addOrder(Order.desc("assignedIdentifier")));
    }

    @SuppressWarnings({ "unchecked" })
    public List<Study> searchStudiesByAssignedIdentifier(final String studySearchText) {
        return (List<Study>) getHibernateTemplate().find(
            "from Study s where s.assignedIdentifier LIKE ? ORDER BY s.assignedIdentifier", studySearchText);
    }

    @SuppressWarnings({"unchecked"})
    public List<Study> getVisibleStudies(VisibleStudyParameters parameters) {
        return searchVisibleStudies(parameters, null);
    }

    @SuppressWarnings({"unchecked"})
    public List<Study> searchVisibleStudies(VisibleStudyParameters parameters, String search) {
        log.debug("Searching visible studies for {} with {}", parameters,
            search == null ? "no term" : "term \"" + search + "\"");
        if (parameters.isAllManagingSites()) {
            return search == null ? getAll() : searchStudiesByStudyName(search);
        } else {
            // These are implemented as separate queries and then merged because
            // the criteria are too complex to reliably express in a single statement.
            List<DetachedCriteria> separateCriteria = new LinkedList<DetachedCriteria>();
            
            if (!parameters.getSpecificStudyIdentifiers().isEmpty()) {
                separateCriteria.add(criteria().add(MoreRestrictions.
                    in("assignedIdentifier", parameters.getSpecificStudyIdentifiers())));
            }
            if (!parameters.getManagingSiteIdentifiers().isEmpty()) {
                separateCriteria.add(criteria().createAlias("managingSites", "ms", Criteria.LEFT_JOIN).add(
                    Restrictions.disjunction().
                        add(MoreRestrictions.in("ms.assignedIdentifier", parameters.getManagingSiteIdentifiers())).
                        add(Restrictions.isNull("ms.assignedIdentifier")) // <- unmanaged studies
                ));
            }
            if (parameters.isAllParticipatingSites()) {
                separateCriteria.add(criteria().createAlias("studySites", "ss").
                    add(Restrictions.isNotNull("ss.id")));
            } else if (!parameters.getParticipatingSiteIdentifiers().isEmpty()) {
                separateCriteria.add(criteria().createAlias("studySites", "ss").createAlias("ss.site", "s").
                    add(MoreRestrictions.in("s.assignedIdentifier", parameters.getParticipatingSiteIdentifiers())));
            }

            Set<Integer> ids = new LinkedHashSet<Integer>();
            for (DetachedCriteria criteria : separateCriteria) {
                if (search != null) {
                    criteria.add(Restrictions.ilike("assignedIdentifier", search, MatchMode.ANYWHERE));
                }
                ids.addAll(getHibernateTemplate().findByCriteria(criteria.setProjection(Projections.id())));
            }
            return getHibernateTemplate().findByCriteria(criteria().add(MoreRestrictions.in("id", ids)));
        }
    }

    private DetachedCriteria criteria() {
        return DetachedCriteria.forClass(Study.class);
    }
}
