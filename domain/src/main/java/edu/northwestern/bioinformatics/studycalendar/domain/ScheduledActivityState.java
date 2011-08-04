package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_activity_stat_id")
    }
)
@Table(name = "scheduled_activity_states")
public class ScheduledActivityState extends AbstractMutableDomainObject implements Cloneable, Serializable {
    private ScheduledActivityMode mode;
    private String reason;
    private Date date;
    private Boolean withTime;

    protected ScheduledActivityState() { }

    public ScheduledActivityState(ScheduledActivityMode mode) {
        this.mode = mode;
        this.withTime = false;
    }

    public ScheduledActivityState(ScheduledActivityMode mode, Boolean withTime) {
        this.mode = mode;
        this.withTime = withTime;
    }

    public ScheduledActivityState(ScheduledActivityMode mode, Date date, String reason) {
        this.reason = reason;
        this.date = date;
        this.mode = mode;
        this.withTime = false;
    }

    public ScheduledActivityState(ScheduledActivityMode mode, Date date, String reason, Boolean withTime) {
        this.reason = reason;
        this.date = date;
        this.mode = mode;
        this.withTime = withTime;
    }

    ////// LOGIC

    @Transient
    public String getTextSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.capitalize(getMode().getName()));
        appendSummaryMiddle(sb);
        if (getReason() != null) sb.append(" - ").append(getReason());
        return sb.toString();
    }

    @Transient
    private void appendSummaryMiddle(StringBuilder sb) {
        sb.append(' ').append(getMode().getPreposition()).append(' ');
        // TODO: centrally configure date format
        if (getDate() != null) sb.append(new SimpleDateFormat("M/d/yyyy").format(getDate()));
    }

    ////// BEAN PROPERTIES

    @Type(type = "scheduledActivityMode")
    @Column(name = "mode_id")
    public ScheduledActivityMode getMode() {
        return mode;
    }

    /**
     * Warning: this setter is expected to be used in very particular circumstances.  If you
     * need to change the mode of a ScheduledActivity in regular practice, always use
     * {@link edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity#changeState}
     * to ensure that the history is preserved.
     */
    public void setMode(ScheduledActivityMode mode) {
        this.mode = mode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Column(name = "actual_date")
//    @Temporal(TemporalType.DATE)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getWithTime() {
        return withTime;
    }

    public void setWithTime(Boolean withTime) {
        this.withTime = withTime;
    }

    ////// OBJECT METHODS

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("It is cloneable", e);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledActivityState that = (ScheduledActivityState) o;
        if (mode != null ? !mode.equals(that.getMode()) : that.getMode() != null) return false;
        if (date != null ? !date.equals(that.getDate()) : that.getDate() != null) return false;
        if (reason != null ? !reason.equals(that.getReason()) : that.getReason() != null) return false;

        return true;
    }

    public int hashCode() {
        return (reason != null ? reason.hashCode() : 0);
    }
}
