package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static edu.northwestern.bioinformatics.studycalendar.utils.FormatTools.*;
import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
import com.sun.mail.imap.protocol.INTERNALDATE;

/**
 * An amendment is a revision containing all the {@link Delta}s needed to
 * revert a calendar to its previous state.  The stored {@link edu.northwestern.bioinformatics.studycalendar.domain.Study}
 * always reflects the latest approved amendment.
 * <p>
 * For example, if you have a calendar C with amendments A0, A1, A2, and A3,
 * the calendar loaded from the database will reflect amendment A3.  If you want
 * to see the calendar as it existed at A1, you need to do a reverse merge from 3 to 2
 * and then from 2 to 1.
 * {@link edu.northwestern.bioinformatics.studycalendar.service.AmendmentService#getAmendedStudy}
 * implements this process.
 *
 * @author Rhett Sutphin
 * @see edu.northwestern.bioinformatics.studycalendar.service.DeltaService
 */
@Entity
@Table(name = "amendments")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_amendments_id")
    }
)
public class Amendment extends AbstractMutableDomainObject implements Revision, NaturallyKeyed {
    public static final String INITIAL_TEMPLATE_AMENDMENT_NAME = "[Original]";

    private Amendment previousAmendment;
    private Date date;
    private String name;
    private List<Delta<?>> deltas;
    private boolean mandatory;

    public Amendment() {
        this(null);
    }

    public Amendment(String name) {
        this.name = name;
        deltas = new ArrayList<Delta<?>>();
        mandatory = true;
    }

    ////// LOGIC

    @Transient
    public String getDisplayName() {
        if (INITIAL_TEMPLATE_AMENDMENT_NAME.equals(getName())) {
            return "Initial template";
        } else {
            StringBuilder n = new StringBuilder();
            if (getDate() != null) {
                n.append(formatDate(getDate()));
            } else {
                n.append("Timeless");
            }
            if (!StringUtils.isBlank(getName())) {
                n.append(" (").append(getName()).append(')');
            }
            return n.toString();
        }
    }

    @Transient
    public String getNaturalKey() {
        StringBuilder sb = new StringBuilder()
            .append(createNaturalKeyDateFormat().format(getDate()));
        if (getName() != null) {
            sb.append('~').append(getName());
        }
        return sb.toString();
    }

    public static DateFormat createNaturalKeyDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    @Transient
    public boolean isFirst() {
        return getPreviousAmendment() == null;
    }

    /**
     * Is this the internal representation of the unamended original protocol?
     * Note that this is not the same as {@link #isFirst} -- in theory the first version
     * of a protocol that PSC knows about could already be amended.  In that case,
     * {@link #isFirst} would be true but this method would return false.  (Note that
     * there's not currently any way to make this situation happen through the UI,
     * but it may be added later.)
     *
     * @see #isFirst
     */
    @Transient
    public boolean isInitialTemplate() {
        return INITIAL_TEMPLATE_AMENDMENT_NAME.equals(getName());
    }

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

    @Transient
    public int getPreviousAmendmentsCount() {
        if (getPreviousAmendment() == null) {
            return 0;
        } else {
            return getPreviousAmendment().getPreviousAmendmentsCount() + 1;
        }
    }

    public void addDelta(Delta<?> delta) {
        getDeltas().add(delta);
        delta.setRevision(this);
    }

    ////// BEAN PROPERTIES

    @OneToMany
    @JoinColumn(name = "amendment_id", nullable = false)
    @OrderBy // order by ID for testing consistency
    public List<Delta<?>> getDeltas() {
        return deltas;
    }

    public void setDeltas(List<Delta<?>> deltas) {
        this.deltas = deltas;
    }

    @ManyToOne
    @JoinColumn(name = "previous_amendment_id")
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

    @Column(name = "amendment_date", nullable = false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    ////// OBJECT METHODS

    @Override
    public String toString(){
        return new StringBuffer(getClass().getSimpleName())
            .append("[date=").append(getDate())
            .append("; name=").append(getName())
            .append("; prev=").append(getPreviousAmendment() == null ? null : getPreviousAmendment().getDisplayName())
            .append(']').toString();
    }
}
