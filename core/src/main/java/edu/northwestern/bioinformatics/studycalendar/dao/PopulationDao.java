/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PopulationDao extends StudyCalendarMutableDomainObjectDao<Population> implements DeletableDomainObjectDao<Population> {
    @Override
    public Class<Population> domainClass() {
        return Population.class;
    }

    /**
     * Finds the population based on study and the population abbreviation
     *
     * @param study        the study which you want to search for the population
     * @param abbreviation the abbreviation for the population
     * @return the population that corresponds to the study and the abbreviation that was passed in
     */
    @SuppressWarnings({"unchecked"})
    public Population getByAbbreviation(Study study, String abbreviation) {
        return (Population) CollectionUtils.firstElement(getHibernateTemplate().find(
                "from Population p where p.abbreviation = ? and p.study = ?",
                new Object[]{abbreviation, study}));
    }

    /**
     * Collects and returns all the abbreviations for populations in the given study
     *
     * @param study the study to return the populations from
     * @return populations retrieved from the given study
     */
    @SuppressWarnings({"unchecked"})
    public Set<String> getAbbreviations(Study study) {
        return new HashSet<String>(getHibernateTemplate().find(
                "select p.abbreviation from Population p where p.study = ?", study));
    }

    /**
     * Find all the populations for a study
     *
     * @param study the study to return the populations from
     * @return a list of all populations retrieved from the given study
     */
    @SuppressWarnings({ "unchecked" })
    public List<Population> getAllFor(Study study) {
        return getHibernateTemplate().find(
            "from Population p where p.study = ?", study);
    }

    @Transactional(readOnly = false)
    public void delete(final Population population) {
        getHibernateTemplate().delete(population);
    }

    public void deleteAll(List<Population> t) {
        getHibernateTemplate().delete(t);
    }
}
