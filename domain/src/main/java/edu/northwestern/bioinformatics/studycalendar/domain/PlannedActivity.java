package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.apache.commons.lang.StringUtils;
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
import java.util.*;

/**
 * @author Rhett Sutphin
 * @author Yufang Wang
 */
@Entity
@Table(name = "planned_activities")
@GenericGenerator(name = "id-generator", strategy = "native",
	parameters = {
		@Parameter(name = "sequence", value = "seq_planned_activities_id")
	}
)
public class PlannedActivity extends PlanTreeNode<Period>
    implements Comparable<PlannedActivity>, Parent<PlannedActivityLabel, SortedSet<PlannedActivityLabel>>
{
	private Activity activity;
	private Population population;
	private Integer day;
	private String details;
	private String condition;
    private Integer weight;
    private SortedSet<PlannedActivityLabel> plannedActivityLabels;

    public PlannedActivity() {
		plannedActivityLabels = new TreeSet<PlannedActivityLabel>();
	}

	////// LOGIC

	public Class<Period> parentClass() {
		return Period.class;
	}

    public int compareTo(PlannedActivity other) {
        // by weight
        int weightDiff = compareWeightTo(other);
        if (weightDiff != 0) return weightDiff;
        // by day
        int dayDiff = getDay() - other.getDay();
        if (dayDiff != 0) return dayDiff;
        // then by activity
        return getActivity().compareTo(other.getActivity());
    }

    public int compareWeightTo(PlannedActivity other) {
        return -1 * (this.getEffectiveWeight() - other.getEffectiveWeight());
    }

    @Transient
    public int getEffectiveWeight() {
        return getWeight() == null ? 0 : getWeight();
    }

	public void addPlannedActivityLabel(PlannedActivityLabel paLabel) {
		paLabel.setPlannedActivity(this);
		getPlannedActivityLabels().add(paLabel);
	}

	public PlannedActivityLabel removePlannedActivityLabel(PlannedActivityLabel paLabel) {
		if (getPlannedActivityLabels().remove(paLabel)) {
			paLabel.setParent(null);
			return paLabel;
		} else {
			return null;
		}
	}

    /**
     * Returns the day this PA represents relative to the containing segment.
     */
    // TODO: make cycleLength-aware
    @Transient
    public String getPlanDay() {
        if (getPeriod() == null) {
            return null;
        }
        return Integer.toString(getPeriod().getStartDay() + getDay() - 1);
    }

	@Transient
	public List<Integer> getDaysInStudySegment() {
		int dayInStudySegment = getPeriod().getStartDay() + getDay() - 1;
		List<Integer> days = new ArrayList<Integer>();
		while (days.size() < getPeriod().getRepetitions()) {
			days.add(dayInStudySegment);
			dayInStudySegment += getPeriod().getDuration().getDays();
		}
		return days;
	}

	@Transient
	public ScheduledActivityMode getInitialScheduledMode() {
		if (StringUtils.isBlank(getCondition())) {
			return ScheduledActivityMode.SCHEDULED;
		} else {
			return ScheduledActivityMode.CONDITIONAL;
		}
	}

	/**
	 * Returns all the labels that are used for any repetition in the planned activity
	 *
	 * @return
	 */
	@Transient
	public SortedSet<String> getLabels() {
		SortedSet<String> labels = new TreeSet<String>();
		for (PlannedActivityLabel paLabel : getPlannedActivityLabels()) {
			labels.add(paLabel.getLabel());
		}
		return labels;
	}

	@Transient
	public List<SortedSet<String>> getLabelsByRepetition() {
		if (getParent() == null) {
			throw new StudyCalendarSystemException("This method does not work unless the planned activity is part of a period");
		}
		int reps = getParent().getRepetitions();
		List<SortedSet<String>> byReps = new ArrayList<SortedSet<String>>(reps);
		while (byReps.size() < getParent().getRepetitions()) {
			byReps.add(getLabelsForRepetition(byReps.size()));
		}
		return byReps;
	}

	public SortedSet<String> getLabelsForRepetition(int rep) {
		SortedSet<String> labels = new TreeSet<String>(LabelComparator.INSTANCE);
		for (PlannedActivityLabel paLabel : getPlannedActivityLabels()) {
			if (paLabel.appliesToRepetition(rep)) {
				labels.add(paLabel.getLabel());
			}
		}
		return labels;
	}

	public Class<PlannedActivityLabel> childClass() {
		return PlannedActivityLabel.class;
	}

	public void addChild(PlannedActivityLabel child) {
		addPlannedActivityLabel(child);
	}

	public PlannedActivityLabel removeChild(PlannedActivityLabel child) {
		return removePlannedActivityLabel(child);
	}

	@Transient
	public SortedSet<PlannedActivityLabel> getChildren() {
		return getPlannedActivityLabels();
	}

	public void setChildren(SortedSet<PlannedActivityLabel> children) {
		setPlannedActivityLabels(children);
	}

	@Override
	public void clearIds() {
		super.clearIds();
		for (PlannedActivityLabel label : getChildren()) {
			label.setId(null);
			label.setGridId(null);
		}
	}

	////// BEAN PROPERTIES

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "activity_id")
	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "period_id")
	public Period getPeriod() {
		return getParent();
	}

	public void setPeriod(Period period) {
		setParent(period);
	}

	@ManyToOne
	public Population getPopulation() {
		return population;
	}

	@OneToMany(mappedBy = "plannedActivity")
	@Cascade(value = {CascadeType.ALL})
	@Sort(type = SortType.NATURAL)
	public SortedSet<PlannedActivityLabel> getPlannedActivityLabels() {
		return plannedActivityLabels;
	}

	public void setPlannedActivityLabels(SortedSet<PlannedActivityLabel> plannedActivityLabels) {
		this.plannedActivityLabels = plannedActivityLabels;
	}

	public void setPopulation(Population population) {
		this.population = population;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    ////// OBJECT METHODS

	@Override
	public PlannedActivity clone() {
		PlannedActivity clone = (PlannedActivity) super.clone();
		clone.setPlannedActivityLabels(new TreeSet<PlannedActivityLabel>());
		for (PlannedActivityLabel label : getPlannedActivityLabels()) {
			clone.addPlannedActivityLabel(label.clone());
		}
		return clone;
	}


	@Override
	protected PlannedActivity copy() {
		PlannedActivity copiedPlannedActivity = (PlannedActivity) super.copy();
		SortedSet<PlannedActivityLabel> plannedActivityLabels = copiedPlannedActivity.getChildren();
		for (PlannedActivityLabel plannedActivityLabel : plannedActivityLabels) {
			plannedActivityLabel.setId(null);
			plannedActivityLabel.setGridId(null);
			plannedActivityLabel.setVersion(null);
		}
		return copiedPlannedActivity;


	}

    public Differences deepEquals(Object o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || !(o instanceof PlannedActivity)) {
            differences.addMessage("not an instance of PlannedActivity");
            return differences;
        }

        PlannedActivity that = (PlannedActivity) o;

        differences.
            recurseDifferences("activity", getActivity(), that.getActivity()).
            registerValueDifference("day", getDay(), that.getDay()).
            registerValueDifference("weight", getWeight(), that.getWeight()).
            registerValueDifference("details", getDetails(), that.getDetails()).
            registerValueDifference("condition", getCondition(), that.getCondition()).
            registerValueDifference("population", getPopulation(), that.getPopulation()).
            recurseDifferences("label", getPlannedActivityLabels(), that.getPlannedActivityLabels());

        return differences;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlannedActivity)) return false;

		PlannedActivity that = (PlannedActivity) o;

		if (activity != null ? !activity.equals(that.getActivity()) : that.getActivity() != null)
			return false;
		if (condition != null ? !condition.equals(that.getCondition()) : that.getCondition() != null)
			return false;
		if (day != null ? !day.equals(that.getDay()) : that.getDay() != null) return false;
		if (weight != null ? !weight.equals(that.getWeight()) : that.getWeight() != null) return false;
        if (details != null ? !details.equals(that.getDetails()) : that.getDetails() != null) return false;
		if (plannedActivityLabels != null ? !plannedActivityLabels.equals(that.getPlannedActivityLabels()) : that.getPlannedActivityLabels() != null)
			return false;
		if (population != null ? !population.equals(that.getPopulation()) : that.getPopulation() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (activity != null ? activity.hashCode() : 0);
		result = 31 * result + (population != null ? population.hashCode() : 0);
		result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
		result = 31 * result + (condition != null ? condition.hashCode() : 0);
		return result;
	}

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[id=").append(getId()).
            append("; activity=").append(getActivity()).
            append("; day=").append(getDay()).
            append("; details=").append(getDetails()).
            append("; condition=").append(getCondition()).
            append("; weight=").append(getWeight()).
            append("; population=").append(getPopulation() == null ? "<none>" : getPopulation().getAbbreviation()).
            append("; labels=").append(getLabels() == null ? "<none>" : getLabels()).
            append(']').toString();
    }
}
