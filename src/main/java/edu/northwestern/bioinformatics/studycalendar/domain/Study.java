package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "studies")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_studies_id")
    }
)
@Where(clause = "load_status > 0")
public class Study extends AbstractMutableDomainObject implements Named, TransientCloneable<Study>, Cloneable {
    @Deprecated
    private String name;
    private String assignedIdentifier; // change it to assignedIdentifier...may be null
    private String longTitle;
    private PlannedCalendar plannedCalendar;
    private LoadStatus loadStatus = LoadStatus.COMPLETE;

    private Amendment amendment; // the current effective/approved amendment
    private Amendment developmentAmendment; // the next amendment, currently in development and not approved

    private List<StudySite> studySites = new ArrayList<StudySite>();

    private boolean memoryOnly = false;

    ////// LOGIC

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

    public void addStudySite(StudySite studySite){
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

    ////// BEAN PROPERTIES

    /**
     * The name of the study, also known as the protocol short title.
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The identifier given to the study by the organization in charge of it
     * @return
     */
    public String getAssignedIdentifier() {
        return assignedIdentifier;
    }

    public void setAssignedIdentifier(String assignedIdentifier) {
        this.assignedIdentifier = assignedIdentifier;
    }

    @OneToOne(mappedBy = "study")
    @Cascade(value = { CascadeType.ALL })
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
    protected Study clone() {
        try {
            // deep-clone the template portions only, for the moment
            Study clone = (Study) super.clone();
            if (getPlannedCalendar() != null) {
                clone.setPlannedCalendar((PlannedCalendar) getPlannedCalendar().clone());
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
            .append("; name=").append(getName());
        if (isMemoryOnly()) sb.append("; transient copy");
        return sb.append(']').toString();
    }
}