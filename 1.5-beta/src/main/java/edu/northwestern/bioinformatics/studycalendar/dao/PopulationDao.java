package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.Set;
import java.util.HashSet;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PopulationDao extends StudyCalendarMutableDomainObjectDao<Population> {
    @Override
    public Class<Population> domainClass() {
        return Population.class;
    }

    @SuppressWarnings({ "unchecked" })
    public Population getByAbbreviation(Study study, String abbreviation) {
        return (Population) CollectionUtils.firstElement(getHibernateTemplate().find(
            "from Population p where p.abbreviation = ? and p.study = ?",
            new Object[] { abbreviation, study }));
    }

    /**
     * Collects and returns all the abbreviations for populations in the given study
     */
    @SuppressWarnings({ "unchecked" })
    public Set<String> getAbbreviations(Study study) {
        return new HashSet<String>(getHibernateTemplate().find(
            "select p.abbreviation from Population p where p.study = ?", study));
    }
}
