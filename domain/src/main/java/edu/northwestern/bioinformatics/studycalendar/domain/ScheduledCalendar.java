/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table
@GenericGenerator(name="id-generator", strategy="native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_calendars_id")
    }
)
public class ScheduledCalendar extends AbstractMutableDomainObject {
    private StudySubjectAssignment assignment;
    private List<ScheduledStudySegment> scheduledStudySegments = new LinkedList<ScheduledStudySegment>();

    ////// LOGIC

    public void addStudySegment(ScheduledStudySegment studySegment) {
        scheduledStudySegments.add(studySegment);
        studySegment.setScheduledCalendar(this);
    }

    @Transient
    public ScheduledStudySegment getCurrentStudySegment() {
        for (ScheduledStudySegment studySegment : getScheduledStudySegments()) {
            if (!studySegment.isComplete()) return studySegment;
        }
        return getScheduledStudySegments().get(getScheduledStudySegments().size() - 1);
    }

    @Transient
    public List<ScheduledStudySegment> getScheduledStudySegmentsFor(StudySegment source) {
        List<ScheduledStudySegment> matches = new ArrayList<ScheduledStudySegment>(getScheduledStudySegments().size());
        for (ScheduledStudySegment scheduledStudySegment : getScheduledStudySegments()) {
            if (scheduledStudySegment.getStudySegment().equals(source)) matches.add(scheduledStudySegment);
        }
        return matches;
    }

    ////// BEAN PROPERTIES
    @ManyToOne
    @JoinColumn(name = "assignment_id")
    public StudySubjectAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(StudySubjectAssignment assignment) {
        this.assignment = assignment;
    }

    // This is annotated this way so that the IndexColumn will work with
    // the bidirectional mapping.  See section 2.4.6.2.3 of the hibernate annotations docs.
    @Fetch(FetchMode.JOIN)
    @OneToMany
    @JoinColumn(name="scheduled_calendar_id", nullable=false)
    @IndexColumn(name="list_index")
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<ScheduledStudySegment> getScheduledStudySegments() {
        return scheduledStudySegments;
    }

    public void setScheduledStudySegments(List<ScheduledStudySegment> studySegments) {
        this.scheduledStudySegments = studySegments;
    }
}
