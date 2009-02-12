package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.DayRange;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DefaultDayRange;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.EmptyDayRange;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "study_segments")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_study_segments_id")
    }
)
public class StudySegment extends PlanTreeInnerNode<Epoch, Period, SortedSet<Period>> implements Named {
    private String name;
    private Integer cycleLength;

    ////// LOGIC

    public Class<Epoch> parentClass() { return Epoch.class; }
    public Class<Period> childClass() { return Period.class; }

    @Override protected SortedSet<Period> createChildrenCollection() { return new TreeSet<Period>(); }

    public void addPeriod(Period period) {
        addChild(period);
    }

    @Transient
    public String getQualifiedName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getEpoch() == null ? getName() : getEpoch().getName());
        if (getEpoch() != null && getEpoch().isMultipleStudySegments()) {
            sb.append(": ").append(getName());
        }
        return sb.toString();
    }

    @Transient
    public int getLengthInDays() {
        return getDayRange().getDayCount();
    }

    @Transient
    public DayRange getDayRange() {
        if (getPeriods().size() == 0) {
            return EmptyDayRange.INSTANCE;
        } else {
            DefaultDayRange range = new DefaultDayRange(Integer.MAX_VALUE, Integer.MIN_VALUE);
            for (Period period : getPeriods()) {
                range.add(period.getTotalDayRange());
            }
            return range;
        }
    }

    @Override
    public Period findNaturallyMatchingChild(String key) {
        Collection<Period> found = findMatchingChildrenByName(key);
        if (found.size() == 1) return found.iterator().next();
        found = findMatchingChildrenByGridId(key);
        if (found.size() == 1) return found.iterator().next();
        return null;
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // This is annotated this way so that the IndexColumn in the parent
    // will work with the bidirectional mapping
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(insertable=false, updatable=false)
    public Epoch getEpoch() {
        return getParent();
    }

    public void setEpoch(Epoch epoch) {
        setParent(epoch);
    }

    @OneToMany(mappedBy = "studySegment")
    @Cascade(value = { CascadeType.ALL })
    @Sort(type = SortType.NATURAL)
    public SortedSet<Period> getPeriods() {
        return getChildren();
    }

    public void setPeriods(SortedSet<Period> periods) {
        setChildren(periods);
    }
    
    public Integer getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(Integer cycleLength) {
        this.cycleLength = cycleLength;
    }
}
