package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;

/**
 * An amendment is a revision containing all the {@link edu.northwestern.bioinformatics.studycalendar.service.delta.Mutator}s needed to
 * revert a calendar to its previous state.  The stored {@link edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar}
 * always reflects the latest amendment.
 * <p>
 * For example, if you have a calendar C with amendments A0, A1, A2, and A3,
 * the calendar loaded from the database will reflect amendment A3.  If you want
 * to see the calendar as it existed at A1, you need to do a reverse merge from 3 to 2
 * and then from 2 to 1.  {@link edu.northwestern.bioinformatics.studycalendar.service.DeltaService#getAmendedCalendar}
 * implements this process.
 *
 * @author Rhett Sutphin
 * @see Customization
 * @see edu.northwestern.bioinformatics.studycalendar.service.DeltaService
 */
@Entity
@Table(name = "amendments")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_amendments_id")
    }
)
public class Amendment extends AbstractMutableDomainObject implements Revision {
    private Amendment previousAmendment;
    private String name;
    private List<Delta<?>> deltas;

    private Integer studyId;
    private String date;

    public Amendment() {
        this(null);
    }

    public Amendment(String name) {
        this.name = name;
        deltas = new ArrayList<Delta<?>>();
    }

    ////// LOGIC

    /**
     * Returns true IFF the candidate is the previous amendment of this
     * one or the previous of any of its previous amendments.  In other words, is the candidate
     * part of the history that terminates in this amendment?
     * @param candidate
     * @return
     */
    public boolean hasPreviousAmendment(Amendment candidate) {
        return this.getPreviousAmendment() != null
            && (this.getPreviousAmendment().equals(candidate)
                || this.getPreviousAmendment().hasPreviousAmendment(candidate));
    }

    public void addDelta(Delta<?> delta) {
        getDeltas().add(delta);
    }

    ////// BEAN PROPERTIES
    @OneToMany
    @JoinColumn(name = "amendment_id", nullable = false)
    @OrderBy // order by ID for testing consistency
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public List<Delta<?>> getDeltas() {
        return deltas;
    }

    public void setDeltas(List<Delta<?>> deltas) {
        this.deltas = deltas;
    }

    @ManyToOne
    @JoinColumn(name = "previous_amendment")
    public Amendment getPreviousAmendment() {
        return previousAmendment;
    }

    public void setPreviousAmendment(Amendment previousAmendment) {
        this.previousAmendment = previousAmendment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStudyId() {
        return studyId;
    }


    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }


    @Column(name = "amendment_date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(" StudyId = ");
        sb.append(getStudyId());
        sb.append(" Date = ");
        sb.append(getDate());
        sb.append(" Name = ");
        sb.append(getName());
        sb.append(" Previous Amendment = ");
        sb.append(getPreviousAmendment());
        return sb.toString();
    }

}
