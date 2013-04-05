/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "planned_calendars")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_calendars_id")
    }
)
public class PlannedCalendar extends PlanTreeOrderedInnerNode<Study, Epoch> {

    ////// LOGIC

    public Class<Study> parentClass() { return Study.class; }
    public Class<Epoch> childClass() { return Epoch.class; }

    public void addEpoch(Epoch epoch) {
        addChild(epoch);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Epoch epoch : getEpochs()) {
            len = Math.max(len, epoch.getLengthInDays());
        }
        return len;
    }

    @Transient
    public String getName() {
        return getStudy() == null ? null : getStudy().getName();
    }

    @Transient
    public int getMaxStudySegmentCount() {
        int max = 0;
        for (Epoch epoch : getEpochs()) {
            max = Math.max(max, epoch.getStudySegments().size());
        }
        return max;
    }

    @Override
    public Epoch findNaturallyMatchingChild(String key) {
        Collection<Epoch> found = findMatchingChildrenByName(key);
        if (found.size() == 1) return found.iterator().next();
        return null;
    }

    @Override
    public PlannedCalendar transientClone() {
        PlanTreeNode<Study> clone = super.transientClone();
        clone.setParent(null);
        return (PlannedCalendar) clone;
    }

    ////// BEAN PROPERTIES

    // This is annotated this way so that the IndexColumn will work with
    // the bidirectional mapping.  See section 2.4.6.2.3 of the hibernate annotations docs.
    @OneToMany
    @JoinColumn(name="planned_calendar_id", nullable=true)
    @IndexColumn(name="list_index")
    @Cascade(value = { CascadeType.ALL })
    public List<Epoch> getEpochs() {
        return getChildren();
    }

    public void setEpochs(List<Epoch> epochs) {
        setChildren(epochs);
    }

    @OneToOne
    @JoinColumn (name = "study_id", nullable = false)
    public Study getStudy() {
        return getParent();
    }

    public void setStudy(Study study) {
        if (study != null && study.getPlannedCalendar() != this) {
            study.setPlannedCalendar(this);
        }
        setParent(study);
    }
    
    public Differences deepEquals(Object o) {
        return new Differences();
    }
}
