/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

import java.util.List;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class StudySegmentDao extends ChangeableDao<StudySegment> {
    @Override
    public Class<StudySegment> domainClass() {
        return StudySegment.class;
    }

    /**
     * Deletes the given study segment
     *
     * @param  segment the study segment to delete
     */
    public void delete(StudySegment segment) {
        getHibernateTemplate().delete(segment);
    }

    public void deleteOrphans() {
        deleteOrphans("StudySegment", "epoch", "EpochDelta", "epoch");
    }

    public void deleteAll(List<StudySegment> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
