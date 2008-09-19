package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.utils.BeanPropertyListComparator;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Comparator;


@Entity
@Table(name = "planned_activity_labels")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_planned_activity_labels_id")
    }
)
public class PlannedActivityLabel extends AbstractMutableDomainObject
    implements Cloneable, Child<PlannedActivity>, Comparable<PlannedActivityLabel>
{
    private Comparator<PlannedActivityLabel> COMPARATOR
        = new BeanPropertyListComparator<PlannedActivityLabel>().
            addProperty("label", LabelComparator.INSTANCE).addProperty("repetitionNumber");

    private PlannedActivity plannedActivity;
    private Integer repetitionNumber;
    private String label;
    private boolean memoryOnly;

    ////// LOGIC

    @Transient
    public boolean isAllRepetitions() {
        return getRepetitionNumber() == null;
    }

    private static String normalizeLabel(String input) {
        if (input == null) return null;
        return input.replaceAll("\\s+", "-").toLowerCase();
    }

    public int compareTo(PlannedActivityLabel o) {
        return COMPARATOR.compare(this, o);
    }

    ////// IMPLEMENTATION OF Child

    public Class<PlannedActivity> parentClass() {
        return PlannedActivity.class;
    }

    public void setParent(PlannedActivity parent) {
        setPlannedActivity(parent);
    }

    @Transient
    public PlannedActivity getParent() {
        return getPlannedActivity();
    }

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public PlannedActivityLabel transientClone() {
        PlannedActivityLabel clone = clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    ////// BEAN PROPERTIES

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_activity_id")
    public PlannedActivity getPlannedActivity() {
        return plannedActivity;
    }

    public void setPlannedActivity(PlannedActivity plannedActivity) {
        this.plannedActivity = plannedActivity;
    }

    @Column(name="rep_num")
    public Integer getRepetitionNumber() {
        return repetitionNumber;
    }

    public void setRepetitionNumber(Integer repetitionNumber) {
        this.repetitionNumber = repetitionNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = normalizeLabel(label);
    }

    ////// OBJECT METHODS

    @Override
    @SuppressWarnings({ "unchecked" })
    protected PlannedActivityLabel clone() {
        try {
            PlannedActivityLabel clone = (PlannedActivityLabel) super.clone();
            clone.setPlannedActivity(null);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlannedActivityLabel)) return false;

        PlannedActivityLabel that = (PlannedActivityLabel) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (plannedActivity != null ? !plannedActivity.equals(that.plannedActivity) : that.plannedActivity != null)
            return false;
        if (repetitionNumber != null ? !repetitionNumber.equals(that.repetitionNumber) : that.repetitionNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (plannedActivity != null ? plannedActivity.hashCode() : 0);
        result = 31 * result + (repetitionNumber != null ? repetitionNumber.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("[id=").append(getId())
            .append("; label=").append(getLabel());
        if (isAllRepetitions()) {
            sb.append("; all reps");
        } else {
            sb.append("; rep number=").append(getRepetitionNumber());
        }
        sb.append(']');
        return sb.toString();
    }
}