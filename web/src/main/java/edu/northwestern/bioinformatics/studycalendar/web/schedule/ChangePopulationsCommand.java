/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Rhett Sutphin
 */
public class ChangePopulationsCommand {
    private SubjectService subjectService;

    private StudySubjectAssignment assignment;
    private Set<Population> populations;

    public ChangePopulationsCommand(StudySubjectAssignment assignment, SubjectService subjectService) {
        this.subjectService = subjectService;
        this.assignment = assignment;
        populations = new HashSet<Population>(this.assignment.getPopulations());
    }

    ////// LOGIC

    public void apply() {
        subjectService.updatePopulations(getAssignment(), getPopulations());
    }

    ////// BOUND PROPERTIES

    public StudySubjectAssignment getAssignment() {
        return assignment;
    }

    public Set<Population> getPopulations() {
        return populations;
    }

    public void setPopulations(Set<Population> populations) {
        this.populations = populations;
    }
}
