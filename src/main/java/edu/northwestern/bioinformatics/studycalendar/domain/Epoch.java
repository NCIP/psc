package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "epochs")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_epochs_id")
    }
)
public class Epoch extends PlanTreeOrderedInnerNode<PlannedCalendar, StudySegment> implements Named {
    private String name;

    ////// FACTORY

    public static Epoch create(String epochName, String... studySegmentNames) {
        Epoch epoch = new Epoch();
        epoch.setName(epochName);
        if (studySegmentNames.length == 0) {
            epoch.addNewStudySegment(epochName);
        } else {
            for (String studySegmentName : studySegmentNames) {
                epoch.addNewStudySegment(studySegmentName);
            }
        }
        return epoch;
    }

    private void addNewStudySegment(String studySegmentName) {
        StudySegment studySegment = new StudySegment();
        studySegment.setName(studySegmentName);
        addStudySegment(studySegment);
    }

    ////// LOGIC

    public Class<PlannedCalendar> parentClass() { return PlannedCalendar.class; }
    public Class<StudySegment> childClass() { return StudySegment.class; }

    public void addStudySegment(StudySegment studySegment) {
        addChild(studySegment);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (StudySegment studySegment : getStudySegments()) {
            len = Math.max(len, studySegment.getLengthInDays());
        }
        return len;
    }

    @Transient
    public boolean isMultipleStudySegments() {
        return getStudySegments().size() > 1;
    }

    @Override
    public StudySegment findNaturallyMatchingChild(String key) {
        Collection<StudySegment> found = findMatchingChildrenByName(key);
        if (found.size() == 1) return found.iterator().next();
        return null;
    }

    ////// BEAN PROPERTIES

    // This is annotated this way so that the IndexColumn will work with
    // the bidirectional mapping.  See section 2.4.6.2.3 of the hibernate annotations docs.
    @OneToMany
    @JoinColumn(name="epoch_id")
    @IndexColumn(name="list_index")
    @Cascade(value = { CascadeType.ALL})
    public List<StudySegment> getStudySegments() {
        return getChildren();
    }

    public void setStudySegments(List<StudySegment> studySegments) {
        setChildren(studySegments);
    }

    // This is annotated this way so that the IndexColumn in the parent
    // will work with the bidirectional mapping
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(insertable=false, updatable=false, nullable=true)
    public PlannedCalendar getPlannedCalendar() {
        return getParent();
    }

    public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
        setParent(plannedCalendar);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
