package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.*;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "studies")
@GenericGenerator(name = "id-generator", strategy = "native",
	parameters = {
		@Parameter(name = "sequence", value = "seq_studies_id")
	}
)
@Where(clause = "load_status > 0")
public class Study extends AbstractMutableDomainObject implements Named,  Cloneable, NaturallyKeyed, Parent<Population, Set<Population>> {

    private String assignedIdentifier;
    private String longTitle;
    private PlannedCalendar plannedCalendar;
    private LoadStatus loadStatus = LoadStatus.COMPLETE;

	private Amendment amendment;			// the current effective/released amendment
	private Amendment developmentAmendment; // the next amendment, currently in development and not released

	private List<StudySite> studySites = new ArrayList<StudySite>();
	private Set<Population> populations = new LinkedHashSet<Population>();

	private boolean memoryOnly = false;

	////// LOGIC

	/**
	 * Adapter for backwards compatibility.  Passes through to assignedIdentifier.
	 */
	@Transient
	public String getName() {
		return getAssignedIdentifier();
	}

	/**
	 * Adapter for backwards compatibility.  Passes through to assignedIdentifier.
	 */
	public void setName(String name) {
		setAssignedIdentifier(name);
	}

	@Transient
	public String getNaturalKey() {
		return getAssignedIdentifier();
	}

	@Transient
	public boolean isInDevelopment() {
		return getDevelopmentAmendment() != null;
	}

	@Transient
	public boolean isInInitialDevelopment() {
		return getDevelopmentAmendment() != null && getAmendment() == null;
	}

	@Transient
	public boolean isInAmendmentDevelopment() {
		return getDevelopmentAmendment() != null && getAmendment() != null;
	}

	@Transient
	public boolean isReleased() {
		return getAmendment() != null;
	}

	@Transient
	public boolean isAmended() {
		return getAmendment().getPreviousAmendment() != null;
	}

	public void addStudySite(StudySite studySite) {
		getStudySites().add(studySite);
		studySite.setStudy(this);
	}

	public void addSite(Site site) {
		if (!getSites().contains(site)) {
			StudySite newSS = new StudySite();
			newSS.setSite(site);
			addStudySite(newSS);
		}
	}

	public StudySite getStudySite(Site site) {
		for (StudySite ss : getStudySites()) {
			if (ss.getSite().equals(site)) return ss;
		}
		return null;
	}

	@Transient
	public List<Site> getSites() {
		List<Site> sites = new ArrayList<Site>(getStudySites().size());
		for (StudySite studySite : studySites) {
			sites.add(studySite.getSite());
		}
		return Collections.unmodifiableList(sites);
	}

	@Transient
	public boolean isMemoryOnly() {
		return memoryOnly;
	}

	public void setMemoryOnly(boolean memoryOnly) {
		this.memoryOnly = memoryOnly;
		getPlannedCalendar().setMemoryOnly(true);
	}

	public Study transientClone() {
		Study clone = clone();
		clone.setMemoryOnly(true);
		return clone;
	}

	public void pushAmendment(Amendment newAmendment) {
		newAmendment.setPreviousAmendment(getAmendment());
		setAmendment(newAmendment);
	}

	public boolean hasAmendment(Amendment a) {
		return a.equals(getDevelopmentAmendment())
			|| a.equals(getAmendment())
			|| (getAmendment() != null && getAmendment().hasPreviousAmendment(a));
	}

	@Transient
	public List<Amendment> getAmendmentsList() {
		List<Amendment> amendments = new LinkedList<Amendment>();
		Amendment current = getAmendment();
		while (current != null) {
			amendments.add(current);
			current = current.getPreviousAmendment();
		}
		return Collections.unmodifiableList(amendments);
	}

	public void addPopulation(Population population) {
		getPopulations().add(population);
		population.setStudy(this);
	}

	////// BEAN PROPERTIES

	/**
	 * The identifier given to the study by the organization in charge of it
	 *
	 * @return
	 */
	public String getAssignedIdentifier() {
		return assignedIdentifier;
	}

	public void setAssignedIdentifier(String assignedIdentifier) {
		this.assignedIdentifier = assignedIdentifier;
	}

	@OneToOne(mappedBy = "study")
	@Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
	public PlannedCalendar getPlannedCalendar() {
		return plannedCalendar;
	}

	public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
		this.plannedCalendar = plannedCalendar;
		if (plannedCalendar != null && plannedCalendar.getStudy() != this) {
			plannedCalendar.setStudy(this);
		}
	}

	public void setStudySites(List<StudySite> studySites) {
		this.studySites = studySites;
	}

	@OneToMany(mappedBy = "study", fetch = FetchType.EAGER)
	@Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
	public List<StudySite> getStudySites() {
		return studySites;
	}

	@OneToMany(mappedBy = "study")
	@OrderBy(value = "name")
	@Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
	public Set<Population> getPopulations() {
		return populations;
	}

	public void setPopulations(Set<Population> populations) {
		this.populations = populations;
	}

	@ManyToOne
	public Amendment getAmendment() {
		return amendment;
	}

	public void setAmendment(Amendment amendment) {
		this.amendment = amendment;
	}

	@ManyToOne
	@JoinColumn(name = "dev_amendment_id")
	public Amendment getDevelopmentAmendment() {
		return developmentAmendment;
	}

	public void setDevelopmentAmendment(Amendment developmentAmendment) {
		this.developmentAmendment = developmentAmendment;
	}

	// @Type(type = "edu.northwestern.bioinformatics.studycalendar.domain.LoadStatus")
	@Enumerated(EnumType.ORDINAL)
	public LoadStatus getLoadStatus() {
		return loadStatus;
	}

	/**
	 * Added for hibernate only..
	 * This method will not change the load status...The load status will always be {LoadStatus.COMPLETE}.
	 *
	 * @param loadStatus
	 */
	public void setLoadStatus(LoadStatus loadStatus) {
		this.loadStatus = loadStatus;
	}

	public String getLongTitle() {
		return longTitle;
	}

	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	////// OBJECT METHODS

    @Override
    public Study clone() {
        try {
            // deep-clone the template portions only, for the moment
            Study clone = (Study) super.clone();
            if (getPlannedCalendar() != null) {
                clone.setPlannedCalendar((PlannedCalendar) getPlannedCalendar().clone());
            }
            clone.setPopulations(new TreeSet<Population>());
            for (Population pouPopulation: getPopulations()){
                clone.addPopulation(pouPopulation.clone());
            }
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName())
                .append("[id=").append(getId())
                .append("; assignedIdentifier=").append(getName());
        if (isMemoryOnly()) sb.append("; transient copy");
        return sb.append(']').toString();
    }

	@Transient
	public Date getLastModifiedDate() {
		Date lastModifiedDate = null;
		List<Amendment> amendmentList = new ArrayList<Amendment>();

		if (isReleased()) {
			amendmentList.add(getAmendment());
		}
		if (isInDevelopment()) {

			amendmentList.add(getDevelopmentAmendment());
		}

		for (Amendment amendment : amendmentList) {

			if (lastModifiedDate == null) {
				lastModifiedDate = amendment.getLastModifiedDate();
			} else if (amendment.getLastModifiedDate() != null && amendment.getLastModifiedDate().compareTo(lastModifiedDate) > 0) {
				lastModifiedDate = amendment.getLastModifiedDate();

			}
		}


		return lastModifiedDate;
	}


   public Population removePopulation(Population population) {
        if (getPopulations().remove(population)) {
            population.setParent(null);
            return population;
        } else {
            return null;
        }
    }

    public Class<Population> childClass() {
        return Population.class;
    }

    public void addChild(Population child) {
        addPopulation(child);
    }

    public Population removeChild(Population child) {
        return removePopulation(child);
    }

    @Transient
    public Set<Population> getChildren() {
        return getPopulations();
    }

    public void setChildren(Set<Population> children) {
        setPopulations(children);
    }

    @Transient
	public List<Amendment> getAmendmentsListInReverseOrder() {
		List<Amendment> amendments = new LinkedList<Amendment>();
		Amendment current = getAmendment();
		while (current != null) {
			amendments.add(current);
			current = current.getPreviousAmendment();
		}
		Collections.reverse(amendments);
		return Collections.unmodifiableList(amendments);
	}

    public Study copy(String newStudyName) {

		Study copiedStudy = TemplateSkeletonCreatorImpl.createBase(newStudyName);
		copiedStudy.setLongTitle(this.getLongTitle());

		copiedStudy.setStudySites(new ArrayList<StudySite>());

		//now copy populations also
		copiedStudy.setPopulations(new LinkedHashSet<Population>());
		Set<Population> populationSet = this.getPopulations();
		for (Population population : populationSet) {
			Population copiedPopulation = population.clone();
			copiedPopulation.setId(null);
			copiedPopulation.setGridId(null);
			copiedPopulation.setVersion(null);
			copiedStudy.addPopulation(copiedPopulation);
		}

		if (plannedCalendar != null) {
			List<Epoch> epoches = this.getPlannedCalendar().getChildren();
			for (int i = 0; i < epoches.size(); i++) {
				Epoch epoch = epoches.get(i);
				Epoch copiedEpoch = (Epoch) epoch.copy();
				copiedEpoch.setPlannedCalendar(null);
				List<StudySegment> studySegments = copiedEpoch.getChildren();

				for (StudySegment studySegment : studySegments) {
					SortedSet<Period> periods = studySegment.getPeriods();
					for (Period period : periods) {
						List<PlannedActivity> plannedActivities = period.getChildren();
						for (PlannedActivity plannedActivity : plannedActivities) {
							plannedActivity.setPopulation(Population.findMatchingPopulationByAbbreviation(copiedStudy.getPopulations(), plannedActivity.getPopulation()));
						}
					}
				}

				TemplateSkeletonCreatorImpl.addEpoch(copiedStudy, i, copiedEpoch);

			}
		}

		return copiedStudy;


	}

    @Transient
    public boolean isDetached() {
        return false;
    }
}