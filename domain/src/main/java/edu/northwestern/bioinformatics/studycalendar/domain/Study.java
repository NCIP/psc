package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a study, its planned template, and all amendments.
 * <p>
 * When an instance of this class is loaded directly from the database, its
 * {@link #getPlannedCalendar, planned calendar} reflects the template as of
 * its last released amendment.  (If there is no released amendment, the
 * directly-accessible planned calendar is empty.)  The amendments list contains
 * all released amendments, deltas, and changes.  However, the node references
 * within the changes may not be complete if resolved against the database directly.
 * Use {@link edu.northwestern.bioinformatics.studycalendar.service.StudyService#getCompleteTemplateHistory}
 * if you need all node references complete.
 *
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
public class Study extends AbstractMutableDomainObject implements Serializable, Named, Cloneable, NaturallyKeyed, Parent<Population, Set<Population>> {
    private String assignedIdentifier;
    private String longTitle;
    private PlannedCalendar plannedCalendar;
    private LoadStatus loadStatus = LoadStatus.COMPLETE;

    private Amendment amendment;            // the current effective/released amendment
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
    @Deprecated
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
        if (getAmendment() != null) {
            getAmendment().setMemoryOnly(memoryOnly);
        }
        if (getDevelopmentAmendment() != null) {
            getDevelopmentAmendment().setMemoryOnly(memoryOnly);
        }
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
        return a == getDevelopmentAmendment()
            || a == getAmendment()
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

    // TODO: this method must just return Study
    public Map<Study,Set<Population>> copy(String newStudyName) {
        Map<Study,Set<Population>> studyPopulation = new TreeMap<Study,Set<Population>>();
        Study copiedStudy = new Study();
        copiedStudy.setAssignedIdentifier(newStudyName);
        copiedStudy.setLongTitle(this.getLongTitle());
        copiedStudy.setPlannedCalendar(new PlannedCalendar());
        Amendment devAmendment = new Amendment();
        devAmendment.addDelta(Delta.createDeltaFor(copiedStudy.getPlannedCalendar()));
        copiedStudy.setDevelopmentAmendment(devAmendment);

        copiedStudy.setStudySites(new ArrayList<StudySite>());
        Set<Population> populationSet = this.getPopulations();
        if (plannedCalendar != null) {
            List<Epoch> epochs = this.getPlannedCalendar().getChildren();
            for (int i = 0; i < epochs.size(); i++) {
                Epoch epoch = epochs.get(i);
                Epoch copiedEpoch = (Epoch) epoch.copy();
                copiedEpoch.setPlannedCalendar(null);
                devAmendment.getDeltas().get(0).addChange(Add.create(copiedEpoch, i));
            }
        }
        studyPopulation.put(copiedStudy,populationSet);
        return studyPopulation;
    }

    @Transient
    public boolean isDetached() {
        return false;
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
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
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
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudySite> getStudySites() {
        return studySites;
    }

    @OneToMany(mappedBy = "study")
    @OrderBy(value = "name")
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
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
            Study clone = (Study) super.clone();
            if (getPlannedCalendar() != null) {
                clone.setPlannedCalendar((PlannedCalendar) getPlannedCalendar().clone());
            }
            clone.setPopulations(new TreeSet<Population>());
            for (Population population : getPopulations()) {
                clone.addPopulation(population.clone());
            }
            if (getAmendment() != null) {
                clone.setAmendment(getAmendment().clone());
            }
            if (getDevelopmentAmendment() != null) {
                clone.setDevelopmentAmendment(getDevelopmentAmendment().clone());
            }
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Study)) return false;

        Study study = (Study) o;

        if (amendment != null ? !amendment.equals(study.amendment) : study.amendment != null)
            return false;
        if (assignedIdentifier != null ? !assignedIdentifier.equals(study.assignedIdentifier) : study.assignedIdentifier != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = assignedIdentifier != null ? assignedIdentifier.hashCode() : 0;
        result = 31 * result + (amendment != null ? amendment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName())
            .append("[id=").append(getId())
            .append("; assignedIdentifier=").append(getName());
        if (isMemoryOnly()) sb.append("; transient copy");
        return sb.append(']').toString();
    }
}